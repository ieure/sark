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
(ns sark.cli
  (:require [sark.core :as sark]
            [sark.api :as api]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn -main [& _]
  (sark/init)
  (jetty/run-jetty api/handler {:port 3000}))
