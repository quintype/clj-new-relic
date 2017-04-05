(ns clj-new-relic.ring
  (:require [clj-new-relic.core :as core]))

(defn- notice-error [e]
  (try
    (core/notice-error e)
    (catch Throwable e)))

(defn with-new-relic [handler error-handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (notice-error e)
        (error-handler request e)))))
