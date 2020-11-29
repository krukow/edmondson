(ns edmondson.utils)

(defn map-values
  "Like map takes and returns a map.
  Maps across the values of a map-data structure, preserving keys.
  Example: (map-values inc {:a 1 :b 2 :c 3})
  => {:a 2, :b 3, :c 4}"
  [f m]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))
