(ns edmondson.config
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clj-http.conn-mgr :as conn-mgr]
            [cheshire.core :as json])
  (:import (com.google.api.client.auth.oauth2 Credential)
           (com.google.api.client.extensions.java6.auth.oauth2
              AuthorizationCodeInstalledApp)
           (com.google.api.client.extensions.jetty.auth.oauth2
            LocalServerReceiver
            LocalServerReceiver$Builder)
           (com.google.api.services.sheets.v4 SheetsScopes)
           (com.google.api.client.googleapis.auth.oauth2
            GoogleAuthorizationCodeFlow
            GoogleAuthorizationCodeFlow$Builder)
            (com.google.api.client.googleapis.auth.oauth2 GoogleClientSecrets)
            (com.google.api.client.http.javanet NetHttpTransport)
            (com.google.api.client.json JsonFactory)
            (com.google.api.client.json.jackson2 JacksonFactory)
            (com.google.api.client.util.store FileDataStoreFactory)))

(defn- environment-config
  [name]
  (let [env (System/getenv name)]
    (if (string/blank? env)
      nil
      env)))

(def ^:dynamic *debug-http* false)
(def ^:dynamic *config-override* nil)
(def ^:dynamic *access-token* nil)
(def config-filename "EDMONDSON_CONFIG")
(def qualtrics-token "QUALTRICS_TOKEN")

(defn debug-from-env?
  []
  (let [env (System/getenv "DEBUG")]
    (if (or *debug-http* (= "1" env))
      true
      false)))

(defn- load-config-from-file
  [path]
  (cond
    (nil? path) {}

    (not (.exists (io/file path)))
    (throw (RuntimeException. (str config-filename " File: " path " set, but does not exist.")))

    :else (-> (io/file path)
              slurp
              (json/parse-string true))))

(defn config-file
  []
  (environment-config config-filename))

(defn access-token
  []
  (let [token (or *access-token*
                  (environment-config qualtrics-token))]
    (when (nil? token)
      (RuntimeException. (str "Please ensure environment variable "
                              qualtrics-token
                              " is set.")))
    token))


(defn deep-merge
  "Merges maps of similar shapes (used for default overriding config files).
  The default must have all the nested keys present."
  [default overrides]
  (letfn [(deep-merge-rec [a b]
            (if (map? a)
              (merge-with deep-merge-rec a b)
              b))]
    (reduce deep-merge-rec nil (list default overrides))))


(def default-config {})

(defn- config-override
  []
  (if *config-override*
    *config-override*
    (load-config-from-file (config-file))))

(defn config
  []
  (deep-merge default-config (config-override)))


(def http-options-base
  {:socket-timeout 30000  ;; in milliseconds
   :conn-timeout 10000
   :connection-manager (conn-mgr/make-reusable-conn-manager {})
   :accept :json
   :as :json})

(defn http-options
  ([] (http-options {}))
  ([overrides]
   (-> http-options-base
       (deep-merge {:headers {"X-API-TOKEN" (access-token)}
                    :debug (debug-from-env?)})
       (deep-merge overrides))))

(defn qualtrics-base-url
  []
  (:qualtrics-base-url (config)))

(defn google-credentials
  [transport]
  (with-open [rdr (clojure.java.io/reader "credentials.json")]
    (let [factory (. JacksonFactory getDefaultInstance)
          secrets
          (GoogleClientSecrets/load factory rdr)
          flow (.. (new GoogleAuthorizationCodeFlow$Builder
                        transport factory secrets [SheetsScopes/SPREADSHEETS_READONLY])
                   (setDataStoreFactory
                    (new FileDataStoreFactory (new java.io.File "tokens")))
                   (setAccessType "offline")
                   build)
          receiver (.. (new LocalServerReceiver$Builder)
                       (setPort 8888)
                       (build))
          ]
      (.
       (new AuthorizationCodeInstalledApp flow, receiver)
       (authorize "user")))))
