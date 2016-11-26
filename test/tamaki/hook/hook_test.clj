(ns tamaki.hook.hook-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.hook.hook :as h]
            [me.raynes.fs :as fs]))

(deftest test-hook
  (testing "clean"
    (let [dir (fs/temp-dir "temp")]
      (h/clean {:build (.getAbsolutePath dir)})
      (is (not (fs/exists? dir)))))
  (testing "initialize"
    (let [created (.getAbsolutePath (io/file (fs/temp-dir "temp") "init"))]
      (h/initialize {:build created})
      (is (fs/exists? created)))))
