(ns alphabase.base64
  "Base64 encoding as defined by RFC 4648."
  (:require
    [clojure.string :as str]
    #?(:cljs [goog.crypt.base64 :as gcb64]))
  #?(:clj
     (:import
       java.util.Base64)))


(defn encode
  "Encode a byte array into a base64 string. Returns nil for nil or empty
  data."
  (^String
   [^bytes data]
   (encode data false false))
  (^String
   [^bytes data url?]
   (encode data url? false))
  (^String
   [^bytes data url? pad?]
   (when (and data (pos? (alength data)))
     #?(:clj
        (let [encoder (if url?
                        (Base64/getUrlEncoder)
                        (Base64/getEncoder))
              encoder (if pad?
                        encoder
                        (.withoutPadding encoder))]
          (.encodeToString encoder data))

        :cljs
        (gcb64/encodeByteArray
          data
          (if-not url?
            (if pad?
              (.-DEFAULT gcb64/Alphabet)
              (.-NO_PADDING gcb64/Alphabet))
            (if pad?
              (.-WEBSAFE gcb64/Alphabet)
              (.-WEBSAFE_NO_PADDING gcb64/Alphabet))))))))


(defn decode
  "Decode a byte array from a base64 string. Returns nil for nil or blank
  strings."
  ^bytes
  [^String string]
  (when-not (str/blank? string)
    #?(:clj (.decode (Base64/getDecoder) (str/escape string {\- \+, \_ \/}))
       :cljs (gcb64/decodeStringToUint8Array string))))
