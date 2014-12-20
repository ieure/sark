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
(ns sark.core-test
  (:use [clojure.test]
        [clojure.pprint])
  (:require [sark.core :as sark]
            [sark.analyzer :as anal]
            [clucy.core :as clucy])
  (:import [org.apache.lucene.analysis.synonym SynonymFilter SynonymMap$Builder]
           [org.apache.lucene.util CharsRef]))

(deftest test-searches
  (testing "Searching for schematics works."
    (let [res (sark/search @sark/index "tempest schematic")]
      (is (re-find #"Tempest DP" (or (:name (first res)) "")))))

  (testing "Searching for `dk manual' returns Donkey Kong hits"
    (let [res (sark/search @sark/index "dk manual")]
      (is (re-find #"(?i)donkey kong" (or (:name (first res)) "")))))

  (testing "Searching for `dkjr manual' returns Donkey Kong Junior hits"
    (let [res (sark/search @sark/index "dkjr manual")]
      (is (re-find #"(?i)donkey kong jr" (or (:name (first res)) "")))))

;; Init once
(sark/init-cache!)
