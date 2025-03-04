(ns alphabase.base8-test
  (:require
    [alphabase.base8 :as b8]
    [alphabase.bytes :as b]
    [clojure.test :refer [deftest testing is]]))


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (b8/encode nil))
        "nil encodes to nil")
    (is (nil? (b8/encode (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "basic examples"
    (is (= "000" (b8/encode (b/byte-array 1)))
        "single zero byte encodes as three zero chars")
    (is (= "000774" (b8/encode (doto (b/byte-array 2)
                                 (b/set-byte 1 127)))))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (b8/decode nil))
        "nil argument decodes to nil")
    (is (nil? (b8/decode ""))
        "empty string decodes to nil")
    (is (thrown-with-msg? #?(:clj Exception, :cljs js/Error)
                          #"Character '@' at index 3 is not a valid octal digit"
          (b8/decode "012@4"))))
  (testing "basic examples"
    (is (b/bytes= (b/byte-array 1) (b8/decode "000"))
        "zero decodes as single zero byte")
    (is (b/bytes= (b/init-bytes [0 127])
                  (b8/decode "000774")))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 32))
          data (b/random-bytes length)]
      (testing "reflexive encoding"
        (is (b/bytes= data (b8/decode (b8/encode data)))
            (str "byte sequence "
                 (pr-str (b/byte-seq data))
                 " should round-trip"))))))
