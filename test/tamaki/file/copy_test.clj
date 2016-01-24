(ns tamaki.file.copy-test
    (:require [clojure.test :refer :all]
            [tamaki.file.copy :as copy]))

(deftest test-copy
  (testing "Copy."
    (copy/copy)))
