(ns spelling-bee.solve
  (:require
   [spelling-bee.core :as bee]
   [spelling-bee.browser :as browser]
   ["puppeteer" :as puppeteer])
  (:require-macros
   [spelling-bee.promise :refer [promise->]]))


(defn argv->vec
  [args]
  (-> (.from js/Array args)
      (.slice 3)
      (js->clj)))

;; (browser/init)

(comment
  (.then (.resolve js/Promise "hello") println)
  (-> (.-argv js/process)
      (argv->vec)
      (first)
      (bee/find-words)
      (.tap println)
      (.flatMap browser/enter-match)
      (.done println))
  (-> (bee/find-words "ohtcumn")
      (.each println)))
