(ns alphabase.base16
  "Functions to encode and decode bytes as hexadecimal."
  {:clj-kondo/ignore [:unused-private-var]}
  (:require
    [alphabase.bytes :as b]
    [clojure.string :as str])
  #?@(:bb
      []
      :clj
      [(:import
         alphabase.codec.Base16)]))


;; ## Utilities

(defn byte->hex
  "Converts a single byte value to a two-character hex string."
  [value]
  {:pre [(number? value) (<= 0 value 255)]}
  (let [hex (str/upper-case
              #?(:clj (Integer/toHexString value)
                 :cljs (.toString value 16)))]
    (if (= 1 (count hex))
      (str "0" hex)
      hex)))


(defn hex->byte
  "Converts a two-character hex string into a byte value."
  [hex]
  {:pre [(string? hex) (<= (count hex) 2)]}
  #?(:clj (Integer/parseInt hex 16)
     :cljs (js/parseInt hex 16)))


;; ## Pure Implementation

(defn- encode*
  "Encode a byte array into a hexadecimal string.

  Pure Clojure implementation."
  ^String
  [^bytes data]
  (let [data-len (alength data)
        output #?(:clj (object-array data-len)
                  :cljs (make-array data-len))]
    (dotimes [i data-len]
      (aset output i (byte->hex (b/get-byte data i))))
    (str/join output)))


(defn- decode*
  "Decode a byte array from a hexadecimal string.

  Pure Clojure implementation."
  ^bytes
  [string]
  {:pre [(even? (count string))]}
  (let [length (/ (count string) 2)
        data (b/byte-array length)]
    (dotimes [i length]
      (let [idx (* 2 i)
            hex (subs string idx (+ idx 2))]
        (when-not (re-matches #"[0-9a-fA-F]+" hex)
          (throw (ex-info (str "Characters '" hex "' at index " idx
                               " are not valid hexadecimal digits")
                          {:string string
                           :idx idx})))
        (b/set-byte data i (hex->byte hex))))
    data))


;; ## General Interface

(defn encode
  "Encode a byte array into a hexadecimal string. Returns nil for nil or empty
  data."
  ^String
  [^bytes data]
  (when (and data (pos? (alength data)))
    #?(:bb (encode* data)
       :clj (Base16/encode data)
       :default (encode* data))))


(defn decode
  "Decode a byte array from a hexadecimal string. Handles both upper and lower case
  characters. Returns nil for nil or blank strings."
  ^bytes
  [^String string]
  (when-not (str/blank? string)
    #?(:bb (decode* string)
       :clj (Base16/decode string)
       :default (decode* string))))
