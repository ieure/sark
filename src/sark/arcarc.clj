;; -*- coding: utf-8 -*-
;;
;; © 2013 Buster Marx, Inc All rights reserved.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.arcarc
  (:require [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:use [slingshot.slingshot]))

(def ^:const base "http://arcarc.xmission.com/")
(def ^:const source (str base "index.txt"))

(defn update* [req-map]
  (let [resp (client/get source req-map)]
    {:lmd (get-in [:headers "last-modified"] resp)
     :files (-> resp
                (:body)
                (io/reader)
                (line-seq))}))

(defn update
  ([] (update nil))

  ([last-modified]
     (update* (conj {:as :stream}
                    (when-not (nil? last-modified)
                      {:headers {"if-modified-since" last-modified}})))))

(defn fetch-local []
  (let [lmd (slurp (io/resource "index.txt.lmd"))]
    (log/infof "Loaded initial index with LMD of `%s'" lmd)
    {:lmd lmd
     :files (-> (io/resource "index.txt")
                (io/reader)
                (line-seq))}))

(defn do-update [{:keys [lmd] :as state}]
  (try+
   (let [{:keys [lmd] :as new-state} (update lmd)]
     (log/infof "Index updated at `%s'" lmd)
     new-state)
    (catch [:status 304] _
      (log/infof "No modifications since `%s; nothing to update." lmd)
      state)))

(def state (agent (fetch-local)))
