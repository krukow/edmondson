(ns edmondson.qualtrics-api
  (:require [clojure.string :as string]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [edmondson.config :as cfg]))

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
