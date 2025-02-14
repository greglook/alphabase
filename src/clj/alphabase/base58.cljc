(ns alphabase.base58
  "Base58-check encoding implementation."
  (:require
    [alphabase.radix :as radix]))


(def ^:const alphabet "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz")


(defn encode
  "Encode a byte array into a base58-check string. Returns nil for nil or empty
  data."
  ^String
  [data]
  (radix/encode alphabet data))


(defn decode
  "Decode a byte array from a base58-check string. Returns nil for nil or blank
  strings."
  ^bytes
  [string]
  (radix/decode alphabet string))
