(ns alphabase.hex
  "Functions to encode and decode bytes as hexadecimal."
  {:clj-kondo/ignore [:unused-namespace]}
  (:require
    [alphabase.bytes :as bytes]
    [clojure.string :as str]))


(defn byte->hex
  "Converts a single byte value to a two-character hex string."
  [value]
  (let [hex #?(:clj (Integer/toHexString value)
               :cljs (.toString value 16))]
    (if (= 1 (count hex))
      (str "0" hex)
      hex)))


(defn hex->byte
  "Converts a two-character hex string into a byte value."
  [hex]
  #?(:clj (Integer/parseInt hex 16)
     :cljs (js/parseInt hex 16)))


#?(:clj
   (let [alphabet (.toCharArray "0123456789abcdef")]
     (defn encode
       "Converts a byte array into a lowercase hexadecimal string. Returns nil for
       empty inputs."
       ^String
       [^bytes data]
       (when (and data (pos? (alength data)))
         (let [chrs (char-array (* 2 (alength data)))]
           (dotimes [i (alength data)]
             (let [b (bytes/get-byte data i)
                   high (bit-and (bit-shift-right b 4) 0x0F)
                   low (bit-and b 0x0F)
                   pos (* 2 i)]
               (aset-char chrs pos ^char (aget alphabet high))
               (aset-char chrs (inc pos) ^char (aget alphabet low))))
           (String. chrs)))))

   :default
   (defn encode
     "Converts a byte array into a lowercase hexadecimal string. Returns nil for
     empty inputs."
     [data]
     (when (and data (pos? (alength data)))
       (->> (bytes/byte-seq data)
            (map byte->hex)
            (str/join)
            (str/lower-case)))))


(defn decode
  "Parses a hexadecimal string into a byte array. Ensures that the resulting
  array is zero-padded to match the hex string length."
  ^bytes
  [^String data]
  (when-not (empty? data)
    (let [length (/ (count data) 2)
          array (bytes/byte-array length)]
      (dotimes [i length]
        (let [hex (subs data (* 2 i) (* 2 (inc i)))]
          (bytes/set-byte array i (hex->byte hex))))
      array)))


(defn validate
  "Checks a string to determine whether it's well-formed hexadecimal. Returns
  an error string if the argument is invalid."
  ^String
  [value]
  (cond
    (not (string? value))
    (str "Value is not a string: " (pr-str value))

    (not (re-matches #"^[0-9a-fA-F]*$" value))
    (str "String '" value "' is not valid hex: "
         "contains illegal characters")

    (< (count value) 2)
    "Hex string must contain at least one byte"

    (odd? (count value))
    (str "String '" value "' is not valid hex: "
         "number of characters (" (count value) ") is odd")

    :else nil))


(defn valid?
  "Returns true if the string is valid hexadecimal."
  [value]
  (nil? (validate value)))
