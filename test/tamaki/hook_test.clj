(ns tamaki.hook-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.hook :as h]
            [me.raynes.fs :as fs]))

(deftest test-hook
  (testing "clean"
    (let [dir (fs/temp-dir "temp")]
      (h/clean {:build (.getAbsolutePath dir)})
      (is (not (fs/exists? dir)))))
  (testing "initialize"
    (let [created (.getAbsolutePath (io/file (fs/temp-dir "temp") "init"))]
      (h/initialize {:build created})
      (is (fs/exists? created))))
  (testing "process-assets"
    (let [temp-build (fs/absolute (fs/temp-dir "temp"))]
      (h/process-assets {:build temp-build :assets ["dev-resources/tamaki/hook/assets"
                                                    "dev-resources/tamaki/hook/asset.txt"]})
      (is (= #{(fs/file temp-build)
               (fs/file temp-build "asset.txt")
               (fs/file temp-build "asset1.html")
               (fs/file temp-build "css/asset.css")
               (fs/file temp-build "css")}
             (set (file-seq (fs/file temp-build)))))
      )
    )
  )
