(ns spelling-bee.async-test
  (:require
    [cljs.pprint :refer [pprint]]
    [spelling-bee.promise :as p]
    [spelling-bee.async :as async]
    ["puppeteer" :as puppeteer]))

(defn catch-test
  []
  (-> (p/promise (throw (js/Error. "Shit")))
      (p/catch [e]
          (println (str "error:" e)))))

(defn let-test
  []
  (pprint (macroexpand-1
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

(defn try-test
  []
  (pprint (macroexpand-1
           '(p/try
              (p/resolve (println "hello"))
              (p/then (async/timeout 100)
                      (println "Timeout!"))
              (throw (js/Error. "Whoops"))
              (catch js/Error e
                (println "Caught: " (.-message e)))
              (finally
                (println "Done!")))))
  (p/try
    (p/resolve (println "hello"))
    (p/then (async/timeout 100)
            (println "Timeout!"))
    (p/reject (js/Error. "Whoops"))
    (catch js/Error e
      (println "Caught:" (.-message e)))
    (finally
      (println "Done!"))))

(defn do-test
  []
  (let [x :test]
    (p/do (println "do test")
          (p/then [] x))))

(defn dummy-a
  [a]
  (println a)
  nil)

(defn dummy-b
  [a b c]
  (println {:a a :b b :c c})
  nil)

(defn doto-test
  []
  (pprint (macroexpand
           '(p/doto :test
              (dummy-a)
              (dummy-b :b :c))))
  (p/doto :test
    (dummy-a)
    (dummy-b :b :c)))


(defn -main
  [& args]
  #_(let-test)
  #_(try-test)
  #_(do-test)
  (doto-test))
