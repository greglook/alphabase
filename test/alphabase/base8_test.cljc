(ns alphabase.base8-test
  (:require
    [alphabase.base8 :as b8]
    [alphabase.bytes :as b]
    [alphabase.test-util :refer [str->bytes]]
    [clojure.test :refer [deftest testing is]]))


(deftest encoding-test
  (testing "edge cases"
    (is (nil? (b8/encode nil))
        "nil encodes to nil")
    (is (nil? (b8/encode (b/byte-array 0)))
        "empty byte array encodes to nil"))
  (testing "basic examples"
    (is (= "0" (b8/encode (b/init-bytes [0])))
        "single zero byte encodes as three zero chars")
    (is (= "0177" (b8/encode (b/init-bytes [0 127]))))
    (is (= "1043126154533472162302661513646244031273145344745643206455631620441"
           (b8/encode (str->bytes "Decentralize everything!!"))))))


(deftest decoding-test
  (testing "edge cases"
    (is (nil? (b8/decode nil))
        "nil argument decodes to nil")
    (is (nil? (b8/decode ""))
        "empty string decodes to nil")
    (is (thrown-with-msg? #?(:clj Exception, :cljs js/Error)
                          #"Character '@' at index 3 is not a valid digit"
          (b8/decode "012@4"))))
  (testing "basic examples"
    (is (b/bytes= (b/init-bytes [0]) (b8/decode "0"))
        "zero decodes as single zero byte")
    (is (b/bytes= (b/init-bytes [0 127])
                  (b8/decode "0177")))
    (is (b/bytes= (str->bytes "Decentralize everything!!")
                  (b8/decode "1043126154533472162302661513646244031273145344745643206455631620441")))))


(deftest round-trips
  (dotimes [_ 100]
    (let [length (inc (rand-int 32))
          data (b/random-bytes length)]
      (testing "reflexive encoding"
        (is (b/bytes= data (b8/decode (b8/encode data)))
            (str "byte sequence "
                 (pr-str (b/byte-seq data))
                 " should round-trip"))))))
