(ns tamaki-core.config-test
  (:require [clojure.test :refer :all]
            [tamaki-core.config :as c]))

(deftest config-test
  (testing "find the post files "
    (println (c/load-config {:plugins ['tamaki]}))))
