(ns edmondson.google-forms
  (:require [edmondson.config :as cfg]
            [edmondson.google-api :as api]
            [edmondson.survey-analysis :as analysis]
            [edmondson.survey-model :as model]))

(defn normalize-responses
  [survey-questions responses]
  (map-indexed (fn [n response]
                 {:responseId n
                  :meta-data {}
                  :answers (zipmap survey-questions response)})
               responses))

(def default-results-url
  "https://docs.google.com/spreadsheets/d/1S_p5d9YrPg1_sawbhhTRNPJPfvnRweFmC4-OxvYhzao/edit?resourcekey#gid=1308791289")

(defn configure-survey-results
  [google-config survey-model]
  (binding [cfg/*credentials-json-path-override*
            (or (:credentials google-config)
                (cfg/environment-config cfg/credentials-json-path-env "credentials.json"))]
    (let [total-team-size (Integer/parseInt
                           (cfg/environment-config "NUM_PARTICIPANTS" "25")
                           10)
          team-name (cfg/environment-config "TEAM_NAME" "Spidercats")
          spreadsheetUrl (cfg/environment-config  "RESULTS_URL" default-results-url)
          spreadsheetId
          (let [url-path (.getPath (java.net.URL. spreadsheetUrl))
                id-action-segment (last (clojure.string/split url-path  #"/spreadsheets/d/"))]
            ;; "1QkBeMNGfsHHga85c-UsLAwnpmz7QyhvFK_n31CzDe7c/edit"
            (first (clojure.string/split id-action-segment #"/")))

          tab-name (cfg/environment-config "TAB_NAME" "Form Responses 1")
          spreadsheet-range (cfg/environment-config "SPREADSHEET_RANGE"
                                (str tab-name "!B:Z")) ;; where the answers are
          survey-results (api/eval-range google-config
                                         spreadsheetId
                                         spreadsheet-range)
          questions (first survey-results)
          answers (rest survey-results)
          num-responses (count answers)
          scored-responses (analysis/score-responses
                            survey-model
                            (normalize-responses questions answers))
          model-index (model/index-questions survey-model)
          ]
      {:scored-responses scored-responses
       :aggregate-results (analysis/aggregate-scores survey-model
                                                     scored-responses)
       :model-index model-index
       :num-responses num-responses
       :participation-rate (/ num-responses (float total-team-size))
       :team-name team-name
       :team-size total-team-size})))
