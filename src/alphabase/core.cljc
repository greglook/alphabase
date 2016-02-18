(ns alphabase.core
  "Core encoding and decoding functions for use with arbitrary bases."
  (:require
    [alphabase.bytes :as bytes]))


(defn- add-byte
  "Adds a single byte value to a vector of digits. Returns the updated digit
  vector."
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


(defn encode
  "Encode binary data using the given alphabet. Returns the encoded string, or
  nil if the input is nil or empty."
  [alphabet data]
  (when-not (and (string? alphabet) (not (empty? alphabet)))
    (throw (ex-info "Expected alphabet to be a non-empty string, got: "
                    (pr-str alphabet))))
  (when-not (empty? data)
    (let [base (count alphabet)
          bytes (bytes/byte-seq data)
          digits (reduce add-byte [0] bytes)
          zeroes (count (take-while zero? bytes))]
      (->> (reverse digits)
           (concat (repeat zeroes 0))
           (map (partial get alphabet))
           (apply str)))))
