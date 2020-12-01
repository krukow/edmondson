(ns google-sheets.psych-safety
  (:require [edmondson.google-api :as api]
            [edmondson.survey-analysis :as analysis]
            [edmondson.survey-model :as model]
            [edmondson.reports :as reports]
            rebel-readline.core
            rebel-readline.clojure.line-reader
            rebel-readline.clojure.service.local
            [clojure.string :as str]))

;; Example of usage.
;; Given https://docs.google.com/spreadsheets/d/1QkBeMNGfsHHga85c-UsLAwnpmz7QyhvFK_n31CzDe7c/edit?usp=sharing
;; Define:

(def spreadsheetId "1QkBeMNGfsHHga85c-UsLAwnpmz7QyhvFK_n31CzDe7c")
(def tab-name "Form Responses 1")

(def agree-disagree-7-pt-params ;; default scoring for 7pt Likert scale
  {"Strongly agree" 7
   "Agree" 6
   "Somewhat agree" 5
   "Neutral" 4
   "Somewhat disagree" 3
   "Disagree" 2
   "Strongly Disagree" 1})

(defn default-scale "identity scoring transformation" [a _] a)

(defn negative-scale
  "Scoring transform for reverse-scored likert questions"
  [a likert-scale]
  (- (inc (count likert-scale)) a))


;; Define the survey model used for this survey
;; This defines the "Constructs" (like "Psychological safety"
;; How the construct/each question is scored (e.g. 7-pt likert scale)

(def example-model
  {
   "Psychological safety"
   {:scoring agree-disagree-7-pt-params ;; default scoring with 7p likert scale
    :scale default-scale ;; default scale is normal scale
    :questions [["I worry that mistakes will be held against me."
                 ;; override default scale: this questions is scored in reverse
                 ;; since Strongly agree is quite bad here.
                 {:scale negative-scale}]
                "I am able to bring up problems and tough issues." ;; defaults
                ["People in the team sometimes reject others for being different."
                 {:scale negative-scale}]
                "It is safe to take a risk within the team."
                ["I find it difficult to ask other members of the team for help."
                 {:scale negative-scale}]
                "No one in the team would deliberately act in a way that undermines my efforts."
                "My unique skills and talents are valued and utilized in this team."]}

   "Generative culture"
   {:scoring agree-disagree-7-pt-params
    :scale default-scale
    :questions ["No-one is punished for delivering news of failure or other bad news."
                "Responsibilities are shared (you hear mostly \"this is our responsibility\" vs \"this is not my responsibility\")"
                "Cross functional and cross-team collaboration is encouraged and rewarded."
                "People on our team welcome new ideas, regardless of source and seniority."
                "Failure causes inquiry (failures are investigated, not ignored or hidden)"
                "Failures are treated primarily as opportunities to improve the system, processes or team."
                "New information is actively sought out."]}

   "Psychological safety domains"
   {:groups ["For you personally, choose the three topics which you find the most uncomfortable, or where you'd least want to bring up your concerns or have a conversation about."]}
   ;; This allows us to group responses based on how people check
   ;; boxes in this question. Also makes it easier to count which
   ;; boxes get the most checks.

   "Open-ended feedback"
   ;; These are not analyzed quantitatively
   {:verbatims ["If you could change one team process, or one thing about how the team works, what would you change and why?"
                "What is something the team does really well? Something that would make you very disappointed if the team stopped doing or something that makes you proud to be part of the team."]}

   })


(def model-index
  (model/index-questions example-model))

;; Ratio taken from Psy. safty measurement report (Amy Edmondson)
;; A psy safety score above 40 (of max 49) is considered very good.
(def very-good-threshold (double (/ 40 49)))


(def spreadsheet-range (str tab-name "!B:R")) ;; this is where the answers are
(def survey-results (api/eval-range {:token-directory "tokens"}
                                    spreadsheetId
                                    spreadsheet-range))
(def survey-questions (first survey-results)) ;; first row of sheet are the questions
(def survey-answers (rest survey-results)) ;; first row of sheet are the questions


(def normalized-responses
  (api/normalize-responses survey-questions survey-answers))

(def scored-responses (analysis/score-responses
                       model-index
                       normalized-responses))

(def aggregate-results
  (analysis/aggregate-scores example-model scored-responses))

(defn report [construct]
  (reports/print-analysis example-model construct scored-responses))

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
              (clojure.pprint/pprint (keys example-model)))
     :prompt (fn[]))))



(comment
  (->> (drop 1 (map :groups scored-responses))
       (map #(get % "For you personally, choose the three topics which you find the most uncomfortable, or where you'd least want to bring up your concerns or have a conversation about."))
       (map #(str/split % #","))
       (map frequencies)
       (apply (partial merge-with +))
       (sort-by second)
       reverse)

  )

 ;(clojure.pprint/pprint scored-survey-results

;; (def scored-responses-managers (group-by (comp :DEMO_MANAGER :groups)
;;                                          scored-responses))

;; (def grouped-scored-responses (group-by (comp :groups) scored-responses))


;; (comment (clojure.pprint/pprint (get (analysis/aggregate-scores (get scored-responses-managers "No"))
;;                                      "Psychological Safety")))


;; (clojure.pprint/pprint scored-survey-results)
