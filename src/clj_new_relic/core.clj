(ns clj-new-relic.core
  (:require [clojure.core :as core]
            [clojure.reflect :as reflect])
  (:refer-clojure :exclude [defn]))

(set! *warn-on-reflection* true)

(definterface ITraced
  (run0 [])
  (run1 [arg1])
  (run2 [arg1 arg2])
  (run3 [arg1 arg2 arg3])
  (run4 [arg1 arg2 arg3 arg4])
  (run_more [arg1 arg2 arg3 arg4 more]))

(defmacro deftyped [name]
  (if (reflect/resolve-class (.getContextClassLoader (Thread/currentThread)) 'com.newrelic.api.agent.Trace)
    `(deftype ~name [f#]
       ITraced
       (^{com.newrelic.api.agent.Trace {}} run0 [_] (f#))
       (^{com.newrelic.api.agent.Trace {}} run1 [_ arg1#] (f# arg1#))
       (^{com.newrelic.api.agent.Trace {}} run2 [_ arg1# arg2#] (f# arg1# arg2#))
       (^{com.newrelic.api.agent.Trace {}} run3 [_ arg1# arg2# arg3#] (f# arg1# arg2# arg3#))
       (^{com.newrelic.api.agent.Trace {}} run4 [_ arg1# arg2# arg3# arg4#] (f# arg1# arg2# arg3# arg4#))
       (^{com.newrelic.api.agent.Trace {}} run_more [_ arg1# arg2# arg3# arg4# more#] (apply f# arg1# arg2# arg3# arg4# more#)))
    `(deftype ~name [f#]
       ITraced
       (run0 [_] (f#))
       (run1 [_ arg1#] (f# arg1#))
       (run2 [_ arg1# arg2#] (f# arg1# arg2#))
       (run3 [_ arg1# arg2# arg3#] (f# arg1# arg2# arg3#))
       (run4 [_ arg1# arg2# arg3# arg4#] (f# arg1# arg2# arg3# arg4#))
       (run_more [_ arg1# arg2# arg3# arg4# more#] (apply f# arg1# arg2# arg3# arg4# more#)))))

(deftyped Traced)

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
