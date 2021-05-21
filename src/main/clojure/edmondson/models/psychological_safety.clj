(ns edmondson.models.psychological-safety
  (:require [edmondson.likert :as likert]))

(def psychological-safety
  {
   "Psychological safety"
   {:scoring likert/agree-disagree-7-pt-params ;; default scoring with 7p likert scale
    :scale likert/default-scale ;; default scale is normal scale
    :measures {:psi (likert/normalize-likert 7 7)
               :avg likert/avg-measure}
    :questions [["If you make a mistake on this team, it is often held against you."
                 ;; override default scale: this questions is scored in reverse
                 ;; since Strongly agree is quite bad here.
                 {:scale likert/negative-scale}]
                "Members of this team are able to bring up problems and tough issues." ;; defaults
                ["People on this team sometimes reject others for being different."
                 {:scale likert/negative-scale}]
                "It is safe to take a risk in this team."
                ["It is difficult to ask other members of this team for help."
                 {:scale likert/negative-scale}]
                "No one on this team would deliberately act in a way that undermines my efforts."
                "Working with members of this team, my unique skills and talents are valued and utilized."]}})
