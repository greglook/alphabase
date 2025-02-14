(ns alphabase.base32
  "Base32 implementation from RFC 4648, including the extended hexadecimal
  alphabet."
  (:require
    [alphabase.bytes :as b]
    [clojure.math :as math]
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
        digits-len (int (math/ceil (* 1.6 data-len)))
        padding-len (if (and pad? (not= 0 (mod digits-len 8)))
                      (- 8 (mod digits-len 8))
                      0)
        output #?(:clj (char-array (+ digits-len padding-len))
                  :cljs (make-array (+ digits-len padding-len)))]
    (loop [data-idx 0
           char-idx 0]
      (if (< data-idx data-len)
        ;; Determine next input and output chunk to encode.
        (let [chunk-len (min 5 (- data-len data-idx))
              output-len (if (= 5 chunk-len)
                           8
                           (int (math/ceil (* 1.6 chunk-len))))
              ;; Read chunk bytes and bit-pack them together. Javascript shift
              ;; operators treat numbers as only 32 bits, so use a high and low
              ;; pair instead of a single number.
              [hi lo] (loop [hi 0
                             lo 0
                             i 0]
                        (if (< i chunk-len)
                          (recur (bit-or (bit-shift-left hi 8)
                                         (bit-and (unsigned-bit-shift-right lo 24)
                                                  0xFF))
                                 (bit-or (bit-shift-left lo 8)
                                         (bit-and (b/get-byte data (+ data-idx i))
                                                  0xFF))
                                 (inc i))
                          [hi lo]))
              ;; Right-pad with zero bits to make total evenly divisible.
              [hi lo] (if (not= 5 chunk-len)
                        (let [padding-bits (- 5 (mod (* 8 chunk-len) 5))
                              mask (dec (bit-shift-left 1 padding-bits))]
                          [(bit-or (bit-shift-left hi padding-bits)
                                   (bit-and (unsigned-bit-shift-right lo (- 32 padding-bits))
                                            mask))
                           (bit-shift-left lo padding-bits)])
                        [hi lo])]
          ;; Unpack and encode digits from the numbers.
          (loop [hi hi
                 lo lo
                 offset (dec output-len)]
            (when (<= 0 offset)
              (let [digit (nth alphabet (bit-and lo 0x1F))]
                (aset output (+ char-idx offset) digit)
                (recur (unsigned-bit-shift-right hi 5)
                       (bit-or (unsigned-bit-shift-right lo 5)
                               (bit-shift-left (bit-and hi 0x1F) 27))
                       (dec offset)))))
          (recur (+ data-idx chunk-len)
                 (+ char-idx output-len)))
        ;; Done encoding, finish loop.
        (do
          ;; Write padding characters if set.
          (dotimes [i padding-len]
            (aset output (+ char-idx i) \=))
          ;; Sanity check that we wrote the expected number of characters.
          (let [char-idx (+ char-idx padding-len)]
            (when (not= (alength output) char-idx)
              (throw (ex-info (str "Expected to encode " data-len " byte array into "
                                   (alength output) " characters, but only got " char-idx)
                              {:data data
                               :output (str/join output)}))))
          (str/join output))))))


(defn- decode-pure
  "Decode a byte array from a base32 string.

  Pure Clojure implementation."
  [string hex?]
  (let [alphabet (if hex?
                   hex-alphabet
                   rfc-alphabet)
        string (str/replace (str/upper-case string) #"=+$" "")
        char-len (count string)
        data-len (int (math/floor (* 0.625 char-len)))
        data (b/byte-array data-len)]
    (loop [char-idx 0
           data-idx 0]
      (if (< char-idx char-len)
        (let [chunk-len (min 8 (- char-len char-idx))
              ;; Decode chunk digits and bit-pack them together. Javascript shift
              ;; operators treat numbers as only 32 bits, so use a high and low
              ;; pair instead of a single number.
              [hi lo] (loop [hi 0
                             lo 0
                             i 0]
                        (if (< i chunk-len)
                          (let [digit (nth string (+ char-idx i))
                                v (str/index-of alphabet digit)]
                            (when-not v
                              (throw (ex-info (str "Character '" digit "' at index "
                                                   (+ char-idx i) " is not a valid Base32 digit")
                                              {:index (+ char-idx i)
                                               :char digit})))
                            (recur (bit-or (bit-shift-left hi 5)
                                           (bit-and (unsigned-bit-shift-right lo 27)
                                                    0x1F))
                                   (bit-or (bit-shift-left lo 5)
                                           (bit-and v 0x1F))
                                   (inc i)))
                          [hi lo]))
              output-len (if (= 8 chunk-len)
                           5
                           (int (math/floor (* 0.625 chunk-len))))
              ;; Unpad extra zero bits to make total evenly divisible.
              [hi lo] (if (not= 8 chunk-len)
                        (let [padding-bits (- 5 (mod (* 8 output-len) 5))
                              mask (dec (bit-shift-left 1 padding-bits))]
                          [(unsigned-bit-shift-right hi padding-bits)
                           (bit-or (unsigned-bit-shift-right lo padding-bits)
                                   (bit-shift-left (bit-and hi mask)
                                                   (- 32 padding-bits)))])
                        [hi lo])]
          ;; Unpack bytes from the decoded numbers.
          (loop [hi hi
                 lo lo
                 offset (dec output-len)]
            (when (<= 0 offset)
              (b/set-byte data (+ data-idx offset) (bit-and lo 0xFF))
              (recur (unsigned-bit-shift-right hi 8)
                     (bit-or (unsigned-bit-shift-right lo 8)
                             (bit-shift-left (bit-and hi 0xFF) 24))
                     (dec offset))))
          (recur (+ char-idx chunk-len)
                 (+ data-idx output-len)))
        ;; Done encoding, finish loop.
        (do
          ;; Sanity check that we read the expected number of bytes.
          (when (not= data-len data-idx)
            (throw (ex-info (str "Expected to decode " char-len " digits into "
                                 data-len " bytes, but only got " data-idx)
                            {:string string
                             :output data})))
          data)))))


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
   (when (and data (pos? (alength data)))
     #?(:clj (encode-fast data hex? pad?)
        :default (encode-pure data hex? pad?)))))


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
   (when-not (str/blank? string)
     #?(:clj (decode-fast string hex?)
        :default (decode-pure string hex?)))))
