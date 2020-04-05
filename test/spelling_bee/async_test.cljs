(ns spelling-bee.async-test
  (:require
    [cljs.pprint :refer [pprint]]
    [spelling-bee.promise :as p]
    [spelling-bee.async :as async]
    ["puppeteer" :as puppeteer]))


(defn -main
  [& args]
  (pprint (macroexpand
           '(p/let [browser (.launch puppeteer)
                    pages (.pages browser)
                    page (nth pages 0)]
              (.goto page "https://google.com")
              (async/timeout 3000)
              (.close browser))))
  (-> (p/let [browser (.launch puppeteer #js {:headless false})
              pages (.pages browser)
              page (nth pages 0)]
        (.goto page "https://google.com")
        (async/timeout 3000)
        (.close browser))
      (.catch (fn [e]
                (println e)))))
