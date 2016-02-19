(ns alphabase.core
  "Core encoding and decoding functions for use with arbitrary bases."
  (:require
    [alphabase.bytes :as bytes]))


(defn- bytes->tokens
 "Encodes a byte array into a sequence of alphabet tokens."
 [^String alphabet ^bytes data]
 ; Algorithms benchmarked for base58-check encoding 32 bytes of data.
 (let [base (count alphabet)]
   #?(; In Clojure, use the optimized BigInt class for division.
      ; bigint division: ~67 µs
      :clj
      (loop [n (bigint (BigInteger. 1 data))
             s (list)]
        (if (< n base)
          (conj s (nth alphabet n))
          (let [r (mod n base)]
            (recur
              (/ (- n r) base)
              (conj s (nth alphabet r))))))

      ; In ClojureScript, implement native big-integer byte division.
      ; persistent collections: ~620 µs
      ; transient collections:  ~515 µs
      :cljs
      (->>
        (bytes/byte-seq data)
        (reduce
          (fn add-byte
            [digits value]
            (loop [digits digits
                   carry value
                   i 0]
              (if (< i (count digits))
                ; Propagate carry value across digits.
                (let [carry' (+ carry (bit-shift-left (nth digits i) 8))]
                  (recur (assoc! digits i (mod carry' base))
                         (int (/ carry' base))
                         (inc i)))
                ; Outside digits, add new for remaining carry.
                (if (pos? carry)
                  (recur (conj! digits (mod carry base))
                         (int (/ carry base))
                         (inc i))
                  digits))))
          (transient [0]))
        (persistent!)
        (reverse)
        (map (partial nth alphabet))))))


(defn- tokens->bytes
  "Decodes a sequence of alphabet tokens into a sequence of byte values."
  [^String alphabet data]
  ; Algorithms benchmarked for base58-check decoding 32 bytes of data.
  (let [base (count alphabet)]
    #?(; bigint math: ~74 µs
       :clj
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

       ; persistent collections: ~255 µs
       ; transient collections:  ~200 µs
       :cljs
       (->>
         (seq data)
         (reduce
           (fn add-token
             [bytev token]
             (let [value (.indexOf alphabet (str token))]
               (when (neg? value)
                 (throw (ex-info
                          (str "Invalid token " (pr-str token)
                               " is not in base-" base " alphabet "
                               (pr-str alphabet)))))
               (loop [bytev bytev
                      carry value
                      i 0]
                 (if (< i (count bytev))
                   ; Emit bytes as we carry values forward.
                   (let [carry' (+ carry (* base (nth bytev i)))]
                     (recur (assoc! bytev i (bit-and carry' 0xff))
                            (bit-shift-right carry' 8)
                            (inc i)))
                   ; Outside bytes, add new for remaining carry.
                   (if (pos? carry)
                     (recur (conj! bytev (bit-and carry 0xff))
                            (bit-shift-right carry 8)
                            (inc i))
                     bytev)))))
           (transient [0]))
         (persistent!)
         (reverse)))))


(defn encode
  "Encodes binary data using the given alphabet. Returns the encoded string, or
  nil if the input is nil or empty."
  [alphabet ^bytes data]
  {:pre [(string? alphabet) (< 1 (count alphabet))]}
  (when-not (zero? (alength data))
    (let [zeroes (count (take-while zero? (bytes/byte-seq data)))]
      (apply str (concat (repeat zeroes (first alphabet))
                         (when (< zeroes (alength data))
                           (bytes->tokens alphabet data)))))))


(defn decode
  "Decodes a string of alphabet tokens. Returns the decoded binary array, or nil
  if the input is nil or empty."
  [alphabet tokens]
  {:pre [(string? alphabet) (not (empty? alphabet))]}
  (when-not (empty? tokens)
    (let [zeroes (count (take-while #{(first alphabet)} tokens))]
      (if (= zeroes (count tokens))
        (bytes/byte-array zeroes)
        (let [byte-seq (tokens->bytes alphabet tokens)
              data (bytes/byte-array (+ zeroes (count byte-seq)))]
          (dotimes [i (count byte-seq)]
            (bytes/set-byte data (+ zeroes i) (nth byte-seq i)))
          data)))))
