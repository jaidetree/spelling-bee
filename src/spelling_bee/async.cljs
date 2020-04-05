(ns spelling-bee.async
  (:refer-clojure :exclude [delay])
  (:require
   [spelling-bee.promise :as p]))

(defn resources
  "
  Example:

  (resources
    :browser (fn []
               (.launch puppeteer))
    :page    (fn [{:keys [browser]}]
               (p/-> (.pages browser) pages
                          (nth pages 0)))
    nil      (fn [{:keys [page]}]
               (.goto page \"https://google.com\")))

  (resources
    :browser []        (.launch puppeteer)
    :page    [browser] (p/-> (.pages browser) pages
                                  (nth pages 0)))
  "
  [init & resources]
  (reduce
   (fn [res [name setup]]
     (println name res)
     (if name
       (p/as-> res resources
         (p/as-> (setup resources) resource
           (assoc resources name resource)))
       (p/as-> res resources
         (setup resources)
         res)))
   (p/resolve init)
   (partition 2 resources)))


(defn tasks
  "
  Example:

  (tasks {}
    (fn [{:keys [page]}]
      (.click page \"play\")))
  "
  [res & tasks]
  (let [res-promise (p/resolve res)]
    (reduce
     (fn [chain task]
       (p/as-> res-promise res-map
         (task res-map)
         res-promise))
     tasks)))


(defn timeout
  "
  Example:

  (-> (timeout (* 7 60 1000))
      (p/then (println \"pizza is ready\") ))
  "
  [ms]
  (p/promise
   (js/setTimeout #(resolve (.now js/Date)) ms)))

(defn delay
  "
  Example:

  (-> (delay (* 7 60 1000) \"pizza is ready\")
      (p/then [msg] (println msg) ))
  "
  [ms x]
  (-> (timeout ms)
      (p/then [_] x)))
