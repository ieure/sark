;; -*- coding: utf-8 -*-
;;
;; eeeee eeeee eeeee  e   e
;; 8   " 8   8 8   8  8   8
;; 8eeee 8eee8 8eee8e 8eee8e
;;    88 88  8 88   8 88   8
;; 8ee88 88  8 88   8 88   8
;;
;; © 2014 Ian Eure.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.meters
  (:require
   [taoensso.timbre :as log]
   [metrics.core :refer [new-registry]]
   [metrics.meters :refer [defmeter mark! rates] :as meter]
   [metrics.histograms :refer [defhistogram update! percentiles mean]]))

(def reg (new-registry))

(defmeter reg searches)

(defhistogram reg results)

(defmeter reg clicks)

(defn searched! [sres]
  (mark! searches)
  (update! results (count sres))
  sres)

 ;; Click tracking

(def ^:const sorted
  "An empty sorted map, to save results in."
  (sorted-map-by (fn [key1 key2]
                   (compare [(get results key2) key2]
                            [(get results key1) key1]))))

(def clicked "A sorted map of top-clicked results."
  (agent sorted))

(def ^:const click-size "Number of top-clicked results to display."
  10)

(defn- click* [state doc] "Record a click"
  (into sorted (update-in state [doc] (fn [n] (if n (inc n) 1)))))

(defn- prune [state]
  "Prune the click map.

   The map will be no more than 5x of click-size."
  (let [tn (* click-size 5)]
    (if (> (count state) tn)
      (into sorted (take tn state))
      state)))

(defn click! [doc] "Save a click on a document"
  (log/debugf "Clicking `%s'" doc)
  (send clicked click* doc))

(defn clicked-top [state] "Return the most clicked results"
  (map first (take click-size @clicked)))



(defn stats []
  {:searches {:rates (rates searches)
              :count (meter/count searches)}
   :results {:percentiles (percentiles results)
             :mean (mean results)}})
