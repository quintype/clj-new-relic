(ns clj-new-relic.core)

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

(defmacro defn-traced [sym & args]
  (let [clazz-name (-> sym munge (str "_traced") symbol)]
    `(do
       (define-traced-class ~clazz-name {:metricName ~(str *ns* \/ sym)
                                         :dispatcher true})
       (let [inner-function# (fn ~sym ~@args)
             traced# (new ~clazz-name inner-function#)]
         (defn ~sym
           ([] (.invoke traced#))
           ([arg1#] (.invoke traced# arg1#))
           ([arg1# arg2#] (.invoke traced# arg1# arg2#))
           ([arg1# arg2# arg3#] (.invoke traced# arg1# arg2# arg3#))
           ([arg1# arg2# arg3# arg4#] (.invoke traced# arg1# arg2# arg3# arg4#))
           ([arg1# arg2# arg3# arg4# & more#] (.invoke traced# arg1# arg2# arg3# arg4# more#)))))))
