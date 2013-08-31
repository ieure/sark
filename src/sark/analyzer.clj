;; -*- coding: utf-8 -*-
;;
;; © 2013 Buster Marx, Inc All rights reserved.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.analyzer
  (:require [clojure.string :as str]
            [clucy.core :as clucy])
  (:import [org.apache.lucene.analysis.core LowerCaseFilter]
           [org.apache.lucene.analysis.en PorterStemFilter]
           [org.apache.lucene.analysis.standard StandardTokenizer StandardFilter]
           [org.apache.lucene.analysis.synonym SynonymFilter SynonymMap$Builder SynonymMap]
           [org.apache.lucene.analysis.util StopwordAnalyzerBase]
           [org.apache.lucene.util CharsRef]
           [org.apache.lucene.analysis Analyzer$TokenStreamComponents]))

 ;; Monitor special cases

(def ^:constant monitor-tokens
  #{"19K6102" "19K3102" "K7000" "13k7201" "25k8104" "19k4600" "25k7401"
    "K6100" "K7200" "19k7901" "13k7301" "13k7203" "14k3101" "K7400"
    "23K7401" "K7500" "K7203" "19k4901" "K6401" "k7204" "k7205" "19k4914"
    "19V2000" "19v2000" "19k4915" "K4500" "25K5515" "13k4800" "K4600"
    "19V1001" "K5515" "K4900" "13k4705" "V2000" "V1000" "V1001" "V1002"
    "25K7191" "19k6100" "19k7201"})

(def ^:constant monitor-re #"(?i)([0-9]{2})?([kv]([0-9]{4}))")

(defn make-monitor-sym [base]
  (let [[from _ one two] (re-find monitor-re base)]
    {from one
     two from}))

(def ^:constant nyms
  {
   "13k4705" "k4705",
   "13k4800" "k4800",
   "13k7201" "k7201",
   "13k7203" "k7203",
   "13k7301" "k7301",
   "14k3101" "k3101",
   "19k3102" "k3102",
   "19k4600" "k4600",
   "19k4901" "k4901",
   "19k4914" "k4914",
   "19k4915" "k4915",
   "19k6100" "k6100",
   "19k6102" "k6102",
   "19k7201" "k7201"
   "19k7901" "k7901",
   "19v1001" "v1001",
   "19v2000" "v2000",
   "23k7401" "k7401",
   "25k5515" "k5515",
   "25k7191" "k7191",
   "25k7401" "k7401",
   "25k8104" "k8104",
   "k7000a" "k7000",
   "3101" "k3101",
   "3102" "k3102",
   "4500" "k4500",
   "4600" "k4600",
   "4705" "k4705",
   "4800" "k4800",
   "4900" "k4900",
   "4901" "k4901",
   "4914" "k4914",
   "4915" "k4915",
   "5515" "k5515",
   "6100" "k6100",
   "6102" "k6102",
   "6401" "k6401",
   "7191" "k7191",
   "7200" "k7200",
   "7201" "k7201",
   "7203" "k7203",
   "7204" "k7204",
   "7205" "k7205",
   "7301" "k7301",
   "7400" "k7400",
   "7401" "k7401",
   "7500" "k7500",
   "7901" "k7901",
   "8104" "k8104",

   "dk" "donky kong",
   "dkjr" "donkey kong jr",
   "donkey kong jr" "dkjr",
   "donkey kong" "dk",
   "dp" "schematic",
   "jnr" "junior",
   "jr" "junior",
   "junior" "jr",
   "sp" "schematic",
   "tm" "manual",
   })

(defn ^CharsRef make-ref [^String s]
  (CharsRef. (str/replace s " " "\u0000")))

(defn ^SynonymMap make-syn-map [nyms]
  (let [m (SynonymMap$Builder. true)]
    (doseq [[from to] nyms]
      (.add m (make-ref from) (make-ref to) true))
    (.build m)))

(def ^:constant ^SynonymMap default-synonyms (make-syn-map nyms))

(defn make-analyzer [& [^SynonymMap synonyms]]
  (proxy [StopwordAnalyzerBase] [clucy/*version*]
    (createComponents [fieldName reader]
      (let [src (StandardTokenizer. clucy/*version* reader)
            tok (-> (LowerCaseFilter. clucy/*version*
                                      (StandardFilter. clucy/*version* src))
                    (SynonymFilter. (or synonyms default-synonyms) true)
                    (PorterStemFilter.))]
        (Analyzer$TokenStreamComponents. src tok)))))

(def standard-analyzer (make-analyzer))

(defmacro with-analyzer [analyzer & body]
  `(binding [clucy/*analyzer* ~analyzer]
     ~@body))

(defmacro with-synonyms [nyms & body]
  `(with-analyzer (make-analyzer (make-syn-map ~nyms))
     ~@body))

(defmacro with-standard-analyzer [& body]
  `(with-analyzer standard-analyzer
     ~@body))
