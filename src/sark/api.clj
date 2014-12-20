;; -*- coding: utf-8 -*-
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
            [clojure.java.io :as io]
            [ring.util.response :as r]
            [compojure.route :as route]
            [compojure.handler :as handler]))

(def ^:constant bounce
  (-> (r/redirect "/index.html")
      (r/status 301)))

(def ^:constant nil-resp (r/response []))

(defroutes sark-routes
  (GET "/s" {{q "q"} :query-params}
       (if-not (empty? q) (sark/search @sark/index q)
         nil-resp))
  (GET "/" [] (io/resource "web/index.html")))

(def handler
  (-> sark-routes
      (handler/api)
      (wrap-json-response)
      (wrap-resource "web")
      (wrap-file-info)))
