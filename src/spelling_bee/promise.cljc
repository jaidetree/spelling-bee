(ns spelling-bee.promise
  (:refer-clojure :exclude [as-> let resolve])
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
              (= (count body) 1) `(->promise ~(first body))
              :else `(as-> (->promise ~(first body)) ~'$
                       ~@(rest body))))]
    `(->promise ~(recurse bindings body))))

(comment
  (p/try
    (.click page "Play")
    (.click page ".sb-input")
    (p/catch js/Error e
      (println "oops" e))
    (p/finally
      (println "Done!")))
  ;; Should transform into =>
  (-> (.click page "Play")
      (then (.click page ".sb-input"))
      (catch [e]
          (if (instance? js/Error e)
            (println "oops" e)
            ;; What should we do if the error is not a type we care about?
            (reject e)))
      (then (println "Done!"))))

(defn catch-expr
  [form]
  (cc/let [[err-type argname & body] form]
    `(.catch
         (fn [~argname]
           (if (instance? ~err-type ~argname)
             (as-> (->promise ~(first body))
                 ~@(rest body))
             (reject ~argname))))))

(defn finally-expr
  [form]
  `(then ~form))

(defn chain-expr
  [form]
  `(then ~form))

(defn promise-form
  [form]
  (cond
    (and (seq? form) (= (first form) 'catch))
    (catch-expr (rest form))

    (and (seq? form) (= (first form) 'finally))
    (chain-expr (second form))

    :else
    (chain-expr form)))

(defmacro try
  [& forms]
  (cc/let [pforms (map promise-form (rest forms))]
    `(-> (->promise ~(first forms))
         ~@pforms)))
