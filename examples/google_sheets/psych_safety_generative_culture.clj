(ns google-sheets.psych-safety-generative-culture
  (:require [edmondson.google-forms :as forms]
            [edmondson.models.psychological-safety :as ps]
            [edmondson.models.generative-culture :as gc]
            [clojure.string :as str]))


;; Define the survey model used for this survey
;; This defines the "Constructs" (like "Psychological safety"
;; How the construct/each question is scored (e.g. 7-pt likert scale)

(def example-model
  (merge ps/psychological-safety
         gc/generative-culture
         {"Psychological safety domains"
          {:groups ["Which three topics would you find most uncomfortable to have a conversation about?"]}
          ;; This allows us to group responses based on how people check
          ;; boxes in this question. Also makes it easier to count which
          ;; boxes get the most checks.

          "Open-ended feedback"
          ;; These are not analyzed quantitatively
          {:verbatims ["If you could change one behavior, one process, or just one thing about your team's work environment  -  what would you change and why?"
                       "What is one thing about your team that you really like and don't want to change, and why?"]}

          }))


(def survey-results
  (forms/configure-survey-results
   {:credentials "./credentials.json"
    :token-directory "./tokens"}
   example-model))


(def psi-stats
  (let [{:keys [mean quantile]} (get-in psych-safety-results [:construct-stats :psi])]
    {:p15 (nth quantile 1)
     :p50 (nth quantile 3)
     :p75 (nth quantile 4)
     :mean mean
     :measurement :psi
     }
    ))


(def ps-question-stats (get psych-safety-results :question-stats))
(defn ps-question->score [q] (get-in ps-question-stats [:question-stats q]))
(def model-index (model/index-questions psych-safety-model))

(def worst-3-questions
  (->> (get ps-question-stats :worst-questions)
       (take 3)
       (map #(assoc (ps-question->score %) :id %))))

(def psych-safety-response-stats (get psych-safety-results :response-stats))

(->> (get-in psych-safety-results [:response-stats :worst-scores])
     (take 10)
     (map #(:psi (:measures %))))

(def p15-psi (nth (get-in psych-safety-results [:construct-stats :psi :quantile]) 1))

(def worst-outlier-responses (take-while #(<= (get-in % [:measures :psi]) p15-psi)
                                         (get-in psych-safety-results [:response-stats :worst-scores])))
(def worst-outlier-response-ids (set (map (comp :responseId meta) worst-outlier-responses)))

(def worst-outlier-scored-responses (filter #(worst-outlier-response-ids (:responseId %)) scored-responses))

(def worst-outlier-psych-safety-results
  (get (analysis/aggregate-scores psych-safety-model worst-outlier-scored-responses)
       "Psychological safety"))

(def worst-ps-question-stats (get worst-outlier-psych-safety-results :question-stats))
(defn worst-ps-question->score [q] (get-in worst-ps-question-stats [:question-stats q]))

(def worst-outliers-worst-3-questions
  (->> (get worst-ps-question-stats :worst-questions)
       (take 3)
       (map #(assoc (worst-ps-question->score %) :id %))))

(def worst-outliers-best-3-questions
  (->> (get worst-ps-question-stats :best-questions)
       (take 3)
       (map #(assoc (worst-ps-question->score %) :id %))))


;; generative culture

(def aggregate-results
  (analysis/aggregate-scores psych-safety-model scored-responses))

(def gen-culture-results (get aggregate-results "Generative culture"))
(def mean-gen-culture (get-in gen-culture-results [:construct-stats :psi :mean]))

(def gci-stats
  (let [{:keys [mean quantile]} (get-in gen-culture-results [:construct-stats :psi])]
    {:p15 (nth quantile 1)
     :p50 (nth quantile 3)
     :p75 (nth quantile 4)
     :mean mean
     :measurement :gci
     }
    ))


(def gc-question-stats (get gen-culture-results :question-stats))
(defn gc-question->score [q] (get-in gc-question-stats [:question-stats q]))

(defn score->answer
  [score qid]
  (let [q-scores (:scoring (get model-index qid))
        scale (:scale (get model-index qid))
        scaled-score (scale score q-scores)]
    (some (fn [[answer qscore]]
            (when (= qscore scaled-score) answer))
          q-scores)))

(def worst-3-questions
  (->> (get gc-question-stats :worst-questions)
       (take 3)
       (map #(assoc (gc-question->score %) :id %))))

(def best-3-questions
  (->> (get gc-question-stats :best-questions)
       (take 3)
       (map #(assoc (gc-question->score %) :id %))))

(def gc-response-stats (get gen-culture-results :response-stats))

(def p15-gci (nth (get-in gen-culture-results [:construct-stats :psi :quantile]) 1))

(def worst-outlier-responses (take-while #(<= (get-in % [:measures :psi]) p15-gci)
                                         (get-in gen-culture-results [:response-stats :worst-scores])))
(def worst-outlier-response-ids (set (map (comp :responseId meta) worst-outlier-responses)))

(def worst-outlier-scored-responses (filter #(worst-outlier-response-ids (:responseId %)) scored-responses))

(def worst-outlier-gen-culture-results
  (get (analysis/aggregate-scores psych-safety-model worst-outlier-scored-responses)
       "Generative culture"))

(def worst-gc-question-stats (get worst-outlier-gen-culture-results  :question-stats))
(defn worst-gc-question->score [q] (get-in worst-gc-question-stats [:question-stats q]))

(def worst-outliers-worst-3-questions
  (->> (get worst-gc-question-stats :worst-questions)
       (take 3)
       (map #(assoc (worst-gc-question->score %) :id %))))

(def worst-outliers-best-3-questions
  (->> (get worst-gc-question-stats :best-questions)
       (take 3)
       (map #(assoc (worst-gc-question->score %) :id %))))

(def domains-map   {"Giving or receiving critical feedback" :feedback
                    "Asking potentially risky/newbie questions" :questions
                    "Disagreeing and voicing alternative opinions in team meetings or with specific people" :meetings
                    "Reevaluating our team processes and/or beliefs" :processes
                    "Rebalancing the workload (delegating or asking for support)" :workload
                    "Discussing career conversations and my future aspirations" :career
                    "Understanding promotions (or why one person was promoted over another)" :promos
                    "Raising \"taboo\" topics (i.e. not up for discussion). You may specify the topic(s) under \"Other\"." :taboo})

(def domains-inverse-map (zipmap (vals domains-map) (keys domains-map)))

(def domains-question "Which three topics would you find most uncomfortable to have a conversation about?")

(defn parse-domains [answer]
    (->> domains-map
         (map (fn [[a k]]
                  [k (.indexOf answer a)])) ;; find index of each possible answer
         (filter (fn [[k index]] (>= index 0)))
         (map first)))


(def domains-answers-counts
    (->> scored-responses ;; all responses
        (map #(get-in % [:groups domains-question])) ;; get the responses to the domain-question
        (remove nil?)
        (map parse-domains) ;; [ (:a :b :c) (:a :b :d) (:d :e :f) ]
        (map frequencies) ;; [{:a 1 :b 1 :c 1} {:a 1 :b 1 :d 1} {:d 1 :e 1 :f 1}]
        (apply (partial merge-with +));; {:a 2 :b 2 :c 1 :d 1 :e 1 :f 1}
        (sort-by second) ;; ([:f 1] [:e 1] [:d 1] [:c 1] [:b 2] [:a 2])
        reverse))

(def open-ended-verbatims (:verbatims (get aggregate-results "Open-ended feedback")))

(def could-be-improved (get open-ended-verbatims "If you could change one behavior, one process, or just one thing about your team's work environment  -  what would you change and why?"))

(def working-well (get open-ended-verbatims "What is one thing about your team that you really like and don't want to change, and why?"))
