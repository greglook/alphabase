(defproject mvxcvi/alphabase "2.0.5-SNAPSHOT"
  :description "Clojure(script) library to encode binary data with alphabet base strings."
  :url "https://github.com/greglook/alphabase"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["master"]

  :aliases
  {"clj:test" ["do" ["check"] ["test"]]
   "cljs:check" ["with-profile" "+doo" "cljsbuild" "once"]
   "cljs:repl" ["run" "-m" "clojure.main" "dev/cljs_repl.clj"]
   "cljs:test" ["doo" "rhino" "test" "once"]
   "coverage" ["with-profile" "+coverage" "cloverage"]}

  :plugins
  [[lein-cljsbuild "1.1.7"]
   [lein-doo "0.1.11"]]

  :cljsbuild
  {:builds [{:id "test"
             :source-paths ["src" "test"]
             :compiler {:optimizations :whitespace
                        :output-dir "target/cljs/out"
                        :output-to "target/cljs/tests.js"
                        :main alphabase.test-runner}}]}

  :codox
  {:metadata {:doc/format :markdown}
   :source-uri "https://github.com/greglook/alphabase/blob/master/{filepath}#L{line}"
   :output-path "target/doc/api"}

  :profiles
  {:dev
   {:dependencies
    [[criterium "0.4.4"]
     [org.clojure/clojure "1.9.0"]
     [org.clojure/clojurescript "1.10.439"]]}

   :doo
   {:dependencies
    [[doo "0.1.11"]]}

   :coverage
   {:plugins
    [[org.clojure/clojure "1.10.0"]
     [lein-cloverage "1.1.0"]]}})
