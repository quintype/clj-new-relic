(ns clj-new-relic.core
  (:require [clj-new-relic.impl :as impl]))

(defmacro defn-traced [sym & args2]
  (let [clazz-name (-> sym munge (str "_traced") symbol)
        docstring? (string? (first args2))
        docstring  (if docstring? (first args2) "")
        args       (if docstring? (rest args2) args2)]
    `(do
       (impl/define-traced-class ~clazz-name ~(assoc (-> sym meta :newrelic) :metricName (str "Clojure/" *ns* \/ sym)))
       (let [inner-function# (fn ~sym ~@args)
             traced#         (new ~clazz-name inner-function#)]
         (defn ~sym
           ~docstring
           ([] (.invoke traced#))
           ([arg1#]  (.invoke traced# arg1#))
           ([arg1# arg2#] (.invoke traced# arg1# arg2#))
           ([arg1# arg2# arg3#] (.invoke traced# arg1# arg2# arg3#))
           ([arg1# arg2# arg3# arg4#] (.invoke traced# arg1# arg2# arg3# arg4#))
           ([arg1# arg2# arg3# arg4# & more#] (.invoke traced# arg1# arg2# arg3# arg4# more#)))))))

(defmacro defn-traced- [sym & args]
  `(defn-traced ~(vary-meta sym assoc :private true) ~@args))

(defn notice-error [^Throwable e]
  (impl/with-agent (com.newrelic.api.agent.NewRelic/noticeError e)))

(defn add-custom-parameters [m]
  (impl/with-agent
    (doseq [[key value] m]
      (com.newrelic.api.agent.NewRelic/addCustomParameter (str key) (str value)))))
