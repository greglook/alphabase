(ns alphabase.core
  "Core encoding and decoding functions for use with arbitrary bases."
  (:require
    [alphabase.bytes :as bytes]))


; TODO: benchmark this code
; TODO: switch to using transient collections?


(defn- bytes->tokens
 "Encodes a byte array into a sequence of alphabet tokens."
 [alphabet ^bytes data]
 (let [base (count alphabet)]
   #?(:clj ; In Clojure, use the optimized BigInt class for division.
      (loop [n (bigint (BigInteger. 1 data))
             s (list)]
        (if (< n base)
          (conj s (nth alphabet n))
          (let [r (mod n base)]
            (recur
              (/ (- n r) base)
              (conj s (nth alphabet r))))))

      :cljs ; In ClojureScript, implement native big-integer byte division.
      (->>
        (bytes/byte-seq data)
        (reduce
          (fn add-byte
            [digits value]
            (loop [digits digits
                   carry value
                   j 0]
              (if (< j (count digits))
                ; Propagate carry value across digits.
                (let [carry' (+ (bit-shift-left (nth digits j) 8) carry)]
                  (recur (assoc digits j (mod carry' base))
                         (int (/ carry' base))
                         (inc j)))
                ; Outside digits, add new for remaining carry
                (if (pos? carry)
                  (recur (conj digits (mod carry base))
                         (int (/ carry base))
                         (inc j))
                  digits))))
          [0])
        (reverse)
        (map (partial nth alphabet))))))


(defn- tokens->bytes
  "Decodes a sequence of alphabet tokens into a byte array."
  [alphabet data]
  (let [base (count alphabet)]
    #?(:clj
       (let [byte-data
             (->>
               (reverse data)
               (map vector (iterate (partial * base) 1N))
               ^clojure.lang.BigInt
               (reduce
                 (fn [n [b c]]
                   (let [v (.indexOf alphabet (str c))]
                     (when (neg? v)
                       (throw (ex-info
                                (str "Invalid character: " (pr-str c)
                                     " is not in alphabet " (pr-str alphabet)))))
                     (+ n (* (bigint v) b))))
                 0N)
               (.toBigInteger)
               (.toByteArray))]
         (if (and (> (count byte-data) 1)
                  (zero? (aget byte-data 0))
                  (neg? (aget byte-data 1)))
           (drop 1 byte-data)
           (seq byte-data)))

       :cljs
       ()
       )))


(defn encode
  "Encodes binary data using the given alphabet. Returns the encoded string, or
  nil if the input is nil or empty."
  [alphabet data]
  {:pre [(string? alphabet) (not (empty? alphabet))]}
  (when-not (empty? data)
    (let [zeroes (count (take-while zero? (bytes/byte-seq data)))]
      (apply str (concat (repeat zeroes (first alphabet))
                         (when (< zeroes (alength data))
                           (bytes->tokens alphabet data)))))))


(defn decode
  "Decodes a string of alphabet tokens. Returns the decoded binary array, or nil
  if the input is nil or empty."
  [alphabet data]
  {:pre [(string? alphabet) (not (empty? alphabet))]}
  (when-not (empty? data)
    (let [zeroes (count (take-while #{(first alphabet)} data))]
      (if (= zeroes (count data))
        (bytes/byte-array zeroes)
        (let [byte-data (tokens->bytes alphabet data)
              result (bytes/byte-array (+ zeroes (count byte-data)))]
          (dotimes [i (count byte-data)]
            (bytes/set-byte result (+ zeroes i) (nth byte-data i)))
          result)))))
