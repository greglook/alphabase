(ns alphabase.multi
  "Multibase spec implementation."
  (:require
    [alphabase.base58 :as base58]
    [alphabase.hex :as hex]))


(def base-codes
  "Map of base keys to the prefix character code for that base. If multiple
  codes map to a base, the value is a sequence with the canonical code first."
  {:base1        \1       ; unary tends to be 11111
   :base2        \0       ; binary has 1 and 0
   :base8        \7       ; highest char in octal
   :base10       \9       ; highest char in decimal
   :base16       [\f \F]  ; highest char in hex
   :base32       [\u \U]  ; rfc4648 - highest letter
   :base32hex    [\v \V]  ; rfc4648 - highest char
   :base58flickr \Z       ; highest char
   :base58btc    \z       ; highest char
   :base64       \y       ; rfc4648 highest char
   :base64url    \Y})     ; rfc4648 highest char


(defn- base->code
  "Looks up the character code prefix to use with the given base key. Returns
  the prefix character, or nil if not found."
  [base-key]
  (when-let [prefix (base-codes base-key)]
    (if (sequential? prefix)
      (first prefix)
      prefix)))


(defn- code->base
  "Looks up the base key corresponding to a prefix code. Returns the base key,
  or nil if not found."
  [prefix]
  (first (keep (fn [[base code]]
                 (when (if (sequential? code)
                         (some #{prefix} code)
                         (= prefix code))
                   base))
               base-codes)))


#?(:clj (do (alter-var-root #'base->code memoize)
            (alter-var-root #'code->base memoize)))


(defn encoded-base
  "Returns the base key the given string is encoded in, if any."
  [tokens]
  (code->base (first tokens)))


(defn encode
  "Converts a byte array into a multibase-encoded string."
  ^String
  [base-key data]
  (some->>
    (case base-key
      :base16 (hex/encode data)
      :base58btc (base58/encode data)
      (throw (ex-info (str "Base " base-key " is not a supported encoding")
                      {:type ::unsupported-base, :base base-key})))
    (str (base->code base-key))))


(defn decode
  "Decodes a multibase-encoded string into a byte array."
  ^bytes
  [tokens]
  (when-not (empty? tokens)
    (if-let [base-key (encoded-base tokens)]
      (case base-key
        :base16 (hex/decode (subs tokens 1))
        :base58btc (base58/decode (subs tokens 1))
        (throw (ex-info (str "Base " base-key " is not a supported encoding")
                        {:type ::unsupported-base, :base base-key})))
      (throw (ex-info (str "Prefix " (pr-str (first tokens)) " does not map to a known base")
                      {:type ::unknown-prefix, :tokens tokens})))))
