(ns tamaki-core.file-test
  (:require [clojure.test :refer :all]
            [tamaki-core.file :as file]
            [tamaki-core.config :as config]))

(deftest test-file-utilities
  (testing "Create an empty file."
    (let [filename  (str (:dest (config/read-config)) "/empty.txt")]
      (file/clean-dest)
      (is (file/create-empty-file filename)))))

