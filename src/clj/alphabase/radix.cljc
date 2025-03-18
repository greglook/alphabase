(ns alphabase.radix
  "Radix encoding and decoding functions for use with arbitrary alphabets."
  {:clj-kondo/ignore [:unused-private-var]}
  (:require
    [alphabase.bytes :as b]
    [clojure.string :as str])
  #?@(:bb
      []
      :clj
      [(:import
         alphabase.codec.Radix)]))


;; ## Pure Implementation

(defn- encode*
  "Encode a byte array into a string using the provided alphabet.

  Pure Clojure implementation."
  ^String
  [alphabet ^bytes data]
  (let [zeros (count (take-while zero? (b/byte-seq data)))
        base (count alphabet)]
    (->>
      (when (< zeros (alength data))
        (->>
          (b/byte-seq data)
          (reduce
            (fn add-byte
              [digits value]
              (loop [digits digits
                     carry value
                     i 0]
                (if (< i (count digits))
                  ;; Propagate carry value across digits.
                  (let [carry' (+ carry (bit-shift-left (nth digits i) 8))]
                    (recur (assoc! digits i (mod carry' base))
                           (int (/ carry' base))
                           (inc i)))
                  ;; Outside digits, add new for remaining carry.
                  (if (pos? carry)
                    (recur (conj! digits (mod carry base))
                           (int (/ carry base))
                           (inc i))
                    digits))))
            (transient [0]))
          (persistent!)
          (reverse)
          (map (partial nth alphabet))))
      (concat (repeat zeros (first alphabet)))
      (apply str))))


(defn- decode*
  "Decode a byte array from a string using the provided alphabet.

  Pure Clojure implementation."
  ^String
  [alphabet string]
  (let [base (count alphabet)
        zeros (count (take-while #{(first alphabet)} string))
        str-len (count string)]
    (if (= zeros str-len)
      (b/byte-array zeros)
      (let [byte-vals (loop [bytev (transient [0])
                             char-idx 0]
                        (if (< char-idx str-len)
                          ;; Multiply next digit into number.
                          (let [digit (nth string char-idx)
                                value (or (str/index-of alphabet (str digit))
                                          (throw (ex-info
                                                   (str "Character '" digit "' at index "
                                                        char-idx " is not a valid digit")
                                                   {:alphabet alphabet
                                                    :digit digit})))
                                bytev (loop [bytev bytev
                                             carry value
                                             i 0]
                                        (if (< i (count bytev))
                                          ;; Emit bytes as we carry values forward.
                                          (let [carry' (+ carry (* base (nth bytev i)))]
                                            (recur (assoc! bytev i (bit-and carry' 0xff))
                                                   (bit-shift-right carry' 8)
                                                   (inc i)))
                                          ;; Outside bytes, add new for remaining carry.
                                          (if (pos? carry)
                                            (recur (conj! bytev (bit-and carry 0xff))
                                                   (bit-shift-right carry 8)
                                                   (inc i))
                                            bytev)))]
                            (recur bytev (inc char-idx)))
                          ;; Done decoding
                          (reverse (persistent! bytev))))
            data (b/byte-array (+ zeros (count byte-vals)))]
        (dotimes [i (count byte-vals)]
          (b/set-byte data (+ zeros i) (nth byte-vals i)))
        data))))


;; ## General Interface

(defn encode
  "Encode a byte array into a string using the provided alphabet. Returns nil
  for nil or empty data."
  ^String
  [^String alphabet ^bytes data]
  {:pre [(string? alphabet)]}
  (when (and data (pos? (alength data)))
    #?(:bb (encode* alphabet data)
       :clj (Radix/encode alphabet data)
       :default (encode* alphabet data))))


(defn decode
  "Decode a byte array from a string using the provided alphabet. Returns nil
  for nil or blank strings."
  ^bytes
  [^String alphabet ^String string]
  {:pre [(string? alphabet)]}
  (when-not (str/blank? string)
    #?(:bb (decode* alphabet string)
       :clj (Radix/decode alphabet string)
       :default (decode* alphabet string))))
