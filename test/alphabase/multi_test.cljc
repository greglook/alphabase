(ns alphabase.multi-test
  (:require
    #?(:clj [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest is testing]])
    [alphabase.bytes :as b]
    [alphabase.multi :as mb]))


(deftest encoding-test
  (testing "exceptional-cases"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":base420 is not a supported encoding"
          (mb/encode :base420 (b/byte-array 5)))))
  (testing "empty byte array"
    (is (nil? (mb/encode :base16 (b/byte-array 0)))
        "encodes as nil hex")
    (is (nil? (mb/encode :base58btc (b/byte-array 0)))
        "encodes as nil base58"))
  (testing "single byte"
    (is (= "f00" (mb/encode :base16 (b/byte-array 1)))
        "encodes as two zero chars with prefix")
    (is (= "z1" (mb/encode :base58btc (b/byte-array 1)))
        "encodes as two zero chars with prefix"))
  (is (= "f007f" (mb/encode :base16
                            (doto (b/byte-array 2)
                              (b/set-byte 1 127))))))


(deftest decoding-test
  (testing "exceptional cases"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #":base1 is not a supported encoding"
          (mb/decode "111111")))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Prefix \\- does not map to a known base"
          (mb/decode "-abcd"))))
  (testing "base encoding prefixes"
    (is (= :base16 (mb/encoded-base "F0123")))
    (is (= :base16 (mb/encoded-base "F0123")))
    (is (= :base58btc (mb/encoded-base "zQm45")))
    (is (= :base64url (mb/encoded-base "Y01fk"))))
  (is (nil? (mb/decode ""))
      "empty string decodes as nil")
  (is (b/bytes= [0] (mb/decode "f00")))
  (is (b/bytes= [0] (mb/decode "z1")))
  (is (b/bytes= [0 127] (mb/decode "f007f"))))
