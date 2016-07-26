Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

...

## [0.2.1] - 2016-07-25

### Fixed

- Minor bug when decoding a string with invalid characters in Clojure.

## [0.2.0] - 2016-03-07

### Added

- Add cljs tests using `doo`.
- `bytes/copy` provides a way to copy bytes from one array to another.
- `bytes/init-bytes` to initialize an array with a seq of values.

### Fixed

- `bytes/byte-seq` returns nil for nil inputs.
- `bigint-` functions are only emitted for `:clj`.
- Pure division and multiplication functions are used as a default, rather than
  just with `:cljs`.

## 0.1.0 - 2016-02-19

Initial project release.

[Unreleased]: https://github.com/greglook/alphabase/compare/0.2.1...HEAD
[0.2.1]: https://github.com/greglook/alphabase/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/greglook/alphabase/compare/0.1.0...0.2.0
