(ns spelling-bee.browser
  (:require
   [spelling-bee.async :as async]
   [spelling-bee.promise :as p]
   ["highland" :as stream]
   ["puppeteer" :as puppeteer]))

(defn init
  []
  (->
   (async/resources
    {}
    :browser (fn []
               (.launch puppeteer #js {:headless false
                                       :defaultViewport nil
                                       :args #js ["--start-fullscreen"]}))
    :page    (fn [{:keys [browser]}]
               (promise-> (.pages browser) $
                          (nth $ 0))))
   (async/tasks
    println
    (fn [{:keys [page]}]
      (.goto page "https://www.nytimes.com/puzzles/spelling-bee"
             #js {:waitUntil "networkidle2"}))
    (fn [_]
      (async/timeout 3000))
    (fn [_]
      (println "timeout"))
    (fn [{:keys [browser]}]
      (.close browser)))))

(defn enter-matches
  [word]
  (.of stream word)
  (println word))


(comment
  (.resolve js/Promise {}))


(defn run-test
  []
  (macroexpand '(p/let [browser (.launch puppeteer)
                        pages (.pages browser)
                        page (nth pages 0)]
                  (.goto page "https://google.com"))))

(run-test)
