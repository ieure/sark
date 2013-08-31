/**
 *
 * eeeee eeeee eeeee  e   e
 * 8   " 8   8 8   8  8   8
 * 8eeee 8eee8 8eee8e 8eee8e
 *    88 88  8 88   8 88   8
 * 8ee88 88  8 88   8 88   8
 *
 * © Copyright 2013 Ian Eure.
 * Author: Ian Eure <ian.eure@gmail.com>
 */

var s = document.getElementById("s");
var results = document.getElementById("results");

function debounce(delay, handler) {
    var timer;
    return function(event) {
        if (timer) {
            window.clearTimeout(timer);
        }
        timer = setTimeout(handler, delay, event);
    };
};

function hideLabel() {
    if (s.value != "") {
        $("label").hide();
    } else {
        $("label").show();
    }
};

function parseSearch(searchString) {
    var ss = searchString.substring(1).split("&")
    var so = {}
    for (var i in ss) {
        var ssp = ss[i].split("=")
        so[ssp[0]] = ssp[1];
    }
    return so;
}

function initialize() {
    $(s).bind("keydown", hideLabel).bind("keyup", hideLabel)
        .bind("keyup", debounce(50, searchEvent))
        .focus();

    $("form").bind("submit", searchEvent);

    if (window.location.search != "") {
        var ss = parseSearch(window.location.search);
        if (ss["s"] != "") {
            s.value = ss["s"];
            hideLabel();
            searchFor(s.value);
        }
    }
};


// Searching

/**
 * Handler for a search event.
 *
 * Updates title / state and fires off the search.
 */
function searchEvent(event) {
    event.stopPropagation();
    searchFor(s.value);
    return false;
};

function searchSuccess(data, status, xhr) {
    if (data.length == 0) {
        $(results).addClass("nil");
        return;
    }

    $(results).empty().hide().removeClass("nil");
    for (i in data) {
        $(results).append("<li><a target=\"_new\" href=\"" + data[i]["url"] + "\">" + data[i]["name"] + "</a>")
    };
    $(results).show();
    document.title = s.value + " - Sark";
    history.pushState({}, document.title, "?s=" + s.value);
};

function searchError() {};

/**
 * Perform a search
 */
function searchFor(text) {
    jQuery.ajax("/s?q=" + text,
                {"async": false,
                 "success": searchSuccess,
                 "error": searchError});
};

function clearSearch() {
    s.value = "";
    document.getElementById("search").style.display = "none";
    showLabel();
    s.focus();
};

initialize();
