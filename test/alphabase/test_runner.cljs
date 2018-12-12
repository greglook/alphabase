(ns alphabase.test-runner
  (:require-macros
    [doo.runner :refer [doo-tests]])
  (:require
    alphabase.base58-test
    alphabase.bytes-test
    alphabase.hex-test
    doo.runner))


(doo-tests
  'alphabase.bytes-test
  'alphabase.base58-test
  'alphabase.hex-test)
