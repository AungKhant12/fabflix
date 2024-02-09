//---[ Main Function ]---------------------------------------------------------
function main() {
    // Populate Genres & Titles
    populateGenreButtons()
    populateTitleLetters();

    addFulltextSearch();
    addAutocomplete();
}
//---[ Main Function ]---------------------------------------------------------

//---[ Functions ]-------------------------------------------------------------
function populateGenreButtons() {
    let FUNC = "populateGenreButtons";
    logToConsole(FUNC, "Sending payload to FetchGenresServlet");

    let genrePayload = {
        datatype: "json",
        method:   "GET",
        url:      "api/fetch-genres",
        success:  (resData) => handleGenreResult(resData)
    };
    $.ajax(genrePayload);
}

function populateTitleLetters() {
    let FUNC = "populateTitleLetters";
    logToConsole(FUNC, "Populating title letters");

    let startLetterContainerNode = $(".start-letter-container");
    let moviePageURL = "index.html";

    const CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*"

    // <a href="index.html?start={letter}">{letter}</a>
    for (let i = 0; i < CHARS.length; i++) {
        let letter = CHARS[i];
        let hrefStr = `${moviePageURL}?start=${letter}`;
        let newAnchorNode = $(`<a href="${hrefStr}">${letter}</a>`);

        startLetterContainerNode.append(newAnchorNode);

        if (i < CHARS.length - 1) {
            let pipeSpanNode = $("<span> | </span>");
            startLetterContainerNode.append(pipeSpanNode);
        }
    }
}

function addFulltextSearch() {
    const inputNode = $(".movie-title-fulltext-search");
    inputNode.keypress((event) => { bindEnterToSearch(event, inputNode); });
}

function addAutocomplete() {
    const FUNC = "addAutocomplete";
    const inputNode = $("#movie-title-fulltext-search");

    logToConsole(FUNC, "Binding autocomplete to input box");
    const DELAY_IN_MS = 300;
    const CHARS_UNTIL_AUTOCOMPLETE = 3;

    let autocompleteObj = {
        // documentation of lookup function found under "Custom lookup function" section
        lookup: (query, doneCallback) => {
            handleLookup(query, doneCallback);
        },
        onSelect: (suggestion) => {
            handleSelectSuggestion(suggestion);
        },
        deferRequestBy: DELAY_IN_MS,

        // may use some other parameters to satisfy all requirements
        // TODO: add other parameters, such as minimum characters
        minChars: CHARS_UNTIL_AUTOCOMPLETE,
    };
    inputNode.autocomplete(autocompleteObj);
    logToConsole(FUNC, "Finished binding autocomplete to input box");
}

function bindEnterToSearch(event, inputNode) {
    const FUNC = "bindEnterToSearch";
    const ENTER_KEYCODE = 13;

    if (event.keyCode === ENTER_KEYCODE) {
        logToConsole(FUNC, "Key pressed on input element: [Enter]");
        let inputVal = inputNode.val();

        logToConsole(FUNC, "Handling normal search");
        handleNormalSearch(inputVal);
    }
}

//---[ Functions ]-------------------------------------------------------------


//---[ Callback Functions ]----------------------------------------------------
function handleGenreResult(resArr) {
    const FUNC = "handleGenreResult";
    logToConsole(FUNC, "received results from FetchGenresServlet:");
    console.log(resArr);

    let genreContainerNode = $(".genre-container");
    let moviePageURL = "index.html";

    for (let i = 0; i < resArr.length; i++) {
        let resObj = resArr[i];
        let genreName = resObj["genre_name"];
        let genreId   = resObj["genre_id"];

        let hrefStr = `${moviePageURL}?genreId=${genreId}`;
        let newAnchorNode = $(`<a href="${hrefStr}"><button class="genre-button">${genreName}</button></a>`);

        genreContainerNode.append(newAnchorNode);
    }
}

function handleNormalSearch(query) {
    const FUNC = "handleNormalSearch";
    logToConsole(FUNC, `Executing normal search: ${query}`);

    // reload window to movie search page w/ query
    let targetUrl = `index.html?query=${query}`;
    window.location.assign(targetUrl);
}

function handleLookup(query, doneCallback) {
    const FUNC = "handleLookup";
    logToConsole(FUNC, "Autocomplete initiated");

    // TODO: if you want to check past query results first, you can do it here
    let prevSuggestionList = window.sessionStorage.getItem(query);
    if (prevSuggestionList) {
        logToConsole(FUNC, "Previous query found. Fetched results from local cache");
        prevSuggestionList = JSON.parse(prevSuggestionList);

        let suggestionsObj = {
            suggestions: prevSuggestionList,
        };
        doneCallback(suggestionsObj);

        return;
    }

    let targetUrl = `api/title-suggestion?query=${escape(query)}`;
    let payload = {
        "url":     targetUrl,
        "method":  "GET",
        "success": (data) => {
            logToConsole(FUNC, "Successful GET request from TitleSuggestionServlet");
            handleLookupAjaxSuccess(data, query, doneCallback);
        },
        "error":   (errorData) => {
            logToConsole(FUNC, "Error - Unable to fulfill AJAX look up");
            console.log(errorData);
        }
    };
    logToConsole(FUNC, `Sending GET request to TitleSuggestionServlet through: ${targetUrl}`);
    jQuery.ajax(payload);
    logToConsole(FUNC, `Finished handling response from TitleSuggestionServlet`);
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    const FUNC = "handleLookupAjaxSuccess";
    logToConsole(FUNC, "Successful AJAX lookup");

    // TODO: if you want to cache the result into a global variable you can do it here
    window.sessionStorage.setItem(query, JSON.stringify(data));

    // - autocomplete's doneCallback()
    //   - expects { suggestions: jsonData } object as parameter (detailed in "Response Format")
    let suggestionsObj = {
        suggestions: data,
    };
    doneCallback(suggestionsObj);
}

function handleSelectSuggestion(suggestion) {
    const FUNC = "handleSelectSuggestion";

    // TODO: jump to the specific result page based on the selected suggestion
    movieId = suggestion["data"]["movie_id"];
    logToConsole(FUNC, `Movie id selected: ${movieId}`);

    targetUrl = `single-movie.html?id=${movieId}`;
    window.location.assign(targetUrl);
}

//---[ Callback Functions ]----------------------------------------------------


//---[ Logging ]---------------------------------------------------------------
function logToConsole(func, msg) {
    const MODULE = "main-page";
    if (DEBUG_TOGGLE)
        console.log(`${MODULE} - ${func}: ${msg}`);
}

//---[ Logging ]---------------------------------------------------------------

//---[ Entry ]-----------------------------------------------------------------
const DEBUG_TOGGLE = true;
main();
//---[ Entry ]-----------------------------------------------------------------
