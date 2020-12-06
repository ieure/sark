;; -*- coding: utf-8 -*-
;;
;; eeeee eeeee eeeee  e   e
;; 8   " 8   8 8   8  8   8
;; 8eeee 8eee8 8eee8e 8eee8e
;;    88 88  8 88   8 88   8
;; 8ee88 88  8 88   8 88   8
;;
;; © 2013, 2014, 2020 Ian Eure.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.arcarc
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]))

(def ^:const base "http://arcarc.xmission.com/")
(def ^:const source (str base "index.txt"))

(def state (atom {:lmd nil, :files nil}))

(defn fetch-update "Update ArcArc index."
  ([] (fetch-update (:lmd @state)))
  ([lmd]
   (let [{:keys [status body],
          {:strs [Date Last-Modified]} :headers}
         (client/get source {:as :stream, :headers {"if-modified-since" lmd}}),]
     (log/infof "Status %d, last remote update at `%s'" status (or Last-Modified Date))
     (when-not (= 304 status)
       {:lmd Last-Modified
        :files (line-seq (io/reader body))}))))

(defn fetch-local []
  "Load the local cache of the ArcArc index."
  (let [lmd (slurp (io/resource "index.txt.lmd"))]
    (log/infof "Loaded cache with LMD of `%s'" lmd)
    {:lmd lmd
     :files (-> (io/resource "index.txt")
                (io/reader)
                (line-seq))}))

(defn load-cache! []
  (reset! state (fetch-local)))

(defn status []
  (let [s @state]
    {:index-last-updated (:lmd s)
     :documents (count (:files s))}))
