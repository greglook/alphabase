(defproject mvxcvi/alphabase "2.1.1"
  :description "Clojure(script) library to encode binary data with alphabet base strings."
  :url "https://github.com/greglook/alphabase"
  :license {:name "Public Domain"
            :url "http://unlicense.org/"}

  :deploy-branches ["main"]

  :aliases
  {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
   "clj:repl" ["repl"]
   "clj:check" ["check"]
   "clj:test" ["kaocha" "unit-clj"]
   "cljs:repl" ["run" "-m" "clojure.main" "dev/cljs_repl.clj"]
   "cljs:check" ["cljsbuild" "once"]
   "cljs:test" ["kaocha" "unit-cljs"]
   "coverage" ["kaocha" "--plugin" "cloverage" "unit-clj"]}

  :plugins
  [[lein-cljsbuild "1.1.8"]]

  :cljsbuild
  {:builds [{:id "test"
             :source-paths ["src" "test"]
             :compiler {:optimizations :whitespace
                        :output-dir "target/cljs/out"
                        :output-to "target/cljs/tests.js"}}]}

  :profiles
  {:dev
   {:dependencies
    [[org.clojure/clojure "1.10.3"]
     [org.clojure/clojurescript "1.10.879"]
     [criterium "0.4.6"]]}

   :kaocha
   {:dependencies
    [[lambdaisland/kaocha "1.60.945"]
     [lambdaisland/kaocha-cloverage "1.0.75"]
     [com.lambdaisland/kaocha-cljs "1.0.113"]]}})
