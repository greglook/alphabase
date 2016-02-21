(defproject mvxcvi/alphabase "0.2.0-SNAPSHOT"
  :description "Clojure(script) library to encode binary data with alphabet base strings."
  :url "https://github.com/greglook/alphabase"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :aliases {"node-repl" ["run" "-m" "clojure.main" "node_repl.clj"]}

  :plugins
  [[lein-cljsbuild "1.1.2"]
   [lein-doo "0.1.6"]]

  :cljsbuild
  {:builds [{:id "test-nodejs"
             :source-paths ["src" "test"]
             :compiler {:output-dir "target/cljs/out"
                        :output-to "target/cljs/tests-node.js"
             :main alphabase.test-runner
             :optimizations :none
             :target :nodejs}}]}

  :codox
  {:metadata {:doc/format :markdown}
   :source-uri "https://github.com/greglook/alphabase/blob/master/{filepath}#L{line}"
   :doc-paths [""]
   :output-path "doc/api"}

  :profiles
  {:dev {:dependencies
         [[criterium "0.4.4"]
          [org.clojure/clojure "1.7.0"]
          [org.clojure/clojurescript "1.7.170"]]}})
