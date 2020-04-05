(ns spelling-bee.promise
  (:require-macros [spelling-bee.promise :as p]))

(defn ->promise
  [p]
  (if (instance? js/Promise p)
    p
    (.resolve js/Promise p)))
