(ns spelling-bee.solve
  (:require
   [spelling-bee.browser :as browser]
   [spelling-bee.promise :as p]
   [spelling-bee.words :as words]))

(defn -main
  [& [charset]]
  (p/let [{:keys [browser page] :as state} (browser/init)]
    (p/try
      (p/as-> state $
        (browser/get-charset $)
        (words/create-search $)
        (browser/enter-matching-words $))
      (catch js/Error e
        (.error js/console e)
        (browser/close state)))))
