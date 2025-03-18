(ns alphabase.base16-test
  (:require
    [alphabase.base16 :as b16]
    [alphabase.bytes :as b]
    [alphabase.test-util :as t]
    [clojure.test :refer [deftest testing is]]))


(deftest byte-helpers
  (testing "byte->hex"
    (is (= "00" (b16/byte->hex 0)))
    (is (= "03" (b16/byte->hex 3)))
    (is (= "10" (b16/byte->hex 16)))
    (is (= "7F" (b16/byte->hex 127)))
    (is (= "FF" (b16/byte->hex 255))))
  (testing "hex->byte"
    (is (= 0 (b16/hex->byte "0")))
    (is (= 1 (b16/hex->byte "01")))
    (is (= 16 (b16/hex->byte "10")))
    (is (= 127 (b16/hex->byte "7f")))
    (is (= 127 (b16/hex->byte "7F")))
    (is (= 255 (b16/hex->byte "FF")))))


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (b16/encode nil))
        "nil encodes to nil")
    (is (nil? (b16/encode (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "basic examples"
    (is (= "00" (b16/encode (b/byte-array 1)))
        "single zero byte encodes as two zero chars")
    (is (= "007F" (b16/encode (doto (b/byte-array 2)
                                (b/set-byte 1 127))))))
  (testing "RFC examples"
    (is (= "66" (b16/encode t/f)))
    (is (= "666F" (b16/encode t/fo)))
    (is (= "666F6F" (b16/encode t/foo)))
    (is (= "666F6F62" (b16/encode t/foob)))
    (is (= "666F6F6261" (b16/encode t/fooba)))
    (is (= "666F6F626172" (b16/encode t/foobar)))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (b16/decode nil))
        "nil argument decodes to nil")
    (is (nil? (b16/decode ""))
        "empty string decodes to nil")
    (is (thrown-with-msg? #?(:clj Exception, :cljs js/Error)
                          #?(:bb #"Characters '2@' at index 2 are not valid hexadecimal digits"
                             :clj #"Character '@' at index 3 is not a valid hexadecimal digit"
                             :cljs #"Characters '2@' at index 2 are not valid hexadecimal digits")
          (b16/decode "012@4abc")))
    #?(:clj
       (is (thrown-with-msg? Exception
                             #"Characters '2@' at index 2 are not valid hexadecimal digits"
             (#'b16/decode* "012@4abc")))))
  (testing "basic examples"
    (is (b/bytes= (b/byte-array 1) (b16/decode "00"))
        "zero decodes as single zero byte")
    (is (b/bytes= (b/init-bytes [10 0 127])
                  (b16/decode "0a007f"))))
  (testing "RFC examples"
    (is (= [0x66] (b/byte-seq (b16/decode "66"))))
    (is (= [0x66 0x6F] (b/byte-seq (b16/decode "666F"))))
    (is (= [0x66 0x6F 0x6F] (b/byte-seq (b16/decode "666F6F"))))
    (is (= [0x66 0x6F 0x6F 0x62] (b/byte-seq (b16/decode "666F6F62"))))
    (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b16/decode "666F6F6261"))))
    (is (= [0x66 0x6F 0x6F 0x62 0x61 0x72] (b/byte-seq (b16/decode "666F6F626172"))))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 128))
          data (b/random-bytes length)]
      (testing "reflexive encoding"
        (is (b/bytes= data (b16/decode (b16/encode data)))
            (str "byte sequence "
                 (pr-str (b/byte-seq data))
                 " should round-trip")))
      #?(:clj
         (testing "pure vs fast"
           (let [string (#'b16/encode* data)]
             (is (= string (b16/encode data))
                 (str "encoding byte sequence "
                      (pr-str (b/byte-seq data))
                      " should produce identical strings"))
             (is (b/bytes= (#'b16/decode* string)
                           (b16/decode string))
                 (str "decoding string " string
                      " should produce identical byte sequences"))))))))
