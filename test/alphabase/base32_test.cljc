(ns alphabase.base32-test
  (:require
    [alphabase.base32 :as b32]
    [alphabase.bytes :as b]
    [clojure.test :refer [deftest is]]))


(deftest encoding-test
  (is (nil? (b32/encode (b/byte-array 0)))
      "empty byte array encodes as nil")
  (is (= "A" (b32/encode (b/byte-array 1)))
      "single zero byte encodes as a zero char")
  (is (= "AA" (b32/encode (b/byte-array 2)))
      "two zero bytes encode as two zero chars")
  (is (= "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567" (b32/encode (b/init-bytes [0 68 50 20 199 66 84 182 53 207 132 101 58 86 215 198 117 190 119 223]))))
  (is (= "AHD32DJBZG273DGBD572" (b32/encode (b/init-bytes [0 28 123 208 210 28 155 95 216 204 17 247 250])))))


(deftest decoding-test
  (is (nil? (b32/decode ""))
      "empty string decodes as nil")
  (is (b/bytes= (b/byte-array 1) (b32/decode "A"))
      "single zero char decodes as single zero byte")
  (is (b/bytes= (b/byte-array 2) (b32/decode "AA"))
      "two zero chars decode as two zero bytes")
  (is (= [0 68 50 20 199 66 84 182 53 207 132 101 58 86 215 198 117 190 119 223]
         (b/byte-seq (b32/decode "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"))))
  (is (= [0 28 123 208 210 28 155 95 216 204 17 247 250]
         (b/byte-seq (b32/decode "AHD32DJBZG273DGBD572")))))


(deftest reflexive-encoding
  (dotimes [_ 10]
    (let [data (b/random-bytes 30)]
      (is (b/bytes= data (b32/decode (b32/encode data)))
          (str "Base32 coding is reflexive for "
               (pr-str (b/byte-seq data)))))))
