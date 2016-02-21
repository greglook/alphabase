(ns alphabase.hex-test
  (:require
    [clojure.test :refer :all]
    [alphabase.hex :as hex]
    [alphabase.bytes :as b]))


(deftest encoding-test
  (is (nil? (hex/encode (b/byte-array 0)))
      "empty byte array encodes as nil")
  (is (= "00" (hex/encode (b/byte-array 1)))
      "single zero byte encodes as two zero chars")
  (is (= "007f" (hex/encode (doto (b/byte-array 2)
                              (b/set-byte 1 127))))))


(deftest decoding-test
  (is (b/bytes= (b/byte-array 0) (hex/decode ""))
      "empty string decodes as empty byte array")
  (is (b/bytes= (b/byte-array 1) (hex/decode "00"))
      "single zero char decodes as single zero byte"))


(deftest reflexive-encoding
  (dotimes [i 10]
    (let [data (b/random-bytes 30)
          encoded (hex/encode data)
          decoded (hex/decode encoded)]
      (is (b/bytes= data decoded)
          (str "Hex coding is reflexive for "
               (pr-str (seq data)))))))
