(ns clj-new-relic.core
  (:require [clj-new-relic.impl :as impl]))

(defmacro defn-traced [sym & args]
  (let [clazz-name (-> sym munge (str "_traced") symbol)]
    `(do
       (impl/define-traced-class ~clazz-name ~(assoc (-> sym meta :newrelic) :metricName (str "Clojure/" *ns* \/ sym)))
       (let [inner-function# (fn ~sym ~@args)
             traced# (new ~clazz-name inner-function#)]
         (defn ~sym
           ([] (.invoke traced#))
           ([arg1#] (.invoke traced# arg1#))
           ([arg1# arg2#] (.invoke traced# arg1# arg2#))
           ([arg1# arg2# arg3#] (.invoke traced# arg1# arg2# arg3#))
           ([arg1# arg2# arg3# arg4#] (.invoke traced# arg1# arg2# arg3# arg4#))
           ([arg1# arg2# arg3# arg4# & more#] (.invoke traced# arg1# arg2# arg3# arg4# more#)))))))

(defmacro defn-traced- [sym & args]
  `(defn-traced ~(vary-meta sym assoc :private true) ~@args))
