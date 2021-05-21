(ns google-sheets.psych-safety
  (:require [edmondson.google-forms :as forms]
            [edmondson.models.psychological-safety :as ps]
            [edmondson.survey-analysis :as analysis]
            [edmondson.reports :as reports]
            [edmondson.config :as cfg]
            [clojure.string :as str]))


;; Define the survey model used for this survey
;; This defines the "Constructs" (like "Psychological safety"
;; How the construct/each question is scored (e.g. 7-pt likert scale)

(def example-model
  (merge ps/psychological-safety
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
   {:credentials (cfg/environment-config cfg/credentials-json-path-env "./credentials.json")
    :token-directory "./tokens"}
   example-model))

(def psych-safety-results (get (:aggregate-results survey-results)
                               "Psychological safety"))
(def psi-stats
  (let [{:keys [mean quantile]} (get-in psych-safety-results [:construct-stats :psi])]
    {:p15 (nth quantile 1)
     :p50 (nth quantile 3)
     :p75 (nth quantile 4)
     :mean mean
     :measurement :psi}))

(def worst-15th-percentile-responses
  (analysis/filter-responses ["Psychological safety" :psi]
                             #(<= % (:p15 psi-stats))
                             (:scored-responses survey-results)))

(def worst-outlier-psych-safety-results
  (get (analysis/aggregate-scores example-model worst-15th-percentile-responses)
       "Psychological safety"))

(defn report [construct]
  (reports/print-analysis example-model construct (:scored-responses survey-results)))

(require 'rebel-readline.core
         'rebel-readline.clojure.line-reader
         'rebel-readline.clojure.service.local)

(defn -main [& args]
  (rebel-readline.core/with-readline-in
    (rebel-readline.clojure.line-reader/create
     (rebel-readline.clojure.service.local/create))
    (clojure.main/repl
     :init #(do
              (in-ns 'google-sheets.psych-safety)
              (use 'edmondson.reports)
              (use 'clojure.pprint)
              (println "Try (report X) where X is one of the following:")
              (clojure.pprint/pprint (keys ps/psychological-safety))
              (println "Or try (pprint (:aggregate-results survey-results)) to see data:")
              )
     :prompt (fn[]))))
