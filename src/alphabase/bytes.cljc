(ns alphabase.bytes
  "Functions to generically handle byte arrays."
  (:refer-clojure :exclude [byte-array compare]))


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
  (when array
    #?(:clj (map from-byte array)
       :cljs (map #(aget array %) (range (alength array))))))


(defn bytes=
  "Returns true if two byte sequences are the same length and have the same
  byte content."
  [a b]
  (= (byte-seq a) (byte-seq b)))


(defn byte-array
  "Creates a new array to hold byte data."
  ^bytes
  [length]
  #?(:clj (clojure.core/byte-array length)
     :cljs (js/Uint8Array. (js/ArrayBuffer. length))))


(defn copy
  "Copies bytes from one array to another.

  - If only a source is given, the result is a fully copied byte array.
  - If a source and a destination with offset are given, copies all of the
    bytes from the source into the destination at that offset.
  - If all arguments are given, copies `length` bytes from the source at the
    given offset to the destination at its offset."
  ([src]
   (let [dst (byte-array (alength ^bytes src))]
     (copy src dst 0)
     dst))
  ([src dst dst-offset]
   (copy src 0 dst dst-offset (alength ^bytes src)))
  ([src src-offset dst dst-offset length]
   #?(:clj (System/arraycopy ^bytes src src-offset ^bytes dst dst-offset length)
      :cljs (dotimes [i length]
              (set-byte dst (+ i dst-offset) (get-byte src (+ i src-offset)))))))


(defn init-bytes
  "Initialize a new array with the given sequence of byte values."
  ^bytes
  [values]
  (let [length (count values)
        data (byte-array length)]
    (dotimes [i length]
      (set-byte data i (nth values i)))
    data))


(defn random-bytes
  "Returns a byte array `length` bytes long with random content."
  ^bytes
  [length]
  (let [data (byte-array length)]
    #?(:clj (.nextBytes (java.security.SecureRandom.) data)
       :cljs (dotimes [i length]
               (set-byte data i (rand-int 256))))
    data))


(defn compare
  "Lexicographically compares two byte-arrays for order. Returns a negative
  number, zero, or a positive number if `a` is less than, equal to, or greater
  than `b`, respectively.

  This ranking compares each byte in the keys in order; the first byte which
  differs determines the ordering; if the byte in `a` is less than the byte in
  `b`, `a` ranks before `b`, and vice versa.

  If the keys differ in length, and all the bytes in the shorter key match the
  longer key, the shorter key ranks first."
  [^bytes a ^bytes b]
  (let [prefix-len (min (alength a) (alength b))]
    (loop [i 0]
      (if (< i prefix-len)
        ; Compare next byte in sequence
        (let [ai (get-byte a i)
              bi (get-byte b i)]
          (if (= ai bi)
            (recur (inc i))
            (- ai bi)))
        ; Reached the end of the shorter key, compare lengths.
        (- (alength a) (alength b))))))
