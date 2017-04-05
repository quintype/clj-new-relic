# clj-new-relic

A Clojure library designed to help trace clojure functions

## Installation

Add this to your Leiningen project.clj `:dependencies`:

    [clj-new-relic "1.2.0"]
    [com.newrelic.agent.java/newrelic-api "3.38.0"]

Jar is available in Clojars.

## Usage

Just replace your defn with `nr/defn`, after requiring `[clj-new-relic.core :as nr]`

```clojure
(clj-new-relic.core/defn-traced foobar [x]
  (prn x))
```

You can also pass in any options the tracer accepts, such as

```clojure
(clj-new-relic.core/defn-traced
  ^{:newrelic {:metricName "Clojure/my.namespace/foobar"}} foobar [x]
  (prn x))
```

You can also annotate a private method with `defn-traced-`

You can also use `notice-error` and `add-custom-parameters` for obvious purposes.

## Ring

The ring handler is used as follows

```clojure
(defn- error-handler [request ^Throwable err]
  {:status 500
   :body (str "Something went wrong " (.getMessage err))})

(-> handler
    (clj-new-relic.ring/wrap-newrelic error-handler))
```

## Using the newrelic agent

This library automatically checks for the newrelic classes at runtime. Hense, you may add new relic via the agent, and not include the dependency above

## Why this library?

Compared to other libraries doing similar things, here are the reasons this library exists

* Completely dependency free, including on new relic. If newrelic is not loaded at macro-eval time, we juts print out a warning, and continue.
* Support all newrelic options including `{:dispatcher true}` to start transactions

## Known Issues

* Functions with docstrings will not work (this will be fixed in a future edition). However, the workaround is as follows

```clojure
(defn foobar "Something great" [x] x) ; => (defn-traced ^{:doc "Something great"} foobar [x] x)
```

* If you are building an uberjar, and newrelic is not present when building the uberjar (and macros are evaluated), then newrelic will not work. This also may get fixed in a future edition

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
