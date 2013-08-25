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
        timer = setTimeout(handler, delay);
    };
};

function initialize() {
    // s.onkeydown = function(event) {
    //     var state = getState();
    //     hideLabel();
    // };

    $(s).focus();

    s.onkeyup = searchEvent;

    var form = document.getElementsByTagName("form")[0];
    form.onsubmit = searchEvent;
};

function flush(node) {
    while (node.childNodes.length >= 1) {
        node.removeChild(node.firstChild);
    }
};

function showLabel() {
    document.getElementsByTagName("label")[0].style.display = "block";
}

function hideLabel() {
    document.getElementsByTagName("label")[0].style.display = "none";
}


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
        $(results).append("<li><a href=\"" + data[i]["url"] + "\">" + data[i]["name"] + "</a>")
    };
    $(results).show();
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
