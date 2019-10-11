(ns alphabase.test-runner
  (:require
    [alphabase.base32-test]
    [alphabase.base58-test]
    [alphabase.bytes-test]
    [alphabase.hex-test]
    [doo.runner])
  (:require-macros
    [doo.runner :refer [doo-tests]]))


(doo-tests
  'alphabase.bytes-test
  'alphabase.base32-test
  'alphabase.base58-test
  'alphabase.hex-test)
