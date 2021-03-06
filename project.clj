(defproject mvxcvi/alphabase "2.1.1-SNAPSHOT"
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
   "cloverage" ["with-profile" "+coverage" "cloverage"]}

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

  :profiles
  {:dev
   {:dependencies
    [[criterium "0.4.5"]
     [org.clojure/clojure "1.10.1"]
     [org.clojure/clojurescript "1.10.597"]]}

   :doo
   {:dependencies
    [[doo "0.1.11"]]}

   :coverage
   {:plugins
    [[org.clojure/clojure "1.10.1"]
     [lein-cloverage "1.1.2"]]}})
