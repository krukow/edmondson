(ns edmondson.survey-analysis
  (:require [clojure.string :as string]
            [edmondson.survey-model :as model]
            [edmondson.config :as cfg]
            [edmondson.utils :as u]
            [kixi.stats.core :refer [mean variance standard-deviation histogram]]
            [kixi.stats.distribution :refer [quantile]]))

(defn calculate-quantiles
  [nums & {:keys [probs]}]
  (let [distribution (transduce identity histogram nums)]
    (map #(quantile distribution %) probs)))

(defn score-answers
  [model-index {answers :answers :as normalized-response}]
  (let [question-prefix (fn [qname];; :DEMO_FUNCTION_6_TEXT -> :DEMO_FUNCTION
                          (->> (string/split (name  qname) #"_")
                               (take 2)
                               (string/join "_")
                               keyword))
        score-answer
        (fn [acc [question-name answer]]
          (if-let [q-score-model (get model-index question-name)]
            (let [scale-fn (get q-score-model :scale)
                  score-map (get q-score-model :scoring)
                  scored-answer (get score-map answer :unscored)
                  excludes (into #{} (get q-score-model :exclude []))]
              (cond (contains? excludes answer)
                    (assoc acc question-name :excluded)

                    (= scored-answer :unscored)
                    (assoc acc question-name :unscored)
                    ;; (throw (new RuntimeException
                    ;;             (str "Answer " answer " to " question-name " is not supported.")))


                    :else
                    (assoc acc question-name
                           (scale-fn scored-answer (:scoring q-score-model)))))
            acc));;if q-score-model empty

        verbatim-answer (fn [acc [question-name answer]]
                          (let [question-prefix (question-prefix question-name)
                                q-score-model (or (get model-index question-prefix)
                                                  (get model-index question-name))]
                            (if (contains? q-score-model :verbatim)
                              (assoc acc question-name answer)
                              acc)))

        group-answer (fn [acc [question-name answer]]
                       (let [question-prefix (question-prefix question-name)
                             q-score-model (get model-index question-name)]
                         (if (contains? q-score-model :group)
                           (assoc acc question-name answer)
                           acc)))]
    (-> normalized-response
        (assoc :scores
               (reduce score-answer {} (:answers normalized-response)))
        (assoc :verbatims
               (reduce verbatim-answer {} (:answers normalized-response)))
        (assoc :groups
               (reduce group-answer {} (:answers normalized-response))))))


(defn score-responses
  [model normalized-responses]
  (let [model-index (model/index-questions model)
        scored-responses (map #(score-answers model-index %) normalized-responses)
        measure-construct
        (fn [scored-response [construct spec]]
          (let [{qs :questions
                 measures :measures} spec
                qs-keys (map #(if (coll? %) (first %) %) qs) ;; just question keys in model
                construct-scores (->> qs-keys
                                      (select-keys (:scores scored-response))
                                      (remove (fn [[k v]] (= :excluded v)))
                                      (into {}))]
            (assoc-in
             scored-response [:measures construct]
             (merge
              (get-in scored-response [:measures construct] {})
              (u/map-values (fn [m]
                              (m construct-scores))
                            measures)))))]
    (map (fn [r] (reduce measure-construct r model))
         scored-responses)))


(defn- aggregate-model-construct
  [[construct model] scored-responses]
  (let [{qs :questions
         verbatim-prefixes :verbatims} model
        qs-keys (map #(if (coll? %) (first %) %) qs) ;; just question keys in model

        normalized-scores (map #(with-meta
                                  (->> qs-keys
                                       (select-keys (:scores %))
                                       (remove (fn [[k v]] (= :excluded v)))
                                       (into {}))
                                  (-> (:meta-data %)
                                      (assoc :responseId (:responseId %))
                                      (assoc :measures (:measures %))))
                    scored-responses)

        ;; compute aggregate scores across questions

        aggregate-scores (map #(let [vs (vals %)
                                     num-answers (count vs)]
                                 (let [measures (:measures (meta %))]
                                   (with-meta
                                     {:mean-score (transduce identity mean vs)
                                      :measures (get measures construct)
                                      :num-answers num-answers
                                      :num-questions (count (keys %))}
                                     (meta %))))
                              normalized-scores)

        complete-answers (filter #(= (:num-answers %) (count qs-keys))
                                 aggregate-scores)
        worst-scores (sort-by :mean-score complete-answers)
        best-scores (reverse worst-scores)

        construct-measures (apply merge-with
                            (fn [x y]
                              (if (coll? x)
                                (conj x y)
                                [x y]))
                            (map #(get-in (meta %) [:measures construct]) complete-answers))
        construct-measures (u/map-values #(if-not (coll? %) [%] %) construct-measures)

        construct-measures-stats
        (u/map-values (fn [ms]
                        {:mean (transduce identity mean ms)
                         :variance (transduce identity variance ms)
                         :quantile (calculate-quantiles
                                    ms
                                    :probs [0.0 0.15 0.25 0.5 0.75 0.85 1.0])
                         :stddev (transduce identity standard-deviation ms)})
                      construct-measures)


        mean-scores (filter (complement nil?) (map :mean-score complete-answers))

        score-variance (transduce identity variance mean-scores)

        score-stddev (transduce identity standard-deviation mean-scores)


        scores-by-question (apply merge-with
                                  (fn [x y]
                                    (if (coll? x)
                                      (conj x y)
                                      [x y]))
                                  normalized-scores)
        ;; ensure everything is a collection, even if only one response
        scores-by-question (u/map-values #(if-not (coll? %) [%] %) scores-by-question)


        question-stats (u/map-values
                        (fn [vs] {:score-total (reduce + vs)
                                  :score-mean  (transduce identity mean vs)
                                  :score-variance (transduce identity variance vs)
                                  :score-stddev (transduce identity standard-deviation vs)})
                        scores-by-question)


        worst-question   (->> question-stats
                              (sort-by (comp :score-total second))
                              (map first))
        best-question    (reverse worst-question)
        stable-question  (->> question-stats
                              (sort-by (comp :score-variance second))
                              (map first))
        varying-question (reverse stable-question)

        construct-score-total (reduce + 0 mean-scores)
        construct-mean (transduce identity mean mean-scores)
        construct-var (transduce identity variance mean-scores)
        construct-stddev (transduce identity standard-deviation mean-scores)

        prefix-key? (fn [key prefixes]
                      (some #(.startsWith (name key) (name %)) prefixes))

        verbatims (map #(->> (:verbatims %)
                             (filter (fn [[k _]]
                                       (prefix-key? k verbatim-prefixes)))
                             (into {}))
                       scored-responses)

        agg-verbatims (apply merge-with
                             (fn [x y] (if (coll? x) (conj x y) [x y]))
                             verbatims)]
    [construct
     {:construct-stats construct-measures-stats

      :response-stats
      {:response-scores aggregate-scores
       :worst-scores worst-scores
       :best-scores best-scores
       :response-score-variance score-variance
       :response-score-stddev score-stddev}

      :question-stats
      {:scores-by-question scores-by-question
       :question-stats question-stats
       :best-questions best-question
       :worst-questions worst-question
       :varying-questions varying-question
       :stable-questions stable-question}

      :verbatims agg-verbatims}]))

(defn aggregate-scores
  [model scored-responses]
  (into {} (map #(aggregate-model-construct % scored-responses)
                model)))

(defn score->answer
  [model-index qid score]
  (let [q-scores (:scoring (get model-index qid))
        scale (:scale (get model-index qid))
        scaled-score (scale score q-scores)]
    (some (fn [[answer qscore]]
            (when (= qscore scaled-score) answer))
          q-scores)))

(defn take-questions
  [type n aggregate-results]
  (let [stats (get aggregate-results :question-stats)
        qid-seq (take n (get stats type))]
    (map (fn [qid]
           [qid (merge
                 {:score-distribution (get-in stats [:scores-by-question qid])}
                 (get-in stats [:question-stats qid]))])
         qid-seq)))

(defn filter-responses [ks pred responses]
  (let [measure-predicate (fn [response]
                            (let [measure (get-in response (concat [:measures] ks))]
                              (if (Double/isNaN measure)
                                false
                                (pred measure))))]
    (filter measure-predicate responses)))
