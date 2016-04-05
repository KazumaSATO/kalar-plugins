(ns tamaki.css.sass-test
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [clojure.string :as string]
            [tamaki.css.sass :as sass]
            [clojure.java.io :as io]))

(deftest test-sass
  (testing "Compiles a sass file."
    (let [output (-> ""  fs/temp-file .getAbsolutePath)]
      (sass/compile-sass "dev-resources/tamaki/css/sass/test.sass" output)
      (let [m (string/replace (slurp output) #"(?m)[\s\n\r]" "")]
        (is (re-seq #"body\{font:100%Helvetica,sans-serif;color:#333;}" m)))))
  (testing "Is sass compiler available?"
    (is (some? (sass/sass-available?)))))
