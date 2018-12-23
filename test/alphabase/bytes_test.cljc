(ns alphabase.bytes-test
  (:require
    #?(:clj [clojure.test :refer :all]
       :cljs [cljs.test :refer-macros [deftest is are testing]])
    [alphabase.bytes :as b]))


(deftest bytes-tests
  (testing "bytes?"
    (is (not (b/bytes? nil)))
    (is (not (b/bytes? "foo")))
    (is (b/bytes? (b/byte-array 0)))
    (is (b/bytes? (b/byte-array 1))))
  (testing "bytes="
    (is (not (b/bytes= "foo" (b/init-bytes [0 1 2]))))
    (is (not (b/bytes= (b/init-bytes [0 1 2]) nil)))
    (is (not (b/bytes= (b/init-bytes [0 1 2])
                       (b/init-bytes [0 1 2 0]))))
    (is (not (b/bytes= (b/init-bytes [0 1 2])
                       (b/init-bytes [0 1 3]))))
    (is (b/bytes= (b/init-bytes [0 1 2])
                  (b/init-bytes [0 1 2])))))


(deftest array-manipulation
  (let [bs (b/byte-array 3)]
    (is (= 0 (b/get-byte bs 0)))
    (b/set-byte bs 0 64)
    (b/set-byte bs 1 128)
    (b/set-byte bs 2 255)
    (is (= 64 (b/get-byte bs 0)))
    (is (= 128 (b/get-byte bs 1)))
    (is (= 255 (b/get-byte bs 2)))))


(deftest array-copying
  (testing "full copy"
    (let [a (b/init-bytes [0 1 2 3 4])
          b (b/copy a)]
      (is (b/bytes? b))
      (is (not (identical? a b)))
      (is (b/bytes= a b))))
  (testing "full write"
    (let [a (b/init-bytes [0 1 2 3])
          b (b/init-bytes [100 110 120 130 140 150 160])]
      (is (= 4 (b/copy a b 2)))
      (is (b/bytes= (b/init-bytes [100 110 0 1 2 3 160]) b))))
  (testing "slice write"
    (let [a (b/init-bytes [1 2 3 4 5 6 7 8 9 10])
          b (b/init-bytes [100 110 120 130 140 150])]
      (is (= 3 (b/copy a 3 b 2 3)))
      (is (b/bytes= (b/init-bytes [100 110 4 5 6 150]) b)))))


(deftest array-sorting
  (is (zero? (b/compare
               (b/init-bytes [])
               (b/init-bytes []))))
  (is (zero? (b/compare
               (b/init-bytes [0 1 2])
               (b/init-bytes [0 1 2]))))
  (is (neg? (b/compare
              (b/init-bytes [0 1 2])
              (b/init-bytes [0 1 3]))))
  (is (pos? (b/compare
              (b/init-bytes [0 2 2])
              (b/init-bytes [0 1 3]))))
  (is (pos? (b/compare
              (b/init-bytes [0 1 2 0])
              (b/init-bytes [0 1 2]))))
  (is (neg? (b/compare
              (b/init-bytes [0 1 2 0])
              (b/init-bytes [0 1 2 0 0])))))

(deftest copy-slice
  (are [bs offset len]
    (b/bytes= (b/init-bytes (take len (drop offset bs)))
             (-> bs b/init-bytes (b/copy-slice offset len)))
    [1 2 3 4 5 6 7 8 9 10] 5 3
    [0 1 2 0] 0 2
    [0 1 2 0] 0 0
    [0 1 2 0] 2 1)
  (are [bs offset]
      (= (drop offset bs)
         (-> bs b/init-bytes (b/copy-slice offset) b/byte-seq))
    [1 2 3 4 5 6 7 8 9 10] 5
    [0 1 2 0] 0
    [0 1 2 0] 0
    [0 1 2 0] 2))


(deftest concat-arrs
  (are [arrs expected]
    (b/bytes= (b/init-bytes expected)
              (->> arrs (map b/init-bytes) (apply b/concat)))
    [[0 1] [2 3] [3 4]] [0 1 2 3 3 4]
    [[0 1]] [0 1]
    [[0 1] [0 1 2]] [0 1 0 1 2]
    [[]] []))
