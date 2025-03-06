(ns alphabase.base8
  "Octal (base8) implementation."
  (:require
    [alphabase.radix :as radix]))


(def ^:const alphabet "01234567")


(defn encode
  "Encode a byte array into an octal (base8) string. Returns nil for nil or
  empty data."
  ^String
  [^bytes data]
  (radix/encode alphabet data))


(defn decode
  "Decode a byte array from an octal (base8) string. Returns nil for nil or
  blank strings."
  ^bytes
  [string]
  (radix/decode alphabet string))
