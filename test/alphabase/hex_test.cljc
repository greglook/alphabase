(ns alphabase.hex-test
  (:require
    [alphabase.bytes :as b]
    [alphabase.hex :as hex]
    [clojure.test :refer [deftest testing is]]))


(deftest byte-helpers
  (testing "byte->hex"
    (is (= "00" (hex/byte->hex 0)))
    (is (= "03" (hex/byte->hex 3)))
    (is (= "10" (hex/byte->hex 16)))
    (is (= "7f" (hex/byte->hex 127)))
    (is (= "ff" (hex/byte->hex 255))))
  (testing "hex->byte"
    (is (= 0 (hex/hex->byte "0")))
    (is (= 1 (hex/hex->byte "01")))
    (is (= 16 (hex/hex->byte "10")))
    (is (= 127 (hex/hex->byte "7f")))
    (is (= 127 (hex/hex->byte "7F")))
    (is (= 255 (hex/hex->byte "FF")))))


(deftest encoding-test
  (is (nil? (hex/encode nil))
      "nil argument encodes to nil")
  (is (nil? (hex/encode (b/byte-array 0)))
      "empty byte array encodes to nil")
  (is (= "00" (hex/encode (b/byte-array 1)))
      "single zero byte encodes as two zero chars")
  (is (= "007f" (hex/encode (doto (b/byte-array 2)
                              (b/set-byte 1 127))))))


(deftest decoding-test
  (is (nil? (hex/decode nil))
      "nil argument decodes to nil")
  (is (nil? (hex/decode ""))
      "empty string decodes to nil")
  (is (b/bytes= (b/byte-array 1) (hex/decode "00"))
      "zero decodes as single zero byte")
  (is (b/bytes= (b/init-bytes [10 0 127])
                (hex/decode "0a007f"))))


(deftest reflexive-encoding
  (dotimes [_ 10]
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
