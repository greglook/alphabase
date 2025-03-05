alphabase
=========

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/greglook/alphabase/tree/main.svg?style=shield)](https://dl.circleci.com/status-badge/redirect/gh/greglook/alphabase/tree/main)
[![codecov](https://codecov.io/gh/greglook/alphabase/branch/main/graph/badge.svg)](https://codecov.io/gh/greglook/alphabase)
[![cljdoc](https://cljdoc.org/badge/mvxcvi/alphabase)](https://cljdoc.org/d/mvxcvi/alphabase/CURRENT)

A simple cross-compiled Clojure(Script) library to handle encoding binary data
in different bases using defined alphabets. If you've ever wanted a simple way
to encode a byte array as a hexadecimal string, or base32, or other bases, this
library is for you!


## Installation

Library releases are published on Clojars. To use the latest version, add the
following dependency to your project:

[![Clojars Project](http://clojars.org/mvxcvi/alphabase/latest-version.svg)](http://clojars.org/mvxcvi/alphabase)


## Usage

- `alphabase.bytes` namespace for generic byte-array handling
- `alphabase.radix` for arbitrary alphabet support
- Built-in support for octal, hexadecimal, base32, base58, and base64

```clojure
=> (require '[alphabase.bytes :as b]
            '[alphabase.base16 :as hex])
            '[alphabase.base32 :as b32]
            '[alphabase.base58 :as b58]

=> (def data (b/random-bytes 32))

=> (hex/encode data)
"333a0fc9d17e07ff9a75afca02df9ab32fdb9eb71565e810e981773bdd1e0c90"

=> (b/bytes= data (hex/decode *1))
true

=> (b32/encode data)
"MZ2B7E5C7QH76NHLL6KALPZVMZP3OPLOFLF5AIOTALXHPOR4DEQ"

=> (b/bytes= data (b32/decode *1))
true

;; base32 is case-insensitive
=> (b/bytes= data (b32/decode (clojure.string/lower-case *2)))
true

=> (b58/encode data)
"4Sy9GnemD6QbtaLtVTkZsZeZXExFmvGk7dJy1gDnBJCF"

=> (b/bytes= data (b58/decode *1))
true
```


## Testing

The unit tests can be run using the following commands:

```sh
# Clojure tests
bin/test clj

# ClojureScript tests on Node
bin/test cljs
```

For a REPL, you can use these:

```sh
# Clojure REPL
bin/repl

# ClojureScript REPL
bin/repl cljs
```


## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
