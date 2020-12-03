(ns edmondson.google-api
  (:require [edmondson.config :as cfg])
  (:import
   (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
   (com.google.api.client.json JsonFactory)
   (com.google.api.client.json.jackson2 JacksonFactory)
   (com.google.api.services.sheets.v4 Sheets
                                      Sheets$Builder
                                      SheetsScopes)
           (com.google.api.services.sheets.v4.model ValueRange)))

(defn generate-token
  [google-config]
    (let [transport (. GoogleNetHttpTransport newTrustedTransport)
          creds (cfg/google-credentials transport
                                        (str (:token-directory google-config)))
          factory (. JacksonFactory getDefaultInstance)]
      (println "Token stored in: " (:token-directory google-config))))

(defn eval-range [google-config sheet range]
  (let [transport (. GoogleNetHttpTransport newTrustedTransport)
        creds (cfg/google-credentials transport
                                      (str (:token-directory google-config)))
        factory (. JacksonFactory getDefaultInstance)
        service  (.. (new Sheets$Builder transport factory creds)
                     (setApplicationName "krukow/edmondson")
                     build)
        response (.. service (spreadsheets)
                     (values)
                     (get sheet range)
                     execute)]
    (seq (.getValues response))))


(defn normalize-responses
  [survey-questions responses]
  (map-indexed (fn [n response]
                 {:responseId n
                  :meta-data {}
                  :answers (zipmap survey-questions response)})
               responses))
