(ns spelling-bee.promise
  (:refer-clojure :exclude [-> resolve let])
  (:require [clojure.core :as cc]))

(defmacro promise
  "
  Example:

  (promise (resolve 5))
  (promise (reject  (js/Error. \"oops\")))
  "
  [body]
  `(js/Promise. (fn [~'resolve ~'reject] ~body)))


(defmacro ->
  "
  Example:

  (promise-> (.resolve js/Promise {}) $
             (assoc $ :a 1)
             (assoc $ :b 2)
             (println))
  ;; => {:a 1 :b 2}
  ;; => #object[Promise [object Promise]]
  "
  [expr name & forms]
  `(cc/-> ~expr
          ~@(map (fn [form]
                   `(.then
                     (fn [~name]
                       ~form))) forms)))

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

(defn wrap-promise
  [results [name expr]]
  (conj results `(fn [~name]
                   ~expr)))

(defmacro let
  [bindings & body]
  (letfn [(recurse [[name expr & bindings] body]
            (cond
              name `(.then
                     (->promise ~expr)
                     (fn [~name]
                       ~(recurse bindings body)))
              (= (count body) 1) (first body)
              :else `(-> (->promise ~(first body)) ~'$
                         ~@(rest body))))]
    `(->promise ~(recurse bindings body))))
