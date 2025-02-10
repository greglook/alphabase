(ns alphabase.base32
  "Base32-check encoding implementation."
  (:require
    [alphabase.core :as abc]
    [clojure.string :as str])
  #?(:clj
     (:import
       alphabase.codec.Base32)))


(def ^:const alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567")


(defn encode
  "Converts a byte array into a base32-check string."
  ^String
  [data]
  (abc/encode alphabet data))


(defn decode
  "Decodes a base32-check string into a byte array."
  ^bytes
  [tokens]
  (abc/decode alphabet (str/upper-case tokens)))
