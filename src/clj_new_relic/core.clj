(ns clj-new-relic.core
  (:require [clojure.core :as core]
            [clojure.reflect :as reflect])
  (:refer-clojure :exclude [defn])
  (:import clj_new_relic.NoOp))

(set! *warn-on-reflection* true)

(defmacro default-class [class1 class2]
  (if (reflect/resolve-class (.getContextClassLoader (Thread/currentThread)) (eval class1))
    (eval class1)
    (eval class2)))

(def the-interface (default-class 'com.newrelic.api.agent.Trace 'NoOp))

(definterface ITraced
  (run0 [])
  (run1 [arg1])
  (run2 [arg1 arg2])
  (run3 [arg1 arg2 arg3])
  (run4 [arg1 arg2 arg3 arg4])
  (run_more [arg1 arg2 arg3 arg4 more]))

(deftype Traced [f]
  ITraced
  (^{the-interface {}} run0 [_] (f))
  (^{the-interface {}} run1 [_ arg1] (f arg1))
  (^{the-interface {}} run2 [_ arg1 arg2] (f arg1 arg2))
  (^{the-interface {}} run3 [_ arg1 arg2 arg3] (f arg1 arg2 arg3))
  (^{the-interface {}} run4 [_ arg1 arg2 arg3 arg4] (f arg1 arg2 arg3 arg4))
  (^{the-interface {}} run_more [_ arg1 arg2 arg3 arg4 more] (apply f arg1 arg2 arg3 arg4 more)))

(defmacro defn [symbol & args]
  `(let [inner-function# (fn ~symbol ~@args)
         traced# ^Traced (->Traced inner-function#)]
     (core/defn ~symbol
       ([] (.run0 traced#))
       ([arg1#] (.run1 traced# arg1#))
       ([arg1# arg2#] (.run2 traced# arg1# arg2#))
       ([arg1# arg2# arg3#] (.run3 traced# arg1# arg2# arg3#))
       ([arg1# arg2# arg3# arg4#] (.run4 traced# arg1# arg2# arg3# arg4#))
       ([arg1# arg2# arg3# arg4# & more#] (.run_more traced# arg1# arg2# arg3# arg4# more#)))))
