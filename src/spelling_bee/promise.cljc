(ns spelling-bee.promise
  (:refer-clojure :exclude [as-> do doto let resolve])
  (:require [clojure.core :as cc]
            [cljs.pprint :refer [pprint]]))

(defmacro promise
  "
  Example:

  (promise (resolve 5))
  (promise (reject  (js/Error. \"oops\")))
  "
  [body]
  `(js/Promise. (fn [~'resolve ~'reject] ~body)))


(defmacro as->
  "
  Example:

  (as-> (.resolve js/Promise {}) $
        (assoc $ :a 1)
        (assoc $ :b 2)
        (println))
  ;; => {:a 1 :b 2}
  ;; => #object[Promise [object Promise]]
  "
  [expr name & forms]
  `(cc/-> (->promise ~expr)
          ~@(map (fn [form]
                   `(then [~name] ~form)) forms)))

(defmacro then
  "
  Example:

  (-> (promise (resolve {}))
      (then [resources] (-> (.launch \"puppeteer\")
                            (then [browser]
                              (assoc resources :browser browser)))))
  "
  ([a-promise body]
   `(.then
     ~a-promise
     (fn ~'success
             []
             ~body)))
  ([a-promise bindings & body]
   `(.then
     ~a-promise
     (fn ~'success
       ~bindings
       ~@body))))

(defmacro catch
  "
  Example:

  (-> (promise (reject \"err\"))
      (catch [msg] (println msg)))
  "
  ([a-promise body]
   `(.catch ~a-promise
           (fn catch
             []
             ~body)))
  ([a-promise bindings body]
   `(.catch ~a-promise
            (fn catch
              ~bindings
              ~body))))

(defmacro resolve
  "
  Example:

  (resolve {})
  "
  [x]
  `(.resolve js/Promise ~x))

(defmacro reject
  "
  Example:

  (reject {})
  "
  [x]
  `(.reject js/Promise ~x))

(defn- chain-expr
  [form]
  `(then ~form))


(defmacro do
  "
  Example:

  (do
    (timeout 3000)
    (println \"Time is up!\"))
  "
  [form & forms]
  `(-> (->promise ~form)
       ~@forms))


(defn- nest-promises
  [[name expr & bindings] body]
  (cond
    name
    `(then
      (->promise ~expr)
      [~name]
      ~(nest-promises bindings body))

    (= (count body) 1)
    `(->promise ~(first body))

    :else
    `(spelling-bee.promise/do
      ~(first body)
      ~@(map chain-expr (rest body)))))

(defmacro let
  "
  Example:e

  (let [browser (.launch puppeteer)
        pages (.pages browser)
        page (nth pages 0)]
    (.goto page \"https://google.com\"))
  "
  [bindings & body]
  `(->promise ~(nest-promises bindings body)))


(defn- catch-expr
  [form]
  (cc/let [[err-type argname & body] form]
    `(spelling-bee.promise/catch
         [~argname]
         (if (instance? ~err-type ~argname)
           (do
             ~(first body)
             ~@(rest body))
           (reject ~argname)))))

(defn- promise-form
  [form]
  (cond
    (and (seq? form) (= (first form) 'catch))
    (catch-expr (rest form))

    (and (seq? form) (= (first form) 'finally))
    (chain-expr (second form))

    :else
    (chain-expr form)))

(defmacro try
  "
  Example:

  (try
    (timeout 1000)
    (println \"Time is up!)
    (throw (js/Error. \"Oops\"))
    (catch js/Error e)
      (println \"Error:\" (.-message e)))
  "
  [form & forms]
  (cc/let [pforms (map promise-form forms)]
    `(spelling-bee.promise/do
      ~form
      ~@pforms)))

(comment
  (doto page
    (open-puzzle)
    (click-play))
  (-> (->promise page)
      (then [page]
            (do
              (open-puzzle page)
              page))
      (then [page]
            (do
              (click-play page)
              page))))

(defn tap-expr
  [[head & rest]]
  `(then
    [x#]
    (spelling-bee.promise/do
      (~head x# ~@rest)
      (then [] x#))))

(defmacro doto
  [init & forms]
  (cc/let [pforms (map tap-expr forms)]
    `(-> (->promise ~init)
         ~@pforms)))
