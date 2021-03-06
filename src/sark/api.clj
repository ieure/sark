;; -*- coding: utf-8 -*-
;;
;; eeeee eeeee eeeee  e   e
;; 8   " 8   8 8   8  8   8
;; 8eeee 8eee8 8eee8e 8eee8e
;;    88 88  8 88   8 88   8
;; 8ee88 88  8 88   8 88   8
;;
;; © 2013, 2014 Ian Eure.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.api
  (:use [compojure.core]
        [ring.middleware.resource]
        [ring.middleware.file-info]
        [ring.middleware.format-response :only [wrap-json-response]]
        [clojure.pprint :only [pprint]])
  (:require [sark.core :as sark]
            [sark.meters :as meters]
            [clojure.java.io :as io]
            [ring.util.response :as r]
            [compojure.route :as route]
            [compojure.handler :as handler]))

(def ^:constant bounce
  (-> (r/redirect "/index.html")
      (r/status 301)))

(def ^:constant nil-resp (r/response []))

(defroutes sark-routes
  ;; Front page
  (GET "/" [] (io/resource "web/index.html"))

  ;; Return search results
  (GET "/s" {{q "q"} :query-params}
       (if-not (empty? q) (sark/search @sark/index q)
               nil-resp))

  ;; Record clickthrough
  (PUT "/c" {{d "d"} :query-params}
       (do (meters/click! d)
           nil-resp))

  ;; Stats
  (GET "/stats" [] (r/response (sark/stats))))

(def handler
  (-> sark-routes
      (handler/api)
      (wrap-json-response)
      (wrap-resource "web")
      (wrap-file-info)))
