(ns build
  "Build instructions for alphabase.

  Different tasks accept different arguments, but some common ones are:

  - `:force`
    Perform actions without prompting for user input.
  - `:qualifier`
    Apply a qualifier to the release, such as 'rc1'.
  - `:snapshot`
    If true, prepare a SNAPSHOT release."
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.tools.build.api :as b]
    [deps-deploy.deps-deploy :as d])
  (:import
    java.time.LocalDate))


(def basis (b/create-basis {:project "deps.edn"}))

(def lib-name 'mvxcvi/alphabase)
(def major-version "2.2")

(def src-dir "src")
(def class-dir "target/classes")


;; ## Utilities

(defn clean
  "Remove compiled artifacts."
  [opts]
  (b/delete {:path "target"})
  opts)


;; ## Version and Releases

(defn- version-info
  "Compute the current version information."
  ([opts]
   (version-info opts false))
  ([opts next?]
   {:tag (str major-version
              "."
              (cond-> (parse-long (b/git-count-revs nil))
                next? inc)
              (when-let [qualifier (:qualifier opts)]
                (str "-" qualifier))
              (when (:snapshot opts)
                "-SNAPSHOT"))
    :commit (b/git-process {:git-args "rev-parse HEAD"})
    :date (str (LocalDate/now))}))


(defn- format-version
  "Format the version string from the `version-info` map."
  [version]
  (let [{:keys [tag commit date]} version]
    (format "mvxcvi/alphabase %s (built from %s on %s)" tag commit date)))


(defn print-version
  "Print the current version information."
  [opts]
  (let [version (version-info opts)]
    (println (format-version version))
    (assoc opts :version version)))


(defn- update-changelog
  "Stamp the CHANGELOG file with the new version."
  [version]
  (let [{:keys [tag date]} version
        file (io/file "CHANGELOG.md")
        changelog (slurp file)]
    (when (str/includes? changelog "## [Unreleased]\n\n...\n")
      (binding [*out* *err*]
        (println "Changelog does not appear to have been updated with changes, aborting")
        (System/exit 3)))
    (-> changelog
        (str/replace #"## \[Unreleased\]"
                     (str "## [Unreleased]\n\n...\n\n\n"
                          "## [" tag "] - " date))
        (str/replace #"\[Unreleased\]: (\S+/compare)/(\S+)\.\.\.HEAD"
                     (str "[Unreleased]: $1/" tag "...HEAD\n"
                          "[" tag "]: $1/$2..." tag))
        (->> (spit file)))))


(defn prep-release
  "Prepare the repository for release."
  [opts]
  (let [status (b/git-process {:git-args "status --porcelain --untracked-files=no"})]
    (when-not (str/blank? status)
      (binding [*out* *err*]
        (println "Uncommitted changes in local repository, aborting")
        (System/exit 2))))
  (let [version (version-info opts true)
        tag (:tag version)]
    (update-changelog version)
    (b/git-process {:git-args ["commit" "-am" (str "Prepare release " tag)]})
    (b/git-process {:git-args ["tag" tag "-s" "-m" (str "Release " tag)]})
    (println "Prepared release for" tag)
    (assoc opts :version version)))


;; ## Library Installation

(defn javac
  "Compile the Java code to a class file."
  [opts]
  (b/javac
    {:basis basis
     :src-dirs [src-dir]
     :class-dir class-dir
     :javac-opts ["--release" "11"]})
  opts)


(defn- pom-template
  "Generate template data for the Maven pom.xml file."
  [version-tag]
  [[:description "Clojure(script) library to encode binary data with alphabet base strings."]
   [:url "https://github.com/greglook/alphabase"]
   [:licenses
    [:license
     [:name "Public Domain"]
     [:url "https://unlicense.org/"]]]
   [:scm
    [:url "https://github.com/greglook/alphabase"]
    [:connection "scm:git:https://github.com/greglook/alphabase.git"]
    [:developerConnection "scm:git:ssh://git@github.com/greglook/alphabase.git"]
    [:tag version-tag]]])


(defn pom
  "Write out a pom.xml file for the project."
  [opts]
  (let [version (version-info opts)
        pom-file (b/pom-path
                   {:class-dir class-dir
                    :lib lib-name})]
    (b/write-pom
      {:basis basis
       :lib lib-name
       :version (:tag version)
       :src-dirs [src-dir]
       :class-dir class-dir
       :pom-data (pom-template
                   (if (or (:snapshot opts) (:qualifier opts))
                     (:commit version)
                     (:tag version)))})
    (assoc opts
           :version version
           :pom-file pom-file)))


(defn jar
  "Build a JAR file for distribution."
  [opts]
  (let [opts (pom opts)
        version (:version opts)
        jar-file (format "target/%s-%s.jar"
                         (name lib-name)
                         (:tag version))]
    ;; TODO: this ships the Codec java source - does it matter?
    (b/copy-dir
      {:src-dirs [src-dir]
       :target-dir class-dir})
    (javac opts)
    (b/jar
      {:class-dir class-dir
       :jar-file jar-file})
    (assoc opts :jar-file jar-file)))


(defn install
  "Install a JAR into the local Maven repository."
  [opts]
  (let [opts (-> opts clean jar)
        version (:version opts)]
    (b/install
      {:basis basis
       :lib lib-name
       :version (:tag version)
       :jar-file (:jar-file opts)
       :class-dir class-dir})
    (println "Installed version" (:tag version) "to local repository")
    opts))


;; ## Clojars Deployment

(defn deploy
  "Publish the library to Clojars."
  [opts]
  (let [opts (-> opts clean jar)
        version (:version opts)
        signing-key-id (System/getenv "CLOJARS_SIGNING_KEY")
        proceed? (or (:force opts)
                     (and
                       (or signing-key-id
                           (do
                             (print "No signing key specified - proceed without signature? [yN] ")
                             (flush)
                             (= "y" (str/lower-case (read-line)))))
                       (do
                         (printf "About to deploy version %s to Clojars - proceed? [yN] "
                                 (:tag version))
                         (flush)
                         (= "y" (str/lower-case (read-line))))))]
    (if proceed?
      (d/deploy
        (-> opts
            (assoc :installer :remote
                   :pom-file (:pom-file opts)
                   :artifact (:jar-file opts))
            (cond->
              signing-key-id
              (assoc :sign-releases? true
                     :sign-key-id signing-key-id))))
      (binding [*out* *err*]
        (println "Aborting deploy")
        (System/exit 1)))
    opts))
