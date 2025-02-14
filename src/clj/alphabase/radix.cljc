(ns alphabase.radix
  "Radix encoding and decoding functions for use with arbitrary alphabets."
  {:clj-kondo/ignore [:unused-private-var]}
  (:require
    [alphabase.bytes :as b]
    [clojure.string :as str])
  #?(:clj
     (:import
       alphabase.codec.Radix)))


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
        zeros (count (take-while #{(first alphabet)} string))]
    (if (= zeros (count string))
      (b/byte-array zeros)
      (let [byte-vals (->>
                        (seq string)
                        (reduce
                          (fn add-digit
                            [bytev digit]
                            (let [value (str/index-of alphabet (str digit))]
                              (when (neg? value)
                                (throw (ex-info
                                         (str "Invalid digit " (pr-str digit) " is not in " *ns*
                                              " (" base ") alphabet")
                                         {:alphabet alphabet, :digit digit})))
                              (loop [bytev bytev
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
                                    bytev)))))
                          (transient [0]))
                        (persistent!)
                        (reverse))
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
    #?(:clj (Radix/encode alphabet data)
       :default (encode* alphabet data))))


(defn decode
  "Decode a byte array from a string using the provided alphabet. Returns nil
  for nil or blank strings."
  ^bytes
  [^String alphabet ^String string]
  {:pre [(string? alphabet)]}
  (when-not (str/blank? string)
    #?(:clj (Radix/decode alphabet string)
       :default (decode* alphabet string))))
