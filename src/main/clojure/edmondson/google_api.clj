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

(defn eval-range [sheet range]
  (let [transport (. GoogleNetHttpTransport newTrustedTransport)
        creds (cfg/google-credentials transport)
        factory (. JacksonFactory getDefaultInstance)
        service  (.. (new Sheets$Builder transport factory creds)
                     (setApplicationName "survey-scorer")
                     build)
        response (.. service (spreadsheets)
                     (values)
                     (get sheet range)
                     execute)]
    (seq (.getValues response))))
