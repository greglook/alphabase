(ns alphabase.base2
  "Binary (base2) implementation."
  (:require
    [alphabase.bytes :as b]
    [clojure.string :as str]))


(defn- byte->octet
  "Convert a byte value to a binary octet string."
  [x]
  (str
    (if (bit-test x 7) "1" "0")
    (if (bit-test x 6) "1" "0")
    (if (bit-test x 5) "1" "0")
    (if (bit-test x 4) "1" "0")
    (if (bit-test x 3) "1" "0")
    (if (bit-test x 2) "1" "0")
    (if (bit-test x 1) "1" "0")
    (if (bit-test x 0) "1" "0")))


(defn- octet->byte
  "Convert a binary octet string to a byte value."
  [octet]
  (bit-or
    (if (= \1 (nth octet 0)) 0x80 0x00)
    (if (= \1 (nth octet 1)) 0x40 0x00)
    (if (= \1 (nth octet 2)) 0x20 0x00)
    (if (= \1 (nth octet 3)) 0x10 0x00)
    (if (= \1 (nth octet 4)) 0x08 0x00)
    (if (= \1 (nth octet 5)) 0x04 0x00)
    (if (= \1 (nth octet 6)) 0x02 0x00)
    (if (= \1 (nth octet 7)) 0x01 0x00)))


(defn- zero-pad
  "Pad the given binary string with zeroes so its length is a multiple of 8."
  [string]
  (let [extra (rem (count string) 8)]
    (if (zero? extra)
      string
      (str (str/join (repeat (- 8 extra) "0"))
           string))))


;; ## General Interface

(defn encode
  "Encode a byte array into a binary (base2) string. Returns nil for nil or
  empty data."
  ^String
  [^bytes data]
  (when (and data (pos? (alength data)))
    (str/join (map byte->octet (b/byte-seq data)))))


(defn decode
  "Decode a byte array from a binary (base2) string. Returns nil for nil or
  blank strings."
  ^bytes
  [string]
  (when-not (str/blank? string)
    (when-let [bad-prefix (first (re-seq #"^[01]*[^01]" string))]
      (let [bad-char (last bad-prefix)
            bad-pos (dec (count bad-prefix))]
        (throw (ex-info (str "Character '" bad-char "' at index "
                             bad-pos " is not a valid binary digit")
                        {:char bad-char
                         :idx bad-pos}))))
    (let [digits (zero-pad string)
          buffer (b/byte-array (int (/ (count digits) 8)))]
      (dotimes [i (alength buffer)]
        (let [octet (subs digits (* i 8) (* (inc i) 8))]
          (b/set-byte buffer i (octet->byte octet))))
      buffer)))
