alphabase
=========

[![CircleCI](https://circleci.com/gh/greglook/alphabase.svg?style=shield&circle-token=f1e11fd825b2006adde3d1316e465abda50b453d)](https://circleci.com/gh/greglook/alphabase)
[![codecov](https://codecov.io/gh/greglook/alphabase/branch/master/graph/badge.svg)](https://codecov.io/gh/greglook/alphabase)
[![cljdoc](https://cljdoc.org/badge/mvxcvi/alphabase)](https://cljdoc.org/d/mvxcvi/alphabase/CURRENT)

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
lein clj:test

# ClojureScript tests on Rhino
lein cljs:test
```

For a REPL, you can use these:

```sh
# Clojure REPL
lein repl

# ClojureScript REPL on Rhino
rlwrap lein cljs:repl
```


## License

This is free and unencumbered software released into the public domain.
See the UNLICENSE file for more information.
