(ns edmondson.config-test
  (:require
    [clojure.test :refer :all]
    [clojure.string :as str]
    [edmondson.config :as cfg]))

(deftest test-deep-merge
  (is (= {} (cfg/deep-merge {} {})))
  (is (= {} (cfg/deep-merge nil {})))
  (is (= {} (cfg/deep-merge {} nil)))
  (is (= nil (cfg/deep-merge nil nil)))

  (is (= {:a 1 :b 2} (cfg/deep-merge {:b 2} {:a 1})))

  (is (= {:a {} :b {:c 1 :d 2}}
         (cfg/deep-merge {:b {:c 1}}
                         {:a {} :b {:d 2}})))

  (is (= {:a {} :b {:c 1 :d 2}}
         (cfg/deep-merge {:b {:c 42}}
                         {:a {} :b {:c 1 :d 2}})))

  (is (= {:a {} :b {:c 42 :d 2}}
         (cfg/deep-merge {:a {} :b {:c 1 :d 2}}
                         {:b {:c 42}}))))
