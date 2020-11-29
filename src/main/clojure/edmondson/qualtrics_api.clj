(ns edmondson.qualtrics-api
  (:require [clojure.string :as string]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [edmondson.config :as cfg]
            [edmondson.utils :as u]))

(defn q-url [& args]
  (string/join "/" (cons (cfg/qualtrics-base-url) (map str args))))

(defn get-json
  ([url] (get-json url {}))
  ([url options]
   (-> url
       (client/get (cfg/deep-merge (cfg/http-options)
                                   options))
       :body)))

(defn all-surveys [] ;;TODO check for "next" link for pagination
  (-> (get-json (q-url "surveys"))
      :result
      :elements))

(defn find-survey [survey-name]
  (->> (all-surveys)
       (filter #(.equalsIgnoreCase (:name %) survey-name) )
       first))

(defn survey-details [survey]
  (-> (get-json (q-url "surveys" (:id survey)))
      :result))

(defn start-export [survey format]
  (let [response
        (client/post (q-url "surveys" (:id survey) "export-responses")
                     (cfg/http-options {:form-params {:format format
                                                      :compress false}
                                        :content-type :json}))]
    (-> response
        :body
        :result)))

(defn export-progress [survey export-response]
  (-> (q-url "surveys"
             (:id survey)
             "export-responses"
             (:progressId export-response))
      get-json
      :result))

(defn export-file-id [survey export-response]
  (let [progress (export-progress survey export-response)]
    (and (= "complete" (:status progress))
         (:fileId progress))))

(defn- export-poll-pause! []  (Thread/sleep 500))
(def ^:dynamic *export-retry-count* (* 5 60 2)) ;5 minutes with 500ms pause

(defn export-sync [survey]
  (let [export-response (start-export survey "json")]
    (loop [fileId (export-file-id survey export-response)
           retry-count 1]
      (cond fileId fileId

            (> retry-count *export-retry-count*)
            (throw (RuntimeException.
                    (str "Max number of retries for export-sync on Survey: "
                         (:id survey))))

            :else (do
                    (export-poll-pause!)
                    (recur (export-file-id survey export-response)
                           (inc retry-count)))))))

(defn survey-results-sync [survey]
  (let [export-file-id (export-sync survey)]
    (-> (q-url "surveys"
               (:id survey)
               "export-responses"
               export-file-id
               "file")
        get-json
        :responses)))


;; Helper function which creates
;; identifiers in the Qualtrics format.
;; takes as input say :QID and "42" and creates keyword :QID_42_TEXT
(defn- create-text-qid
  [qid & args] ;; :QID "42" -> :QID_42_TEXT
  (->> (concat [(name qid)] args ["TEXT"]) ;;(QUID 2 TEXT)
       (string/join "_")
       keyword))


;;maps qualtrics internal question ids
;;to the user defined and semantic question names
(defmulti index-sub-questions
  (fn [question _]
    (:type (:questionType question)))) ;; dispatch on question type
                                       ;; (see Qualtrics API)


;; Multiple choice
(defmethod index-sub-questions "MC"
  [{:keys [questionType questionName choices] :as question}
   qid]
  ;;process choices with text entry
  (reduce
   (fn [acc [choice-id choice]]
     (let [full-choice-id (create-text-qid qid (name choice-id))]
       (if (contains? choice :textEntry)
         ;; Multiple choice with text entry
         (assoc acc full-choice-id
                (create-text-qid (keyword questionName)
                                 (name choice-id)))
         acc)))
   {} choices))

;; text entry
(defmethod index-sub-questions "TE" [{:keys [questionName]} qid]
  ;;create an additional _TEXT question
  {(create-text-qid qid) (create-text-qid questionName)})

;; other
(defmethod index-sub-questions "DB" [question qid] {}) ;; skip

(defn index-survey-questions [details]
  (reduce
   (fn [acc [qid question]]
     (-> acc
       ;assoc qualtrics id with our question name: QID42 -> EFF-TEAM_PSY-SAFE-1
         (assoc qid (keyword (:questionName question)))
       ;create an merge in subquestions
         (merge (index-sub-questions question qid))))
   {}
   (:questions details)))

(defn normalize-responses
  [survey-details responses]
  (let [question-names (index-survey-questions survey-details)
        id->label #(get question-names % %)
        meta-data-keys #{:distributionChannel
                         :_recordId
                         :startDate
                         :endDate
                         :recordedDate
                         :status
                         :locationLongitude
                         :locationLatitude
                         :progress
                         :duration
                         :finished}]
    (map (fn [response]
           (-> response
               (assoc :meta-data (select-keys (:values response)
                                              meta-data-keys))

               (assoc :answers
                      ;;replaces keys of type question with question labels
                      ;; e.g. id (QID167) with labels :SM-BATCH_MVP
                      (into {}
                            (map (fn [[qid qval]]
                                   (if-let [qlabel (get (:labels response) qid)]
                                     [(id->label qid) qlabel]
                                     [(id->label qid) qval]))
                                 (:values response))))
               (dissoc :displayedFields :displayedValues :values :labels)

               (update :answers #(into {} (remove (comp meta-data-keys first) %)))))

         responses)))
