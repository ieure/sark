#! /usr/bin/env python
# -*- coding: utf-8 -*-
#
# © 2014 Buster Marx, Inc All rights reserved.
# Author: Ian Eure <ian.eure@gmail.com>
#

from bs4 import BeautifulSoup
from itertools import imap
import json
import re
import sys

def load():
    with open('pins.html', 'r') as fd:
        return BeautifulSoup(fd.read())

WHITELIST = set(['Sega', 'Stern', 'Bally', 'Alvin G', 'Gottlieb',
                 'Capcom/Pinball Mfg. Inc.', 'Fabulous Fantasies',
                 'Williams', 'Data East', 'Petaco', 'Capcom',
                 'International Concepts', 'Mr. Pinball Australia',
                 'Inder', 'Sega/Stern', 'Geiger'])
def game(row):
    """Return (abbv, name) for this row, or None if it is not a game."""
    if row.attrs != {u'bgcolor': u'#FFFFBB', u'align': u'center', u'valign': u'middle'}:
        return None

    cells = row.find_all('td')
    if clean(cells[6].text) not in WHITELIST:
        return None

    name = clean(cells[2].text)
    if 'redemption' in name:
        return None

    return (clean(cells[0].text), name)


WSRE = re.compile(r'\s+')
def clean(text):
    return WSRE.sub(" ", text).strip()


def main():
    soup = load()
    rows = soup.find_all("tr")
    json.dump(filter(None, imap(game, rows)), sys.stdout)

if __name__ == '__main__':
    main()
