(ns clj-new-relic.impl)

(set! *warn-on-reflection* true)

(definterface ITraced
  (invoke [])
  (invoke [arg1])
  (invoke [arg1 arg2])
  (invoke [arg1 arg2 arg3])
  (invoke [arg1 arg2 arg3 arg4])
  (invoke [arg1 arg2 arg3 arg4 more]))

(defonce error-message-printed (atom false))
(defn print-error-message []
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

(defmacro define-traced-class [name args]
  (let [invoke (if (class-exists? 'com.newrelic.api.agent.Trace)
                 (with-meta 'invoke {'com.newrelic.api.agent.Trace args})
                 (do (print-error-message)
                     'invoke))]
    `(deftype ~name [f#]
       ITraced
       (~invoke [_] (f#))
       (~invoke [_ arg1#] (f# arg1#))
       (~invoke [_ arg1# arg2#] (f# arg1# arg2#))
       (~invoke [_ arg1# arg2# arg3#] (f# arg1# arg2# arg3#))
       (~invoke [_ arg1# arg2# arg3# arg4#] (f# arg1# arg2# arg3# arg4#))
       (~invoke [_ arg1# arg2# arg3# arg4# more#] (apply f# arg1# arg2# arg3# arg4# more#)))))

(defmacro with-agent [& body]
  (when (class-exists? 'com.newrelic.api.agent.NewRelic)
    `(do ~@body)))
