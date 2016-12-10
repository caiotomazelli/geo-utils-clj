# geo-utils

A Clojure library designed to perform some common operation with geo objects (Cartesian and Lat-Lgn points, line segments and polylines).

## How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.

## License

Copyright © 2016 [Caio Tomazelli](https://github.com/caiotomazelli) 

Distributed under the Eclipse Public License, the same as Clojure.