;Напишите функцию копирующую из входного канала в выходной только уникальные значения (которые ранее не встречались).
;В случае прихода из входного канала сообщения :reset - информация о “накопленных” значениях - сбрасывается.

(ns cljlab2.core
  (:require [clojure.core.async :refer [put! close! >!! <!! <! >! go chan]]
            [clojure.test :refer [deftest is testing run-tests]]
            [cljlab2.core :as core]
            [clojure.core.async :as async]))


(defn copy-unique-values [input-chan output-chan]
  (go
    (loop [seen #{}]
      (let [value (<! input-chan)]
        (if (= value :reset)
          (recur #{})
          (if (contains? seen value)
            (recur seen)
            (do
              (>! output-chan value)
              (recur (conj seen value)))))))))

(def input-chan (chan))
(def output-chan (chan))

(go
  (put! input-chan 0)
  (put! input-chan 2)
  (put! input-chan 0)
  (put! input-chan 3)
  (put! input-chan 2)
  (put! input-chan 4)
  (put! input-chan :reset)
  (put! input-chan 2)
  (put! input-chan 3)
  (put! input-chan 1)
  (close! input-chan))

(go
  (copy-unique-values input-chan output-chan))

(go
  (loop []
    (let [value (<! output-chan)]
      (when value
        (println value)
        (recur)))))