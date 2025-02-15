(ns alphabase.radix-test
  (:require
    [alphabase.bytes :as b]
    [alphabase.radix :as radix]
    [clojure.test :refer [deftest testing is]]))


(def test-alphabet
  "abcdefghij")


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (radix/encode test-alphabet nil))
        "nil encodes to nil")
    (is (nil? (radix/encode test-alphabet (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "basic examples"
    (is (= "a" (radix/encode test-alphabet (b/byte-array 1)))
        "single zero byte encodes as one zero digit")
    (is (= "abch" (radix/encode test-alphabet (b/init-bytes [0 127]))))
    (is (= "bahgfgff" (radix/encode test-alphabet (b/init-bytes [164 69 87]))))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (radix/decode test-alphabet nil))
        "nil argument decodes to nil")
    (is (nil? (radix/decode test-alphabet ""))
        "empty string decodes to nil")
    (is (thrown-with-msg? #?(:clj Exception, :cljs js/Error)
                          #"Character '@' at index 3 is not a valid digit"
          (radix/decode test-alphabet "abc@de")))
    #?(:clj
       (is (thrown-with-msg? Exception
                             #"Character '@' at index 3 is not a valid digit"
             (#'radix/decode* test-alphabet "abc@de")))))
  (testing "basic examples"
    (is (b/bytes= (b/byte-array 1) (radix/decode test-alphabet "a"))
        "zero digit decodes as single zero byte")
    (is (b/bytes= (b/byte-array 3) (#'radix/decode* test-alphabet "aaa"))
        "multiple zero digits decodes as zero bytes")
    (is (= [164 69 87] (b/byte-seq (radix/decode test-alphabet "bahgfgff"))))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 32))
          data (b/random-bytes length)]
      (testing "reflexive encoding"
        (is (b/bytes= data (radix/decode test-alphabet (radix/encode test-alphabet data)))
            (str "byte sequence "
                 (pr-str (b/byte-seq data))
                 " should round-trip")))
      #?(:clj
         (testing "pure vs fast"
           (let [string (#'radix/encode* test-alphabet data)]
             (is (= string (radix/encode test-alphabet data))
                 (str "encoding byte sequence "
                      (pr-str (b/byte-seq data))
                      " should produce identical strings"))
             (is (b/bytes= (#'radix/decode* test-alphabet string)
                           (radix/decode test-alphabet string))
                 (str "decoding string " string
                      " should produce identical byte sequences"))))))))
