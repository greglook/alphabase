(ns alphabase.base58
  "Native base58-check encoding implementation."
  (:require
    [alphabase.core :as abc]))


(def ^:const alphabet "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz")


(defn encode
  "Converts a byte array into a base58-check string."
  ^String
  [data]
  (abc/encode alphabet data))


(defn decode
  "Decodes a base58-check string into a byte array."
  ^bytes
  [data]
  (abc/decode alphabet data))
