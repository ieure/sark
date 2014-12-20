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
(ns sark.arcarc
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [taoensso.timbre :as log])
  (:use [slingshot.slingshot]))

(def ^:const base "http://arcarc.xmission.com/")
(def ^:const source (str base "index.txt"))

(def state (atom {:lmd nil, :files nil}))

(defn- update* [req-map] "Update ArcArc index - internal."
  (let [resp (client/get source req-map)]
    {:lmd (get-in resp [:headers "last-modified"])
     :files (-> resp
                (:body)
                (io/reader)
                (line-seq))}))

(defn update "Update the ArcArc index"
  ([] (update nil))

  ([last-modified]
     (update* (conj {:as :stream}
                    (when-not (nil? last-modified)
                      {:headers {"if-modified-since" last-modified}})))))

(defn fetch-local []
  "Load the local cache of the ArcArc index."
  (let [lmd (slurp (io/resource "index.txt.lmd"))]
    (log/infof "Loaded cache with LMD of `%s'" lmd)
    {:lmd lmd
     :files (-> (io/resource "index.txt")
                (io/reader)
                (line-seq))}))

(defn do-update "Refresh the index."
  ([] (do-update @state))
  ([{:keys [lmd] :as state}]
     (try+
      (let [{:keys [lmd] :as new-state} (update lmd)]
        (log/infof "index.txt updated at `%s'" lmd)
        [true new-state])
      (catch [:status 304] _
        (log/infof "No modifications since `%s; nothing to update." lmd)
        [nil state]))))

(defn load-cache! []
  (reset! state (fetch-local)))

(defn status []
  (let [s @state]
    {:index-last-updated (:lmd s)
     :documents (count (:files s))}))
