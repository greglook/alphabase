alphabase
=========

[![Build Status](https://travis-ci.org/greglook/alphabase.svg?branch=develop)](https://travis-ci.org/greglook/alphabase)
[![API codox](https://img.shields.io/badge/doc-API-blue.svg)](https://greglook.github.io/alphabase/api/)

A simple cross-compiled Clojure(Script) library to handle encoding binary data
in different bases using defined alphabets. If you've ever wanted a simple way
to encode a byte array as hexadecimal or base58, this library is for you!

## Installation

Library releases are published on Clojars. To use the latest version with
Leiningen, add the following dependency to your project definition:

[![Clojars Project](http://clojars.org/mvxcvi/alphabase/latest-version.svg)](http://clojars.org/mvxcvi/alphabase)

## Usage

- `alphabase.bytes` namespace for generic byte-array handling
- `alphabase.core` with arbitrary alphabet support
- `alphabase.hex` and `alphabase.base58` with convenience wrappers

## Testing

The unit tests can be run using the following commands:

```sh
# Clojure tests
lein test

# ClojureScript tests on PhantomJS
lein doo phantom test
```

For a REPL, you can use these:

```sh
# Clojure REPL
lein repl

# ClojureScript REPL on NodeJS
rlwrap lein cljs-repl
```

## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
