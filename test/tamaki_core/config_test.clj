(ns tamaki-core.config-test
  (:require [clojure.test :refer :all]
            [tamaki-core.config :as c]))

(deftest config-test
  (testing "test loading configuration files"
    (let [config (c/load-config {:plugins ['tamaki]
                                 :hooks {:clean ['foo/bar]}})]
      (is (= ['tamaki.hook/clean 'foo/bar] (-> config :hooks :clean))))))
