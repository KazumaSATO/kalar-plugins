(ns tamaki.file.copy-test
    (:require [clojure.test :refer :all]
            [tamaki.file.copy :as copy]
            [tamaki-core.file :as file]
              ))

(deftest test-copy
  (testing "Copy."
    (file/clean-dest)
    (let [result (copy/copy)]
      (is (= nil result)))))
