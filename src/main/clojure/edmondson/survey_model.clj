(ns edmondson.survey-model)

(defprotocol Question
  (build-question-model [question defaults] "build question model"))

(extend-type clojure.lang.Keyword ;; question name (use default attributes)
  Question
  (build-question-model [question defaults]
    {question defaults}))

(extend-type String ;; question name (use default attributes)
  Question
  (build-question-model [question defaults]
    {question defaults}))

(extend-type clojure.lang.IPersistentVector
  ;; vector of question name + overrides
  Question
  (build-question-model [[question overrides] defaults]
    {question (merge defaults overrides)}))


(defn index-questions
  [model]
  "Takes a survey model and return an index of model questions.
This is a map of question names to the model for each question
(i.e. a map that describes scoring, scale, verbatims, group).
This makes it quick to lookup a question in the model
(e.g. to score a response)."

  (let [r (fn [acc [construct cons-attributes]]
            (let [construct-params (select-keys cons-attributes [:scoring
                                                                 :scale])
                  qs (:questions cons-attributes)
                  qs-map (apply merge
                                (map #(build-question-model % construct-params)
                                     qs))
                  vs (:verbatims cons-attributes)
                  vs-map (zipmap vs (repeat {:verbatim :verbatim}))

                  gs (:groups cons-attributes)
                  gs-map (zipmap gs (repeat {:group :group}))]
              (merge-with merge acc qs-map vs-map gs-map)))]

    (reduce r {} model)))
