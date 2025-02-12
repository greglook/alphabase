(ns alphabase.base32
  "Base32 implementation from RFC 4648, including the extended hexadecimal
  alphabet."
  (:require
    [alphabase.bytes :as b]
    [alphabase.core :as abc]
    [clojure.string :as str])
  #?(:clj
     (:import
       alphabase.codec.Base32)))


(def ^:const rfc-alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567")
(def ^:const hex-alphabet "0123456789ABCDEFGHIJKLMNOPQRSTUV")


;; ## Pure Implementation

(defn- encode-pure
  "Encode a byte array into a base32 string.

  Pure Clojure implementation."
  ^String
  [^bytes data hex? pad?]
  (let [alphabet (if hex?
                   hex-alphabet
                   rfc-alphabet)
        data-len (alength data)
        padding (rem data-len 5)]
    (loop [groups []
           offset 0]
      (if (< offset data-len)
        ;; Read in 40 bits as 5 octets, write 8 characters.
        (let [input-bytes (min 5 (- data-len offset))
              output-chars (cond-> (int (/ (* input-bytes 8) 5))
                             (pos? (rem (* input-bytes 8) 5))
                             (inc))
              [b0 b1 b2 b3 b4 :as bs] (map #(or (b/get-byte data (+ offset %)) 0)
                                    (range 0 input-bytes))
              bits [;; top 5 bits of byte 0
                    (when b0
                      (bit-and (bit-shift-right b0 3) 0x1F))
                    ;; bottom 3 bits of byte 0 + top 2 bits of byte 1
                    (when b1
                      (bit-or (bit-and (bit-shift-left  b0 2) 0x1C)
                              (bit-and (bit-shift-right b1 6) 0x03)))
                    ;; middle 5 bits of byte 1
                    (when b2
                      (bit-and (bit-shift-right b1 1) 0x1F))
                    ;; bottom 1 bit of byte 1 + top 4 bits of byte 2
                    (when b2
                      (bit-or (bit-and (bit-shift-left  b1 4) 0x10)
                              (bit-and (bit-shift-right b2 4) 0x0F)))
                    (when b3
                      ;; bottom 4 bits of byte 2 + top 1 bit of byte 3
                      (bit-or (bit-and (bit-shift-left  b2 1) 0x1E)
                              (bit-and (bit-shift-right b3 7) 0x01)))
                    (when b3
                      ;; middle 5 bits of byte 3
                      (bit-and (bit-shift-right b3 2) 0x1F))
                    (when b4
                      ;; bottom 2 bits of byte 3 + top 3 bits of byte 4
                      (bit-or (bit-and (bit-shift-left  b3 3) 0x18)
                              (bit-and (bit-shift-right b4 5) 0x07)))
                    ;; bottom 5 bits of byte 4
                    (when b4
                      (bit-and b4 0x1F))]
              s (->>
                  bits
                  (take-while some?)
                  (map #(nth alphabet %))
                  (take output-chars)
                  (apply str))]
          (recur (conj groups s) (+ offset 5)))
        ;; Apply padding to final result.
        (cond-> (apply str groups)
          pad?
          (str (case (int padding)
                 4 "="
                 3 "==="
                 2 "===="
                 1 "======"
                 nil)))))))


(defn- decode-pure
  "Decode a byte array from a base32 string.

  Pure Clojure implementation."
  [string hex?]
  (let [alphabet (if hex?
                   hex-alphabet
                   rfc-alphabet)
        char->n (into {} (map vector (seq alphabet) (range)))
        input (str/replace (str/upper-case string) #"=+$" "")
        length (let [l (* 5 (int (/ (count input) 8)))]
                 (case (rem (count input) 8)
                   0 (+ l 0)
                   2 (+ l 1)
                   4 (+ l 2)
                   5 (+ l 3)
                   7 (+ l 4)))
        buffer (b/byte-array length)]
    (loop [char-offset 0
           byte-offset 0]
      (when (< char-offset (count string))
        ;; Read in 40 bits as 8 characters, write 5 octets.
        (let [input-chars (min 8 (- (count input) char-offset))
              output-bytes (case input-chars
                             2 1
                             4 2
                             5 3
                             7 4
                             8 5)
              [c0 c1 c2 c3 c4 c5 c6 c7]
              (concat (map #(char->n (nth input (+ char-offset %)))
                           (range input-chars))
                      (repeat (- 8 input-chars) 0))
              bs
              [;; 5 bits of c0 + top 3 bits of c1
               (bit-or (bit-and 0xF8 (bit-shift-left  c0 3))
                       (bit-and 0x07 (bit-shift-right c1 2)))
               ;; bottom 2 bits of c1 + 5 bits of c2 + top 1 bit of c3
               (bit-or (bit-and 0xC0 (bit-shift-left  c1 6))
                       (bit-and 0x3E (bit-shift-left  c2 1))
                       (bit-and 0x01 (bit-shift-right c3 4)))
               ;; bottom 4 bits of c3 + top 4 bits of c4
               (bit-or (bit-and 0xF0 (bit-shift-left  c3 4))
                       (bit-and 0x0F (bit-shift-right c4 1)))
               ;; bottom 1 bits of c4 + 5 bits of c5 + top 2 bit of c6
               (bit-or (bit-and 0x80 (bit-shift-left  c4 7))
                       (bit-and 0x7C (bit-shift-left  c5 2))
                       (bit-and 0x03 (bit-shift-right c6 3)))
               ;; bottom 3 bits of c6 + 5 bits of c7
               (bit-or (bit-and 0xE0 (bit-shift-left c6 5))
                       (bit-and 0x1F c7))]]
          (dotimes [i output-bytes]
            (b/set-byte buffer (+ byte-offset i) (nth bs i))))
        (recur (+ char-offset 8) (+ byte-offset 5))))
    buffer))


;; ## Fast Implementation

#?(:clj
   (defn- encode-fast
     "Encode a byte array into a base32 string.

     Optimized Java implementation."
     ^String
     [^bytes data hex? pad?]
     (Base32/encode data (boolean hex?) (boolean pad?))))


#?(:clj
   (defn- decode-fast
     "Decode a byte array from a base32 string.

      Optimized Java implementation."
     ^String
     [^bytes data hex?]
     (Base32/decode data (boolean hex?))))


;; ## General API

(defn encode
  "Encode a byte array into a base32 string.

   If `hex?` is true, this uses the extended hex alphabet instead of the RFC
   4648 alphabet. If `pad?` is true, the result will be extended with `=`
  characters to be an even multiple of eight digits in length."
  (^String
   [^bytes data]
   (encode data false false))
  (^String
   [^bytes data hex?]
   (encode data hex? false))
  (^String
   [^bytes data hex? pad?]
   #?(:clj (encode-fast data hex? pad?)
      :default (encode-pure data hex? pad?))))


(defn decode
  "Decode a byte array from a base32 string. Handles both upper and lower case
  characters, as well as trailing padding `=` characters.

  If `hex?` is true, this uses the extended hex alphabet instead of the RFC
  4648 alphabet."
  (^bytes
   [string]
   (decode string false))
  (^bytes
   [string hex?]
   #?(:clj (decode-fast string hex?)
      :default (decode-pure string hex?))))
