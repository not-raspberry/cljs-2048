# cljs-2048

A 2048 game in ClojureScript.

Written because I lost my phone, so I had to buy a new one. Google Play worked
well on the new phone (unlike the old one) so I could download a 2048 game and
kind of get obsessed by it for a few days.

## Overview

I'm going to host it on Github pages.

## Development

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL.

## License

Copyright © 2014 not-raspberry

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
