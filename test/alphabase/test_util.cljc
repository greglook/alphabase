(ns alphabase.test-util
  "Unit test utilities.")


(defn str->bytes
  [s]
  #?(:clj (.getBytes ^String s)
     :cljs (let [encoder (js/TextEncoder.)]
             (.encode encoder s))))


;; RFC test vectors
(def f (str->bytes "f"))
(def fo (str->bytes "fo"))
(def foo (str->bytes "foo"))
(def foob (str->bytes "foob"))
(def fooba (str->bytes "fooba"))
(def foobar (str->bytes "foobar"))
