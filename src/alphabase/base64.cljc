(ns alphabase.base64
  "Base64 encoding implementation."
  (:require
    [alphabase.core :as abc]))


; FIXME: This won't quite work, since it doesn't handle padding the same way
; these base64 does with trailing '=' characters.


(def ^:const alphabet "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/")


(defn encode
  "Converts a byte array into a base64 string."
  ^String
  [data]
  (throw (ex-info "Not Yet Implemented"))
  (abc/encode alphabet data))


(defn decode
  "Decodes a base64 string into a byte array."
  ^bytes
  [tokens]
  (throw (ex-info "Not Yet Implemented"))
  (abc/decode alphabet tokens))
