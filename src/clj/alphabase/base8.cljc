(ns alphabase.base8
  "Octal (base8) implementation."
  (:require
    [alphabase.bytes :as b]
    [clojure.math :as math]
    [clojure.string :as str]))


(def ^:const alphabet "01234567")


;; ## Pure Implementation

(defn- encode*
  "Encode a byte array into a base8 string.

  Pure Clojure implementation."
  ^String
  [^bytes data]
  (let [data-len (alength data)
        digits-len (int (math/ceil (* (/ 8 3) data-len)))
        output #?(:clj (char-array digits-len)
                  :cljs (make-array digits-len))]
    (loop [data-idx 0
           char-idx 0]
      (if (< data-idx data-len)
        ;; Determine next input and output chunk to encode.
        (let [chunk-len (min 3 (- data-len data-idx))
              output-len (if (= 3 chunk-len)
                           8
                           (int (math/ceil (* (/ 8 3) chunk-len))))
              ;; Read chunk bytes and bit-pack them together.
              n (loop [n 0
                       i 0]
                  (if (< i chunk-len)
                    (recur (bit-or (bit-shift-left n 8)
                                   (bit-and (b/get-byte data (+ data-idx i))
                                            0xFF))
                           (inc i))
                    n))
              ;; Right-pad with zero bits to make total evenly divisible.
              n (if (not= 3 chunk-len)
                  (let [padding-bits (- 3 (mod (* 8 chunk-len) 3))]
                    (bit-shift-left n padding-bits))
                  n)]
          ;; Unpack and encode digits from the numbers.
          (loop [n n
                 offset (dec output-len)]
            (when (<= 0 offset)
              (let [digit (nth alphabet (bit-and n 0x07))]
                (aset output (+ char-idx offset) (char digit))
                (recur (unsigned-bit-shift-right n 3)
                       (dec offset)))))
          (recur (long (+ data-idx chunk-len))
                 (long (+ char-idx output-len))))
        ;; Done encoding, finish loop.
        (do
          ;; Sanity check that we wrote the expected number of characters.
          (assert (= (alength output) char-idx)
                  (str "Expected to encode " data-len " byte array into "
                       (alength output) " characters, but only got " char-idx))
          (str/join output))))))


(defn- decode*
  "Decode a byte array from a base8 string.

  Pure Clojure implementation."
  [string]
  (let [char-len (count string)
        data-len (int (math/floor (* (/ 3 8) char-len)))
        data (b/byte-array data-len)]
    (loop [char-idx 0
           data-idx 0]
      (if (< char-idx char-len)
        (let [chunk-len (min 8 (- char-len char-idx))
              ;; Decode chunk digits and bit-pack them together.
              n (loop [n 0
                       i 0]
                  (if (< i chunk-len)
                    (let [digit (nth string (+ char-idx i))
                          v (str/index-of alphabet digit)]
                      (when-not v
                        (throw (ex-info (str "Character '" digit "' at index "
                                             (+ char-idx i) " is not a valid octal digit")
                                        {:index (+ char-idx i)
                                         :char digit})))
                      (recur (bit-or (bit-shift-left n 3)
                                     (bit-and v 0x07))
                             (inc i)))
                    n))
              output-len (if (= 8 chunk-len)
                           3
                           (int (math/floor (* (/ 3 8) chunk-len))))
              ;; Unpad extra zero bits to make total evenly divisible.
              n (if (not= 8 chunk-len)
                  (let [padding-bits (- 3 (mod (* 8 output-len) 3))]
                    (unsigned-bit-shift-right n padding-bits))
                  n)]
          ;; Unpack bytes from the decoded numbers.
          (loop [n n
                 offset (dec output-len)]
            (when (<= 0 offset)
              (b/set-byte data (+ data-idx offset) (bit-and n 0xFF))
              (recur (unsigned-bit-shift-right n 8)
                     (dec offset))))
          (recur (long (+ char-idx chunk-len))
                 (long (+ data-idx output-len))))
        ;; Done encoding, finish loop.
        (do
          ;; Sanity check that we read the expected number of bytes.
          (assert (= data-len data-idx)
                  (str "Expected to decode " char-len " digits into "
                       data-len " bytes, but only got " data-idx))
          data)))))


;; ## General Interface

(defn encode
  "Encode a byte array into an octal (base8) string. Returns nil for nil or
  empty data."
  ^String
  [^bytes data]
  (when (and data (pos? (alength data)))
    (encode* data)))


(defn decode
  "Decode a byte array from an octal (base8) string. Returns nil for nil or
  blank strings."
  ^bytes
  [string]
  (when-not (str/blank? string)
    (decode* string)))
