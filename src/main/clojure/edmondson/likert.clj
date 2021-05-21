(ns edmondson.likert)

(def agree-disagree-7-pt-params ;; default scoring for 7pt Likert scale
  {"I strongly agree" 7
   "I agree" 6
   "I agree a little bit" 5
   "I neither agree or disagree" 4
   "I disagree a little bit" 3
   "I disagree" 2
   "I strongly disagree" 1})

(def original-agree-disagree-7-pt-params ;; default scoring for 7pt Likert scale
  {"Strongly agree" 7
   "Agree" 6
   "Somewhat agree" 5
   "Neutral" 4
   "Somewhat disagree" 3
   "Disagree" 2
   "Strongly disagree" 1})

(defn default-scale "identity scoring transformation" [a _] a)

(defn negative-scale
  "Scoring transform for reverse-scored likert questions"
  [a likert-scale]
  (- (inc (count likert-scale)) a))

(defn avg-measure
  [response-scores]
  (let [scores (vals response-scores)
        num-scores (count scores)]
    (if (> num-scores 0)
      (/ (reduce + scores) num-scores)
      ##NaN)))

(defn normalize-likert
  "Normalize a likert-based construct to 0-100 range"
  [num-questions max-score] ;; min score is assumed to be 1
  (let [factor (/ 100 (- (* max-score num-questions) num-questions))]
    (fn [response-scores]
      (let [score (reduce + (vals response-scores))]
        (if (or (Double/isNaN score) (< score num-questions)) ;; invalid result
          ##NaN
          (int (* factor (- score num-questions))))))))
