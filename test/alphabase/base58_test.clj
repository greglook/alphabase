(ns alphabase.base58-test
  (:require
    [clojure.test :refer :all]
    [alphabase.base58 :as b58]
    [alphabase.bytes :refer [bytes= random-bytes]]))


(deftest encoding-test
  (is (nil? (b58/encode (byte-array 0)))
      "empty byte array encodes as nil")
  (is (= "1" (b58/encode (byte-array 1)))
      "single zero byte encodes as a zero char")
  (is (= "11" (b58/encode (byte-array 2)))
      "two zero bytes encode as two zero chars"))


(deftest decoding-test
  (is (nil? (b58/decode ""))
      "empty string decodes as nil")
  (is (bytes= (byte-array 1) (b58/decode "1"))
      "single zero char decodes as single zero byte")
  (is (bytes= (byte-array 2) (b58/decode "11"))
      "two zero chars decode as two zero bytes"))


(deftest reflexive-encoding
  (dotimes [i 10]
    (let [data (random-bytes 30)]
      (is (bytes= data (b58/decode (b58/encode data)))
          (str "Base58 coding is reflexive for "
               (pr-str (seq data)))))))
