(ns edmondson.html
  (:require [edmondson.reports :as reports]
            [edmondson.survey-analysis :as analysis]))


(defn render-question [[qid qstats] model-index]
  (let [mean (:score-mean qstats)
        stddev (:score-stddev qstats)
        cl (int (Math/ceil mean))
        cl-answer (analysis/score->answer model-index qid cl)
        fl (int (Math/floor mean))
        fl-answer (analysis/score->answer model-index qid fl)]
    [:li "\"" qid "\""
     [:ul
      [:li "Mean score: " (reports/fmt-number mean) " (stddev: " (reports/fmt-number stddev) ")."]
      [:li "which is between \"" fl-answer "/" fl "\" and \"" cl-answer "/" cl "\"."]]]))

(defn render-question-with-distribution [[qid qstats] model-index]
  (let [[li q1 li-body q2
         ul] (render-question [qid qstats] model-index)
        dist (:score-distribution qstats)]
    [li q1 li-body q2
     (conj ul
           [:li (str "Distribution: " (sort dist))])]))

(defn render-questions [qs model-index]
  [:ul (map #(render-question % model-index) qs)])

(defn render-questions-with-distribution
  [qs model-index]
  [:ul (map #(render-question-with-distribution % model-index) qs)])
