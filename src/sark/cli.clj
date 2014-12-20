;; -*- coding: utf-8 -*-
;;
;; © 2013, 2014 Ian Eure.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.cli
  (:require [sark.core :as sark]
            [sark.api :as api]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main [& _]
  (sark/init)
  (jetty/run-jetty api/handler {:port 3000}))
