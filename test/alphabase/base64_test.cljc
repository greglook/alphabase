(ns alphabase.base64-test
  (:require
    [alphabase.base64 :as b64]
    [alphabase.bytes :as b]
    [alphabase.test-util :as t]
    [clojure.test :refer [deftest testing is]]))


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (b64/encode nil))
        "nil encodes to nil")
    (is (nil? (b64/encode (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "basic examples"
    (is (= "AA" (b64/encode (b/byte-array 1)))
        "single zero byte encodes as two zero chars"))
  (testing "RFC examples"
    (testing "with base alphabet"
      (is (= "Zg" (b64/encode t/f)))
      (is (= "Zm8" (b64/encode t/fo)))
      (is (= "Zm9v" (b64/encode t/foo)))
      (is (= "Zm9vYg" (b64/encode t/foob)))
      (is (= "Zm9vYmE" (b64/encode t/fooba)))
      (is (= "Zm9vYmFy" (b64/encode t/foobar))))
    (testing "with base alphabet and padding"
      (is (= "Zg==" (b64/encode t/f false true)))
      (is (= "Zm8=" (b64/encode t/fo false true)))
      (is (= "Zm9v" (b64/encode t/foo false true)))
      (is (= "Zm9vYg==" (b64/encode t/foob false true)))
      (is (= "Zm9vYmE=" (b64/encode t/fooba false true)))
      (is (= "Zm9vYmFy" (b64/encode t/foobar false true))))
    ;; TODO: examples where url-safety matters
    (testing "with url-safe alphabet"
      (is (= "Zg" (b64/encode t/f true)))
      (is (= "Zm8" (b64/encode t/fo true)))
      (is (= "Zm9v" (b64/encode t/foo true)))
      (is (= "Zm9vYg" (b64/encode t/foob true)))
      (is (= "Zm9vYmE" (b64/encode t/fooba true)))
      (is (= "Zm9vYmFy" (b64/encode t/foobar true))))
    (testing "with url-safe alphabet and padding"
      (is (= "Zg==" (b64/encode t/f true true)))
      (is (= "Zm8=" (b64/encode t/fo true true)))
      (is (= "Zm9v" (b64/encode t/foo true true)))
      (is (= "Zm9vYg==" (b64/encode t/foob true true)))
      (is (= "Zm9vYmE=" (b64/encode t/fooba true true)))
      (is (= "Zm9vYmFy" (b64/encode t/foobar true true))))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (b64/decode nil))
        "nil argument decodes to nil")
    (is (nil? (b64/decode ""))
        "empty string decodes to nil")
    (is (thrown? #?(:clj Exception, :cljs js/Error)
          (b64/decode "012`4abc"))))
  (testing "basic examples"
    (is (= [0x00] (b/byte-seq (b64/decode "AA")))
        "two zero chars decodes as single zero byte"))
  (testing "RFC examples"
    (is (= [0x66] (b/byte-seq (b64/decode "Zg"))))
    (is (= [0x66 0x6F] (b/byte-seq (b64/decode "Zm8"))))
    (is (= [0x66 0x6F 0x6F] (b/byte-seq (b64/decode "Zm9v"))))
    (is (= [0x66 0x6F 0x6F 0x62] (b/byte-seq (b64/decode "Zm9vYg"))))
    (is (= [0x66 0x6F 0x6F 0x62 0x61] (b/byte-seq (b64/decode "Zm9vYmE"))))
    (is (= [0x66 0x6F 0x6F 0x62 0x61 0x72] (b/byte-seq (b64/decode "Zm9vYmFy"))))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 128))
          data (b/random-bytes length)
          message (str "byte sequence " (pr-str (b/byte-seq data)) " should round-trip")]
      (testing "with regular alphabet"
        (is (b/bytes= data (b64/decode (b64/encode data)))
            message))
      (testing "with url alphabet"
        (let [msg (str message " (" (b64/encode data true) ")")]
          (is (b/bytes= data (b64/decode (b64/encode data true)))
              msg)))
      (testing "with regular alphabet and padding"
        (is (b/bytes= data (b64/decode (b64/encode data false true)))
            message))
      (testing "with url alphabet and padding"
        (is (b/bytes= data (b64/decode (b64/encode data true true)))
            message)))))
