(ns clj-new-relic.core
  (:require [clojure.core :as core])
  (:refer-clojure :exclude [defn]))

(set! *warn-on-reflection* true)

(definterface ITraced
  (run0 [])
  (run1 [arg1])
  (run2 [arg1 arg2])
  (run3 [arg1 arg2 arg3])
  (run4 [arg1 arg2 arg3 arg4])
  (run_more [arg1 arg2 arg3 arg4 more]))

(defonce error-message-printed (atom false))
(core/defn print-error-message []
  (when-not @error-message-printed
    (reset! error-message-printed true)
    (binding [*out* *err*]
      (println "WARNING: [CLJ-NEW-RELIC] Cannot Find com.newrelic.api.agent.Trace. Please add newrelic to project.clj to get clojure traces."))))

(defn class-exists? [class-sym]
  (try
    (resolve class-sym)
    true
    (catch ClassNotFoundException e
      false)))

(defmacro deftraced [name args]
  (if (class-exists? 'com.newrelic.api.agent.Trace)
    `(deftype ~name [f#]
       ITraced
       (^{com.newrelic.api.agent.Trace ~args} run0 [_] (f#))
       (^{com.newrelic.api.agent.Trace ~args} run1 [_ arg1#] (f# arg1#))
       (^{com.newrelic.api.agent.Trace ~args} run2 [_ arg1# arg2#] (f# arg1# arg2#))
       (^{com.newrelic.api.agent.Trace ~args} run3 [_ arg1# arg2# arg3#] (f# arg1# arg2# arg3#))
       (^{com.newrelic.api.agent.Trace ~args} run4 [_ arg1# arg2# arg3# arg4#] (f# arg1# arg2# arg3# arg4#))
       (^{com.newrelic.api.agent.Trace ~args} run_more [_ arg1# arg2# arg3# arg4# more#] (apply f# arg1# arg2# arg3# arg4# more#)))
    (do
      (print-error-message)
      `(deftype ~name [f#]
         ITraced
         (run0 [_] (f#))
         (run1 [_ arg1#] (f# arg1#))
         (run2 [_ arg1# arg2#] (f# arg1# arg2#))
         (run3 [_ arg1# arg2# arg3#] (f# arg1# arg2# arg3#))
         (run4 [_ arg1# arg2# arg3# arg4#] (f# arg1# arg2# arg3# arg4#))
         (run_more [_ arg1# arg2# arg3# arg4# more#] (apply f# arg1# arg2# arg3# arg4# more#))))))

(defmacro defn [sym & args]
  (let [clazz-name (-> sym munge (str "_traced") symbol)]
    `(do
       (deftraced ~clazz-name {})
       (let [inner-function# (fn ~sym ~@args)
             traced# (new ~clazz-name inner-function#)]
         (core/defn ~sym
           ([] (.run0 traced#))
           ([arg1#] (.run1 traced# arg1#))
           ([arg1# arg2#] (.run2 traced# arg1# arg2#))
           ([arg1# arg2# arg3#] (.run3 traced# arg1# arg2# arg3#))
           ([arg1# arg2# arg3# arg4#] (.run4 traced# arg1# arg2# arg3# arg4#))
           ([arg1# arg2# arg3# arg4# & more#] (.run_more traced# arg1# arg2# arg3# arg4# more#)))))))
