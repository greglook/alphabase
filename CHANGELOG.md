Change Log
==========

All notable changes to this project will be documented in this file, which
follows the conventions of [keepachangelog.com](http://keepachangelog.com/).
This project adheres to [Semantic Versioning](http://semver.org/).


## [Unreleased]

A quick follow-up to make the library is usable with babashka, which can't use
the optimized Java encoding implementations.

### Fixed
- Add reader conditionals for the `:bb` platform to use the pure-Clojure
  implementations, similar to `:cljs`.


## [3.0.182] - 2025-03-17

This is a new release of alphabase with some major changes. Most significantly,
the JVM-targeted implementation is now written in Java for performance,
resulting in several 10x-100x speedups and better memory efficiency.
Javascript-targeted code continues to be written in (mostly) pure
Clojurescript.

The second significant change is that the prior Base32 implementation was
incorrect; it was using the same radix-encoding approach as Base58, but that
produced the wrong results. The data did round-trip, but this was not compliant
with RFC 4648. **If you serialized data with the 2.x version, it will not
deserialize correctly with 3.x!** Sorry about that.

### Changed
- **Breaking:** `alphabase.core` is now `alphabase.radix` if you need customized encoding.
- **Breaking:** `alphabase.hex` is now `alphabase.base16` for consistency with the other included bases.
- **Breaking:** Hex and base32 strings now default to upper-case encoding.
- `alphabase.bytes/random-bytes` now uses a cryptographically-strong API for random values in Javascript.
- Switched to tools.build and updated CI and other development tools.
- Rewrote Clojure implementations in Java for speed and efficiency.

### Fixed
- **Breaking:** Base32 encoding now complies with RFC 4648.
- Standardized error messages across the codecs.

### Added
- Byte utilities for UTF-8 strings `b/from-string` and `b/to-string`.
- New `alphabase.base2` namespace provides binary encoding.
- New `alphabase.base8` namespace provides octal encoding.
- New `alphabase.base64` convenience namespace provides the standard library
  Base64 implementations in Java and Google Closure.


## [2.1.1] - 2021-11-07

Largely a maintenance release, with significant build/test changes.

### Added
- Base-32 decoding is now case-insensitive.

## [2.1.0] - 2019-10-11

### Added
- Basic base-32 support in the `alphabase.base32` namespace.

## [2.0.4] - 2019-03-23

### Fixed
- Remove reference to `javax.xml.bind.DatatypeConverter` in the `alphabase.hex`
  namespace for compatibility with Java 11, which moves that class to a separate
  module.

## [2.0.3] - 2019-01-20

### Fixed
- Fixed a few reflection warnings in `alphabase.bytes`.

## [2.0.2] - 2018-12-24

### Added
- `alphabase.bytes/copy-slice` returns a copy of a sequence of bytes inside a
  source array.
- `alphabase.bytes/concat` returns a byte array which is the concatenation of
  the arrays given as arguments.

## [2.0.1] - 2018-12-12

Note that this is a major release because of the removal of multibase support
from alphabase.

### Added
- `alphabase.bytes/bytes?` provides a cross-platform test for byte data.
- Added two new arities to `alphabase.bytes/copy` which simplify full cloning of
  a byte array and a full source write to an offset in dest.
- `alphabase.bytes/copy` returns the number of bytes copied in the
  multi-argument arities.
- `alphabase.bytes/compare` offers a lexicographic comparator for byte arrays.

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

[Unreleased]: https://github.com/greglook/alphabase/compare/3.0.182...HEAD
[3.0.182]: https://github.com/greglook/alphabase/compare/2.1.1...3.0.182
[2.1.1]: https://github.com/greglook/alphabase/compare/2.1.0...2.1.1
[2.1.0]: https://github.com/greglook/alphabase/compare/2.0.4...2.1.0
[2.0.4]: https://github.com/greglook/alphabase/compare/2.0.3...2.0.4
[2.0.3]: https://github.com/greglook/alphabase/compare/2.0.2...2.0.3
[2.0.2]: https://github.com/greglook/alphabase/compare/2.0.1...2.0.2
[2.0.1]: https://github.com/greglook/alphabase/compare/1.0.0...2.0.1
[1.0.0]: https://github.com/greglook/alphabase/compare/0.2.2...1.0.0
[0.2.2]: https://github.com/greglook/alphabase/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/greglook/alphabase/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/greglook/alphabase/compare/0.1.0...0.2.0
