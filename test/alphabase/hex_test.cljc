(ns alphabase.hex-test
  (:require
    [alphabase.bytes :as b]
    [alphabase.hex :as hex]
    [alphabase.test-util :as t]
    [clojure.test :refer [deftest testing is]]))


(deftest byte-helpers
  (testing "byte->hex"
    (is (= "00" (hex/byte->hex 0)))
    (is (= "03" (hex/byte->hex 3)))
    (is (= "10" (hex/byte->hex 16)))
    (is (= "7F" (hex/byte->hex 127)))
    (is (= "FF" (hex/byte->hex 255))))
  (testing "hex->byte"
    (is (= 0 (hex/hex->byte "0")))
    (is (= 1 (hex/hex->byte "01")))
    (is (= 16 (hex/hex->byte "10")))
    (is (= 127 (hex/hex->byte "7f")))
    (is (= 127 (hex/hex->byte "7F")))
    (is (= 255 (hex/hex->byte "FF")))))


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (hex/encode nil))
        "nil encodes to nil")
    (is (nil? (hex/encode (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "basic examples"
    (is (= "00" (hex/encode (b/byte-array 1)))
        "single zero byte encodes as two zero chars")
    (is (= "007F" (hex/encode (doto (b/byte-array 2)
                                (b/set-byte 1 127))))))
  (testing "RFC examples"
    (is (= "66" (hex/encode t/f)))
    (is (= "666F" (hex/encode t/fo)))
    (is (= "666F6F" (hex/encode t/foo)))
    (is (= "666F6F62" (hex/encode t/foob)))
    (is (= "666F6F6261" (hex/encode t/fooba)))
    (is (= "666F6F626172" (hex/encode t/foobar)))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (hex/decode nil))
        "nil argument decodes to nil")
    (is (nil? (hex/decode ""))
        "empty string decodes to nil")
    (is (thrown-with-msg? #?(:clj Exception, :cljs js/Error)
                          #?(:clj #"Character '@' at index 3 is not a valid hexadecimal digit"
                             :cljs #"Characters '2@' at index 2 are not valid hexadecimal digits")
          (hex/decode "012@4abc")))
    #?(:clj
       (is (thrown-with-msg? Exception
                             #"Characters '2@' at index 2 are not valid hexadecimal digits"
             (#'hex/decode* "012@4abc")))))
  (testing "basic examples"
    (is (b/bytes= (b/byte-array 1) (hex/decode "00"))
        "zero decodes as single zero byte")
    (is (b/bytes= (b/init-bytes [10 0 127])
                  (hex/decode "0a007f"))))
  (testing "RFC examples"
    (is (= [0x66] (b/byte-seq (hex/decode "66"))))
    (is (= [0x66 0x6F] (b/byte-seq (hex/decode "666F"))))
    (is (= [0x66 0x6F 0x6F] (b/byte-seq (hex/decode "666F6F"))))
    (is (= [0x66 0x6F 0x6F 0x62] (b/byte-seq (hex/decode "666F6F62"))))
    (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (hex/decode "666F6F6261"))))
    (is (= [0x66 0x6F 0x6F 0x62 0x61 0x72] (b/byte-seq (hex/decode "666F6F626172"))))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 128))
          data (b/random-bytes length)]
      (testing "reflexive encoding"
        (is (b/bytes= data (hex/decode (hex/encode data)))
            (str "byte sequence "
                 (pr-str (b/byte-seq data))
                 " should round-trip")))
      #?(:clj
         (testing "pure vs fast"
           (let [string (#'hex/encode* data)]
             (is (= string (hex/encode data))
                 (str "encoding byte sequence "
                      (pr-str (b/byte-seq data))
                      " should produce identical strings"))
             (is (b/bytes= (#'hex/decode* string)
                           (hex/decode string))
                 (str "decoding string " string
                      " should produce identical byte sequences"))))))))
