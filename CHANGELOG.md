Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added
- Added two new arities to `alphabase.bytes/copy` which simplify full cloning of
  a byte array and a full source write to an offset in dest.

### Removed
- **BREAKING:** Removed `alphabase.multi` in favor of a unified
  [`multiformats`](//github.com/greglook/clj-multiformats) codebase.

### Changed
- Removed `javax.xml.bind.DatatypeConverter` optimization for `:clj` in the hex
  namespace for Java 9+ compatibility.
  [#4](//github.com/greglook/alphabase/issues/4)

## [1.0.0] - 2017-11-04

This project has been stable for a while now, so bumping the version to 1.0.0.

### Changed
- Update various dependencies.
- Migrate to CircleCI 2.0.

## [0.2.2] - 2016-12-15

### Added
- Add `alphabase.multi` with limited [multibase](https://github.com/multiformats/multibase)
  implementation using the existing hex and base58btc support.

### Changed
- Improve performance of hex coding in Clojure using core `DataTypeConverter`
  class.

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

[Unreleased]: https://github.com/greglook/alphabase/compare/1.0.0...HEAD
[1.0.0]: https://github.com/greglook/alphabase/compare/0.2.2...1.0.0
[0.2.2]: https://github.com/greglook/alphabase/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/greglook/alphabase/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/greglook/alphabase/compare/0.1.0...0.2.0
