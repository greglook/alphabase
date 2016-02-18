(ns alphabase.bytes
  "Functions to generically handle byte arrays."
  (:refer-clojure :exclude [byte-array]))


(defn byte-array
  "Creates a new array to hold byte data."
  [size]
  #?(:clj (clojure.core/byte-array size)
     :cljs (js/Uint8Array. (js/ArrayBuffer. size))))


; TODO: conversion functions to make values into Java byte range [-128, 127] vs intuitive byte range [0, 255]
