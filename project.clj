(defproject mvxcvi/alphabase "0.1.0-SNAPSHOT"
  :description "Clojure(script) library to encode binary data with alphabet base strings."
  :url "https://github.com/greglook/alphabase"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :aliases {"node-repl" ["run" "-m" "clojure.main" "node_repl.clj"]}

  :profiles
  {:dev {:dependencies
         [[org.clojure/clojure "1.7.0"]
          [org.clojure/clojurescript "1.7.170"]]}})
