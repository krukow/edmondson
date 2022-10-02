(ns edmondson.reports
  (:require [edmondson.survey-analysis :as analysis]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))

(defn fmt-number [x]
  (when x
    (pprint/cl-format nil "~,1f" x)))

(defn question-text [question-name] question-name)

(defn print-result [res]
  (let [lookup-questions
        (fn [qs & keyargs]
          (let [q-texts (map question-text qs)
                scores (map (get-in res [:question-stats
                                         :question-stats]) qs)
                lines (map (fn [qid qtxt score]
                             (str (name qid) ": "
                                  qtxt
                                  " (" (select-keys score keyargs) ")"))
                           qs q-texts scores)]
            lines))]

    (println "Mean score: " (fmt-number (get-in res [:construct-stats :avg :mean])))
    (println "Score stddev: " (fmt-number (get-in res [:construct-stats :avg :stddev])))

    (println)
    (println "Worst responses scored")
    (println (map (comp fmt-number :mean-score)
                  (take 5 (get-in res [:response-stats :worst-scores]))))

    (println)
    (println "Best responses scored")
    (println (map (comp fmt-number :mean-score)
                  (take 5 (get-in res [:response-stats :best-scores]))))

    (println)
    (println "Response score stddev")
    (println (fmt-number (get-in res [:response-stats :response-score-stddev])))

    (println)
    (println "Worst scoring questions")
    (let [qs (take 3 (get-in res [:question-stats :worst-questions]))
          lines (lookup-questions qs :score-mean)]
      (println (clojure.string/join "\n" lines)))

    (println)
    (println "Best scoring questions")
    (let [qs (take 3 (get-in res [:question-stats :best-questions]))
          lines (lookup-questions qs :score-mean)]
      (println (clojure.string/join "\n" lines)))

    (println)


    (println "Varying questions")
    (let [qs (take 3 (get-in res [:question-stats :varying-questions]))
          lines (lookup-questions qs :score-mean :score-stddev)]
      (println (clojure.string/join "\n" lines)))


    (println)

    (println "Stable questions")
    (let [qs (take 3 (get-in res [:question-stats :stable-questions]))
          lines (lookup-questions qs :score-mean :score-stddev)]
      (println (clojure.string/join "\n" lines)))))


(defn take-outliers
  [n target scored-construct-results]
  (let [worst-scores (get-in scored-construct-results [:response-stats target])
        worst-score-ids  (map (comp :responseId meta) worst-scores)]
    (take n worst-score-ids)))


(defn print-analysis
  [survey-model construct scored-responses]
  (let [overall-scores  (analysis/aggregate-scores survey-model scored-responses)
        construct-overall-scores (get overall-scores construct)
        worst-outlier-responses (set (take-outliers 5 :worst-scores
                                                    construct-overall-scores))
        best-outlier-responses (set (take-outliers 5 :best-scores
                                                   construct-overall-scores))
        worst-n-responses (filter #(worst-outlier-responses (:responseId %))
                                  scored-responses)
        best-n-responses (filter #(best-outlier-responses (:responseId %))
                                 scored-responses)

        worst-outlier-scores  (analysis/aggregate-scores
                               survey-model
                               worst-n-responses)
        best-outlier-scores  (analysis/aggregate-scores
                              survey-model
                              best-n-responses)
                ]
    (println construct)
    (println)
    (println "Overall scores")
    (println)
    (print-result construct-overall-scores)

    (println)
    (println "Worst 5 scores")
    (println)
    (print-result (get worst-outlier-scores construct))
    (println)
    (println "Best 5 scores")
    (println)
    (print-result (get best-outlier-scores construct))
    (println)


    ))
