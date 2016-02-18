(ns alphabase.bytes
  "Functions to generically handle byte arrays."
  (:refer-clojure :exclude [byte-array]))


(defn byte-array
  "Creates a new array to hold byte data."
  [size]
  #?(:clj (clojure.core/byte-array size)
     :cljs (js/Uint8Array. (js/ArrayBuffer. size))))


(defn to-byte
  "Coerces a number to a byte value."
  [x]
  #?(:clj (if (< 127 x) (- x 256) x)
     :cljs x))


(defn from-byte
  "Coerces a byte value to a number."
  [x]
  #?(:clj (if (neg? x) (+ 256 x) x)
     :cljs x))


(defn get-byte
  "Reads a byte value out of an array and coerces it to a number."
  [^bytes array i]
  (from-byte (aget array i)))


(defn set-byte
  "Sets a byte value in an array after coercing it from a number."
  [^bytes array i x]
  (aset array i (byte (to-byte x))))


(defn byte-seq
  "Return a sequence of the bytes in an array, after coercion."
  [array]
  #?(:clj (map from-byte array)
     :cljs (map #(aget array %) (range (alength array)))))
