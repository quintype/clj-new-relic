# clj-new-relic

A Clojure library designed to help trace clojure functions

## Installation

Add this to your Leiningen project.clj `:dependencies`:

    [clj-new-relic "1.0.0"]
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

## Using the newrelic agent

This library automatically checks for the newrelic classes at runtime. Hense, you may add new relic via the agent, and not include the dependency above

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
