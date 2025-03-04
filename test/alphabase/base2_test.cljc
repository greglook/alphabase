(ns alphabase.base2-test
  (:require
    [alphabase.base2 :as b2]
    [alphabase.bytes :as b]
    [clojure.test :refer [deftest testing is]]))


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (b2/encode nil))
        "nil encodes to nil")
    (is (nil? (b2/encode (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "basic examples"
    (is (= "00000000" (b2/encode (b/byte-array 1)))
        "single zero byte encodes as three zero chars")
    (is (= "0000000001111111"
           (b2/encode (doto (b/byte-array 2)
                        (b/set-byte 1 127)))))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (b2/decode nil))
        "nil argument decodes to nil")
    (is (nil? (b2/decode ""))
        "empty string decodes to nil")
    (is (thrown-with-msg? #?(:clj Exception, :cljs js/Error)
                          #"Character '@' at index 2 is not a valid binary digit"
          (b2/decode "01@00101"))))
  (testing "basic examples"
    (is (b/bytes= (b/byte-array 1) (b2/decode "0"))
        "zero decodes as single zero byte")
    (is (b/bytes= (b/init-bytes [0 127])
                  (b2/decode "0000000001111111")))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 8))
          data (b/random-bytes length)]
      (testing "reflexive encoding"
        (is (b/bytes= data (b2/decode (b2/encode data)))
            (str "byte sequence "
                 (pr-str (b/byte-seq data))
                 " should round-trip"))))))
