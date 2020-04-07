(ns spelling-bee.browser
  (:require
   [clojure.string :as s]
   [spelling-bee.async :as async]
   [spelling-bee.promise :as p]
   ["highland" :as stream]
   ["puppeteer" :as puppeteer]))

(defn launch-browser
  []
  (.launch puppeteer #js {:headless false
                          :defaultViewport nil
                          :args #js ["--start-fullscreen"]}))

(defn open-puzzle
  [page]
  (.goto page "https://www.nytimes.com/puzzles/spelling-bee"
         #js {:waitUntil "networkidle2"}) )

(defn click-play
  [page]
  (.click page ".pz-modal__button"))

(defn init
  []
  (p/let [browser (launch-browser)
          pages (.pages browser)
          page (nth pages 0)]
    (p/doto page
      (open-puzzle)
      (click-play))
    {:browser browser
     :page page}))

(defn query-charset
  []
  (let [cells (.querySelectorAll js/document ".hive-cell text")]
    (-> (.from js/Array cells)
        (.map #(.-textContent %)))))

(defn letters->charset
  [letters]
  (->> letters
       (js->clj)
       (s/join "")))

(defn get-charset
  [{:keys [page] :as state}]
  (p/let [letters (.evaluate page query-charset)
          charset (letters->charset letters)]
    (assoc state :charset charset)))

(defn input-word
  [page word]
  (p/let [keyboard (.-keyboard page)]
    (.type keyboard word #js {:delay 100})
    (.press keyboard "Enter" #js {:delay 100})
    (async/timeout 500)))

(defn zero-pad
  [n]
  (if (> n 9) (str n)
      (str "0" n)))

(defn get-timestamp
  []
  (let [date (js/Date.)
        date-str (s/join "-"
                         (map zero-pad
                              [(.getFullYear date)
                               (inc (.getMonth date))
                               (.getDate date)]))
        time-str (s/join "."
                         (map zero-pad
                              [(.getHours date)
                               (.getMinutes date)
                               (.getSeconds date)]))
        tz (* -100 (/ (.getTimezoneOffset date) 60))
        tz-str (str (when (pos? tz) "+") tz)]
    (str date-str " at " time-str tz-str)))

(comment
  (get-timestamp))

(defn dismiss-genius-modal
  [page]
  (p/let [modal (.$ page ".sb-modal-frame.congrats")
          ts (get-timestamp)]
    (when modal
      (p/try
        (.screenshot page #js {:path (str "./victories/" ts ".jpg")
                               :type "jpeg"
                               :quality 100
                               :fullPage true})
        (catch js/Error e
          (.error js/console e))
        (finally
          (.click page ".sb-modal-frame .sb-modal-button"))))))

(defn enter-word
  [word page]
  (-> (p/doto page
          (input-word word)
          (dismiss-genius-modal))
      (stream)))

(defn enter-matching-words
  [{:keys [page search] :as state}]
  (-> search
      (.tap println)
      (.map #(enter-word % page))
      (.series)
      (.last)
      (.map (constantly state))
      (.toPromise js/Promise)))

(defn close
  [{:keys [browser]}]
  (when (and browser (.-close browser))
    (.close browser)))

(comment
  (.resolve js/Promise {}))
