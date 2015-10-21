# Reagent demo: widget shop #

Simple Clojure and ClojureScript example for an online shop UI using Reagent.

What's not included: 
- validation 
- authentication

## Building ##

To build a standalone uberjar that can be run with "java -jar" command,
use "lein build".

## Running locally ##

Clone this repo.
Create a PostgreSQL database with the database.sql script and set the connection parameters in settings.edn.

Run "lein repl" and invoke the -main function for the backend.
Run "lein figwheel dev" to setup ClojureScript dev environment.

Then open in local browser:
http://localhost:3000/index.html  (for the basic widgetshop product gallery sample)
http://localhost:3000/sales.html  (for the sales data listing and visualization sample)



