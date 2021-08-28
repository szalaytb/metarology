(ns clojure-metarology.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as wp]
            [environ.core :refer [env]]))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (concat "<!DOCTYPE html><head><title>Weather report station CE</title>
                 <style>body {font-size: 24px; padding:20px;}</style>"
                 "<link rel=\"stylesheet\" href=\"https://unpkg.com/purecss@2.0.6/build/pure-min.css\" integrity=\"sha384-Uu6IeWbM+gzNVXJcM9XV3SohHtmWE+3VGi496jvgX1jyvDTXfdK+rfZc8C1Aehk5\" crossorigin=\"anonymous\">"
                 "</head><body>"
                 "<p>You have few options to choose from:</p>"
                 "<a href=\"/targeting\">targeting</a><br /><hr>"
                 "<a href=\"/hoard\">hoard</a><br /><hr>"
                 "<a href=\"/collect\">collect</a><br /><hr>"
                 "<p>Targeting targets a specific country's weather</p>"
                 "<p>While hoard gathers information about them all</p>"
                 "<p>Collect saves all of the METAR reports for later</p>"
                 "</body></html")})
(defn splush []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Collection script (probably) ran successfuly."})

(defn WEATHER_DATA [raw_data]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (concat "<!DOCTYPE html><head><title>Weather report station CE</title>
                 <style>body {font-size: 24px; max-width:100%;}</style>
                 </head><body>" raw_data "</body></html>")
    })

(defn targeting [target_data]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (concat "<!DOCTYPE html><head><title>Targeted search</title>
                 <style>body {font-size: 24px; max-width:100%;}</style></head>
                 <body><a href=\"https://tgftp.nws.noaa.gov/data/observations/metar/decoded/" target_data ".TXT\">Weather information</a></body></html>")})

(defn select_station []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (concat
           "<!DOCTYPE html><head><title>Weather report station CE</title>
                 <style>body {font-size: 24px; max-width:100%;}</style>
                 </head><body>"
           "<form action=\"/action-page\" method=GET>"
           "<p>Enter the weather station you would like from the list we made</p>"
           "<label for=\"cities\">Choose a city:</label>
            <select id=\"cities\" name=\"city\">
              <option value=\"LHBP\">Hungary, Budapest</option>
              <option value=\"LOWW\">Austria, Vien</option>
              <option value=\"LKPR\">Czech Republic, Prague</option>
              <option value=\"LRBS\">Romania, Bucharest</option>
              <option value=\"UKKK\">Ukraine, Kiev</option>
              <option value=\"LZIB\">Slovakia, Bratislava</option>
              <option value=\"EPWA\">Poland, Warsaw</option>
              <option value=\"EDDB\">Germany, Berlin</option>
              <option value=\"LFPO\">France, Paris</option>
              <option value=\"EGGS\">Britain, London</option>
              <option value=\"LIRF\">Italy, Rome</option>
              <option value=\"LDZA\">Croatia, Zagreb</option>
              <option value=\"LGAV\">Greece, Athens</option>
              <option value=\"UUEE\">Russia, Moscow</option>
              <option value=\"LEMD\">Spain, Madrid</option>
            </select><br><br>"
           "<input type=\"submit\" value=\"Submit\">"
           "</form>"
           "</body></html>"
           )
   })

(defn newline-to-br [s]
  (clojure.string/replace s #"\r\n|\n|\r" "<br />\n"))

(defroutes app
  (GET "/" []
       (splash))
  (GET ["/:Station", :Station #"[A-Z]{4,4}"] [Station]
       (def METAR (slurp (apply str (concat "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/" Station ".TXT")))))
  (GET ["/:Station/wind", :Station #"[A-Z]{4,4}"] [Station]
  	(def METAR (slurp (apply str (concat "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/" Station ".TXT"))))
  	(def RESULT (concat (concat (concat (apply str (re-find (re-pattern "Temperature: \\d+\\s+\\S+\\s+\\S+\\d+\\s+\\S+") METAR))
  	(apply str (re-find (re-pattern "Wind: from the \\S+") METAR)))
  	(apply str (re-find (re-pattern "Wind: Variable") METAR)))
  	(apply str (re-find (re-pattern "Wind: Calm") METAR)))
  	)
  	)
  (GET "/targeting" []
       (select_station))
  (GET "/action-page" {params :query-params}
       (targeting (apply str(get params "city"))))
  (GET "/hoard" []
       (let [Wien "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LOWW.TXT"
             Prague "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LKPR.TXT"
             Budapest "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LHBP.TXT"
             Bucharest "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LRBS.TXT"
             Kiev "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/UKKK.TXT"
             Bratislava "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LZIB.TXT"
             Warsaw "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/EPWA.TXT"
             Berlin "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/EDDB.TXT"
             Paris "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LFPO.TXT"
             London "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/EGSS.TXT"
             Rome "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LIRF.TXT"
             Zagreb "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LDZA.TXT"
             Athens "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LGAV.TXT"
             Moscow "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/UUEE.TXT"
             Madrid "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LEMD.TXT"]
         (WEATHER_DATA (newline-to-br (apply str (concat
                           (slurp Wien) "\n"
                           (slurp Prague) "\n"
                           (slurp Budapest) "\n"
                           (slurp Bucharest) "\n"
                           (slurp Kiev) "\n"
                           (slurp Bratislava) "\n"
                           (slurp Warsaw) "\n"
                           (slurp Berlin) "\n"
                           (slurp Paris) "\n"
                           (slurp London) "\n"
                           (slurp Rome) "\n"
                           (slurp Zagreb) "\n"
                           (slurp Athens) "\n"
                           (slurp Moscow) "\n"
                           (slurp Madrid)
                           ))))))
  (GET "/collect" []
       (spit (apply str (concat "weather.backlog" "." (.toString (java.time.LocalDateTime/now)) ".txt" ))
                (let [Wien "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LOWW.TXT"
                      Prague "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LKPR.TXT"
                      Budapest "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LHBP.TXT"
                      Bucharest "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LRBS.TXT"
                      Kiev "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/UKKK.TXT"
                      Bratislava "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LZIB.TXT"
                      Warsaw "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/EPWA.TXT"
                      Berlin "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/EDDB.TXT"
                      Paris "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LFPO.TXT"
                      London "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/EGSS.TXT"
                      Rome "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LIRF.TXT"
                      Zagreb "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LDZA.TXT"
                      Athens "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LGAV.TXT"
                      Moscow "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/UUEE.TXT"
                      Madrid "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/LEMD.TXT"]
                                                            (newline-to-br (apply str (concat
                                                                           (slurp Wien) "\n"
                                                                           (slurp Prague) "\n"
                                                                           (slurp Budapest) "\n"
                                                                           (slurp Bucharest) "\n"
                                                                           (slurp Kiev) "\n"
                                                                           (slurp Bratislava) "\n"
                                                                           (slurp Warsaw) "\n"
                                                                           (slurp Berlin) "\n"
                                                                           (slurp Paris) "\n"
                                                                           (slurp London) "\n"
                                                                           (slurp Rome) "\n"
                                                                           (slurp Zagreb) "\n"
                                                                           (slurp Athens) "\n"
                                                                           (slurp Moscow) "\n"
                                                                           (slurp Madrid) "\n"
                                                                          )))))(splush))

  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
