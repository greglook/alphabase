(ns alphabase.hex-test
  (:require
    #?(:clj [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest is testing]])
    [alphabase.bytes :as b]
    [alphabase.hex :as hex]))


(deftest encoding-test
  (is (nil? (hex/encode (b/byte-array 0)))
      "empty byte array encodes as nil")
  (is (= "00" (hex/encode (b/byte-array 1)))
      "single zero byte encodes as two zero chars")
  (is (= "007f" (hex/encode (doto (b/byte-array 2)
                              (b/set-byte 1 127))))))


(deftest decoding-test
  (is (nil? (hex/decode ""))
      "empty string decodes as nil")
  (is (b/bytes= (b/byte-array 1) (hex/decode "00"))
      "single zero char decodes as single zero byte"))


(deftest reflexive-encoding
  (dotimes [i 10]
    (let [data (b/random-bytes 30)
          encoded (hex/encode data)
          decoded (hex/decode encoded)]
      (is (b/bytes= data decoded)
          (str "Hex coding is reflexive for "
               (pr-str (b/byte-seq data)))))))


(deftest hex-validation
  (testing "validation errors"
    (is (re-seq #"not a string" (hex/validate 123)))
    (is (re-seq #"contains illegal characters" (hex/validate "012xabc")))
    (is (re-seq #"must contain at least one byte" (hex/validate "0")))
    (is (re-seq #"number of characters .+ is odd" (hex/validate "012ab")))
    (is (nil? (hex/validate "012abc"))))
  (testing "validation predicate"
    (is (false? (hex/valid? 123)))
    (is (false? (hex/valid? "012xabc")))
    (is (false? (hex/valid? "0")))
    (is (false? (hex/valid? "012ab")))
    (is (true? (hex/valid? "012abc")))))
