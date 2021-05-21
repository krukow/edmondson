(ns edmondson.google-api
  (:require [edmondson.config :as cfg]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint])
  (:import
   (com.google.api.client.googleapis.javanet GoogleNetHttpTransport)
   com.google.api.client.http.FileContent
   (com.google.api.client.json.gson GsonFactory)
   com.google.api.client.util.store.FileDataStoreFactory
   (com.google.api.services.drive Drive Drive$Builder)
   (com.google.api.services.drive.model File FileList)
   (com.google.api.services.sheets.v4 Sheets
                                      Sheets$Builder)
   (com.google.api.services.sheets.v4.model ValueRange)))

(defn generate-token
  [google-config]
    (let [transport (. GoogleNetHttpTransport newTrustedTransport)
          creds (cfg/google-credentials transport
                                        (str (:token-directory google-config)))
          factory (. GsonFactory getDefaultInstance)]
      (println "Token stored in: " (:token-directory google-config))))

(defn eval-range [google-config sheet range]
  (let [transport (. GoogleNetHttpTransport newTrustedTransport)
        creds (cfg/google-credentials transport
                                      (str (:token-directory google-config)))
        factory (. GsonFactory getDefaultInstance)
        service  (.. (new Sheets$Builder transport factory creds)
                     (setApplicationName "krukow/edmondson")
                     build)
        response (.. service (spreadsheets)
                     (values)
                     (get sheet range)
                     execute)]
    (seq (.getValues response))))

(defn- drive-service [google-config]
  (let [transport (. GoogleNetHttpTransport newTrustedTransport)
        creds (cfg/google-credentials transport
                                      (str (:token-directory google-config)))
        factory (. GsonFactory getDefaultInstance)
        service  (.. (new Drive$Builder transport factory creds)
                     (setApplicationName "krukow/edmondson")
                     build)]
    service))

(defn ls-files [google-config]
  (let [service (drive-service google-config)
        response (.. service
                     files
                     list
                     (setPageSize (int 100))
                     (setFields "nextPageToken, files(id, name)")
                     execute)]
    (seq (.getFiles response))))

(defn upload-to-folder [google-config
                        {:keys [folder-id target-filename]}
                        {:keys [content-type file]}]
  (let [service (drive-service google-config)
        drive-file-metadata (doto (File.)
                                (.setName target-filename)
                                (.setParents [folder-id]))
        file-content (FileContent. content-type file)]
    (.. service
        files
        (create drive-file-metadata file-content)
        execute)))

(defn upload [{:keys [token-directory folder-url file-path]}]
  (when-not (re-seq #"/folders/" folder-url)
    (println "Folder url is invalid: it must have form: https://drive.google.com/drive/u/0/folders/[folderid] - instead it was: " folder-url)
    (System/exit 1))
  (when (clojure.string/blank? file-path)
    (println "File path is blank: " file-path)
    (System/exit 1))
  (when-not (.exists (io/file file-path))
    (println "File does not exist: " file-path)
    (System/exit 1))
  (let [url-path (.getPath (java.net.URL. folder-url))
        folder-id (last (clojure.string/split url-path  #"/folders/"))
        file (io/file file-path)
        content-type (java.nio.file.Files/probeContentType (.toPath file))
        extension (last (clojure.string/split file-path #"[\\.]"))
        target-filename (str "survey-results-" (java.time.ZonedDateTime/now) "." extension)]
    (pprint/pprint
     (upload-to-folder {:token-directory token-directory}
                       {:folder-id folder-id :target-filename target-filename}
                       {:file file :content-type content-type}))

    ))
