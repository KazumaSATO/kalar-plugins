(ns tamaki.text.html-test
  (:require [clojure.test :refer :all]
            [net.cgrand.enlive-html :as ehtml]
            [tamaki.text.html :as thtml])
  (:import (java.io StringReader)))

(deftest test-html-handling
  (testing "extract plain texts"
    (let [html-text (slurp "dev-resources/tamaki/text/post-body.html")
          enlive-model (ehtml/html-resource (StringReader. html-text))
          result (thtml/extract-text enlive-model)
          ]
      (is (= "RanceWorksMenu鎌倉日帰りAugust 23, 2015traveling日曜日に日帰りで鎌倉に行ってきた。 5年前に初めて鎌倉を観光して以来，江ノ島に行けなかったことと，しらす丼を食べなかったことを僕はずっと悔やんでいた。 前回の旅でやり残したことをやろう，僕はそう意気込んで出発した。鎌倉駅に着いたのはお昼前だった。 駅前や小町取りは沢山の観光客で賑わっていた。 到着早々，しらす丼を食べるために秋本という懐石料理屋に入った。僕は写真の釜揚げしらすを注文した。 僕は生のしらすを食べるつもりでいたが，台風による時化で船が出なかったらしく， 生のしらすを提供していなかった。 はじめは残念だったけれども，出された釜揚げしらすが美味しかったので十分満足できた。 丼を半分食べたところに温泉卵を入れ，残り半分を別の味で楽しんだ。その後，鶴岡八幡宮に向かって小町通りを歩いた。 小町通りには面白い店が沢山並んでいた。 中でも写真に写る丸いものを売っている店に興味をひかれた。 この丸いものはコンニャクで作られた石鹸らしい。 これを使って試しに手を洗ってみると，プルプルした手触りがして気持ちがよかった。 気にしていたコンニャクの臭みはなく色ごとに違う香りがつけられていた。鶴岡八幡宮でお参りをした後，小町通りを引き返し，鎌倉駅から江ノ電に乗って江ノ島に向かった。 台風のせいか，写真の江ノ島に向かう橋を歩くころには天気が悪くなっていた。 この後，江ノ島の橋周辺を見てまわり帰宅の途についた。Previous PageNext PageRanceWorks感想 日本の思想March 09, 2016word2vec Explained 紹介March 05, 2016Recurrent neural network based language model 紹介March 05, 2016AboutDiaryLearn MoreTwitterGithubCopyright ©Sato Kazuma. All Right Reserved."
             result)))))
