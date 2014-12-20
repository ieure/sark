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
            [clucy.core :as clucy]))

(def inames
  ["PDF_Arcade_Atari_Kee/Tempest/Tempest_Troubleshooting_Guide_TM-195_1st Printing.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-2nd-02A.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-1st-03B.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-4th-03A.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-1st-01A.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_Troubleshooting_Guide_TM-195_2nd_Printing.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-2nd-01A.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-2nd-03A.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_ST-190_3rd_Printing_Backdoor_Sheet.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_TM-190_2nd_Printing.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_Bulletin_Bug_40_Credits.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-2nd-02B.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Thumbs.db"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-1st-01B.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_Bulletin_Skill_Step.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_CO-190-01_New_Roms.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-1st-02B.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-2nd-03B.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-1st-02A.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_TM-190_1st_Printing.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-1st-03A.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-2nd-01B.pdf"
   "PDF_Arcade_Atari_Kee/Tempest/Tempest_DP-190-4th-03B.pdf"])

(deftest test-synonyms
  (testing "Searching for schematics works."
    (let [i (anal/with-synonyms {"dp" "schematic"
                                 "tm" "manual"}
              (sark/build-index inames))]
      (is (re-find #"Tempest DP"
                      (:name (first (sark/explain i "tempest schematic")))))))

  (testing "Searching for schematics with the default analyzer works."
    (let [i (anal/with-standard-analyzer
              (sark/build-index inames))
          res (sark/explain i "tempest schematic")]
      (is (re-find #"Tempest DP" (or (:name (first res)) "")))))

  (testing "Searching for schematics in the default index works"
    (let [res (sark/search @sark/index "tempest schematic")]
      (is (re-find #"Tempest DP" (or (:name (first res)) "")))))

  (testing "Searching for `dk manual' returns donkey kong hits"
    (let [res (sark/search @sark/index "dk manual")]
      (is (re-find #"(?i)donkey kong" (or (:name (first res)) ""))))))

;; Init once
(sark/init-cache!)
