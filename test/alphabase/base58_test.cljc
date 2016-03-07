(ns alphabase.base58-test
  (:require
    #?(:clj [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest is testing]])
    [alphabase.base58 :as b58]
    [alphabase.bytes :as b]))


(deftest encoding-test
  (is (nil? (b58/encode (b/byte-array 0)))
      "empty byte array encodes as nil")
  (is (= "1" (b58/encode (b/byte-array 1)))
      "single zero byte encodes as a zero char")
  (is (= "11" (b58/encode (b/byte-array 2)))
      "two zero bytes encode as two zero chars")
  (is (= "SZAGsv33aLcoudQhUGqRiy" (b58/encode (b/init-bytes [206 241 251 119 171 175 222 43 229 46 42 96 211 113 239 178]))))
  (is (= "NjKBom1a4wfq24FUN9DhFR" (b58/encode (b/init-bytes [175 248 91 160 59 158 190 226 43 83 14 172 225 89 130 36])))))


(deftest decoding-test
  (is (nil? (b58/decode ""))
      "empty string decodes as nil")
  (is (b/bytes= (b/byte-array 1) (b58/decode "1"))
      "single zero char decodes as single zero byte")
  (is (b/bytes= (b/byte-array 2) (b58/decode "11"))
      "two zero chars decode as two zero bytes")
  (is (= [105 205 195 119 174 173 71 209 51 100 228 186 146 110 189 150]
         (b/byte-seq (b58/decode "E4mzkv6NcaHwm7zhgLBqYq"))))
  (is (= [250 23 163 73 100 205 245 86 251 16 189 82 135 104 29 145]
         (b/byte-seq (b58/decode "XtBd3vqJxY8Knhqs2ewdxk")))))


(deftest reflexive-encoding
  (dotimes [i 10]
    (let [data (b/random-bytes 30)]
      (is (b/bytes= data (b58/decode (b58/encode data)))
          (str "Base58 coding is reflexive for "
               (pr-str (b/byte-seq data)))))))
