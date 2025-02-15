(ns alphabase.base32-test
  (:require
    [alphabase.base32 :as b32]
    [alphabase.bytes :as b]
    [alphabase.test-util :as t]
    [clojure.test :refer [deftest testing is]]))


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (b32/encode nil))
        "nil encodes to nil")
    (is (nil? (b32/encode (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "RFC examples"
    (testing "for base32"
      (testing "without padding"
        (is (= "MY" (b32/encode t/f)))
        (is (= "MZXQ" (b32/encode t/fo)))
        (is (= "MZXW6" (b32/encode t/foo)))
        (is (= "MZXW6YQ" (b32/encode t/foob)))
        (is (= "MZXW6YTB" (b32/encode t/fooba)))
        (is (= "MZXW6YTBOI" (b32/encode t/foobar))))
      (testing "with padding"
        (is (= "MY======" (b32/encode t/f false true)))
        (is (= "MZXQ====" (b32/encode t/fo false true)))
        (is (= "MZXW6===" (b32/encode t/foo false true)))
        (is (= "MZXW6YQ=" (b32/encode t/foob false true)))
        (is (= "MZXW6YTB" (b32/encode t/fooba false true)))
        (is (= "MZXW6YTBOI======" (b32/encode t/foobar false true)))))
    (testing "for base32hex"
      (testing "without padding"
        (is (= "CO" (b32/encode t/f true)))
        (is (= "CPNG" (b32/encode t/fo true)))
        (is (= "CPNMU" (b32/encode t/foo true)))
        (is (= "CPNMUOG" (b32/encode t/foob true)))
        (is (= "CPNMUOJ1" (b32/encode t/fooba true)))
        (is (= "CPNMUOJ1E8" (b32/encode t/foobar true))))
      (testing "with padding"
        (is (= "CO======" (b32/encode t/f true true)))
        (is (= "CPNG====" (b32/encode t/fo true true)))
        (is (= "CPNMU===" (b32/encode t/foo true true)))
        (is (= "CPNMUOG=" (b32/encode t/foob true true)))
        (is (= "CPNMUOJ1" (b32/encode t/fooba true true)))
        (is (= "CPNMUOJ1E8======" (b32/encode t/foobar true true)))))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (b32/decode nil))
        "nil decodes to nil")
    (is (nil? (b32/decode ""))
        "empty string decodes to nil")
    (is (thrown-with-msg? #?(:clj Exception, :cljs js/Error)
                          #"Character '@' at index 3 is not a valid Base32 digit"
          (b32/decode "MZX@W6YQ")))
    #?(:clj
       (is (thrown-with-msg? Exception
                             #"Character '@' at index 3 is not a valid Base32 digit"
             (#'b32/decode* "MZX@W6YQ" false)))))
  (testing "RFC examples"
    (testing "for base32"
      (testing "without padding"
        (is (= [0x66] (b/byte-seq (b32/decode "MY"))))
        (is (= [0x66 0x6F] (b/byte-seq (b32/decode "MZXQ"))))
        (is (= [0x66 0x6F 0x6F] (b/byte-seq (b32/decode "MZXW6"))))
        (is (= [0x66 0x6F 0x6F 0x62] (b/byte-seq (b32/decode "MZXW6YQ"))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b32/decode "MZXW6YTB"))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61 0x72] (b/byte-seq (b32/decode "MZXW6YTBOI")))))
      (testing "with padding"
        (is (= [0x66] (b/byte-seq (b32/decode "MY======"))))
        (is (= [0x66 0x6F] (b/byte-seq (b32/decode "MZXQ===="))))
        (is (= [0x66 0x6F 0x6F] (b/byte-seq (b32/decode "MZXW6==="))))
        (is (= [0x66 0x6F 0x6F 0x62] (b/byte-seq (b32/decode "MZXW6YQ="))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b32/decode "MZXW6YTB"))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61 0x72] (b/byte-seq (b32/decode "MZXW6YTBOI======")))))
      (testing "with mixed case"
        (is (= [0x66 0x6F 0x6F] (b/byte-seq (b32/decode "mzxw6"))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b32/decode "mZxW6yTb"))))))
    (testing "for base32hex"
      (testing "without padding"
        (is (= [0x66] (b/byte-seq (b32/decode "CO" true))))
        (is (= [0x66 0x6F] (b/byte-seq (b32/decode "CPNG" true))))
        (is (= [0x66 0x6F 0x6F] (b/byte-seq (b32/decode "CPNMU" true))))
        (is (= [0x66 0x6F 0x6F 0x62] (b/byte-seq (b32/decode "CPNMUOG" true))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b32/decode "CPNMUOJ1" true))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61 0x72] (b/byte-seq (b32/decode "CPNMUOJ1E8" true)))))
      (testing "with padding"
        (is (= [0x66] (b/byte-seq (b32/decode "CO======" true))))
        (is (= [0x66 0x6F] (b/byte-seq (b32/decode "CPNG====" true))))
        (is (= [0x66 0x6F 0x6F] (b/byte-seq (b32/decode "CPNMU===" true))))
        (is (= [0x66 0x6F 0x6F 0x62] (b/byte-seq (b32/decode "CPNMUOG=" true))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b32/decode "CPNMUOJ1" true))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61 0x72] (b/byte-seq (b32/decode "CPNMUOJ1E8======" true)))))
      (testing "with mixed case"
        (is (= [0x66 0x6F 0x6F] (b/byte-seq (b32/decode "cpnmu" true))))
        (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b32/decode "cPnMuOj1" true))))))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 128))
          data (b/random-bytes length)
          message (str "byte sequence "
                       (pr-str (b/byte-seq data))
                       " should round-trip")]
      (testing "reflexive encoding"
        (testing "for base32"
          (is (b/bytes= data (b32/decode (b32/encode data)))
              message))
        (testing "for base32 with padding"
          (is (b/bytes= data (b32/decode (b32/encode data false true)))
              message))
        (testing "for base32hex"
          (is (b/bytes= data (b32/decode (b32/encode data true) true))
              message))
        (testing "for base32hex with padding"
          (is (b/bytes= data (b32/decode (b32/encode data true true) true))
              message)))
      #?(:clj
         (testing "pure vs fast"
           (doseq [hex? [false true]
                   pad? [false true]]
             (testing (str "for " (if hex? "base32hex" "base32")
                           (when pad? " with padding"))
               (let [string (#'b32/encode* data hex? pad?)]
                 (is (= string (b32/encode data hex? pad?))
                     (str "encoding byte sequence "
                          (pr-str (b/byte-seq data))
                          " should produce identical strings"))
                 (is (b/bytes= (#'b32/decode* string hex?)
                               (b32/decode string hex?))
                     (str "decoding string " string
                          " should produce identical byte sequences"))))))))))
