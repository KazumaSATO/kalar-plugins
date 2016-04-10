(ns tamaki.file.file-test
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [tamaki.file.file :as file]))

(deftest copy
  (testing "Copy."
    (let [d (str (fs/temp-dir ""))]
      (#'file/copy "dev-resources/tamaki/file/html" d)
      (is (fs/exists? (fs/file (str d "/tamaki/file/html/foo.html")))))))
