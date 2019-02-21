(ns clj-new-relic.core-test
  (:require [clojure.test :refer :all]
            [clj-new-relic.core :refer :all]))

(defn-traced increment [a] (+ a 1))
(defn-traced increment-with-doc "increments" [a] (+ a 1))
(defn-traced- increment-private-with-doc "increments" [a] (+ a 1))

(deftest defn-traced-test
  (testing "defn-traced can be invoked"
    (is (= 2 (increment 1))))

  (testing "defn-traced generates doc and works"
    (is (= 2 (increment 1)))
    (is (= "increments" (:doc (meta #'increment-with-doc)))))

  (testing "defn-traced- generates doc and works"
    (is (= 2 (increment 1)))
    (is (= "increments" (:doc (meta #'increment-private-with-doc))))))

