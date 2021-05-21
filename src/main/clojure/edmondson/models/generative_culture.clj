(ns edmondson.models.generative-culture
  (:require [edmondson.likert :as likert]))

(def generative-culture
  {"Generative culture"
   {:scoring likert/agree-disagree-7-pt-params
    :scale likert/default-scale
    :measures {:gci (likert/normalize-likert 7 7)
               :avg likert/avg-measure}
    :questions ["Messengers are not punished when they deliver news of failures or other bad news."
                "Responsibilities are shared."
                "Cross-functional collaboration is encouraged and rewarded."
                "New ideas are welcomed."
                "Failures are investigated, not ignored or hidden."
                "Failures are treated primarily as opportunities to improve."
                "Information is actively sought."]}})
