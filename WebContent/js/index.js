//---[ Main Function ]---------------------------------------------------------
function main() {
    const FUNC = "main";

    // get parameters sent to movie list page
    let pageParams = {
        "movieTitleParam":    getParameterByName("movieTitle"),
        "movieYearParam":     getParameterByName("movieYear"),
        "movieDirectorParam": getParameterByName("movieDirector"),
        "starNameParam":      getParameterByName("starName"),
        "genreParam":         getParameterByName("genreId"),
        "startParam":         getParameterByName("start"),
        "pageParam":          getParameterByName("page"),
        "limitParam":         getParameterByName("limit"),
        "sortPriorityParam":  getParameterByName("sortPriority"),
        "ratingOrderParam":   getParameterByName("ratingOrder"),
        "titleOrderParam":    getParameterByName("titleOrder"),
        "queryParam":         getParameterByName("query"),
    }
    setDefaultParameter(pageParams);
    logVarToConsole(FUNC, "Page parameters set:", pageParams);

    // set page's pagination
    addPagination(pageParams);

    // call servlet based on URL parameters
    let payload = setPayload(pageParams);
    jQuery.ajax(payload);

    addFulltextSearch();
    addAutocomplete();
}
//---[ Main Function ]---------------------------------------------------------


//---[ Functions ]-------------------------------------------------------------
function addPagination(paramsObj) {
    const FUNC = "addPagination";
    logToConsole(FUNC, "Entered function");

    logToConsole(FUNC, "Parameters gotten:");
    console.log(paramsObj);

    // gets integer pageNum
    let pageNum = 1;
    if (paramsObj["pageParam"] !== null) { pageNum = parseInt(paramsObj["pageParam"]); }
    if (paramsObj["pageParam"] < 1) { pageNum = 1; }
    logToConsole(FUNC, `pageNumStr: ${paramsObj["pageParam"]}, pageNum: ${pageNum}`);

    // assign to sort param, if needed
    if (paramsObj["sortParam"] === null) { paramsObj["sortParam"] = "desc"; }
    else if (paramsObj["sortParam"] !== "desc" || paramsObj["sortParam"] !== "asc") { paramsObj["sortParam"] = "desc"; }
    logToConsole(FUNC, `sort param: ${paramsObj["sortParam"]}`);


    // adds page navigation buttons
    const pageContainerNode = $(".pagination-container");

    let pageDecAnchorNode = null;
    let pageIncAnchorNode = null;

    let pageHref = "";
    if (paramsObj["queryParam"]) { pageHref += `&query=${paramsObj["queryParam"]}`; }
    if (paramsObj["movieTitleParam"]) { pageHref += `&movieTitle=${paramsObj["movieTitleParam"]}`; }
    if (paramsObj["movieYearParam"]) { pageHref += `&movieYear=${paramsObj["movieYearParam"]}`; }
    if (paramsObj["movieDirectorParam"]) { pageHref += `&movieDirector=${paramsObj["movieDirectorParam"]}`; }
    if (paramsObj["starNameParam"]) { pageHref += `&starName=${paramsObj["starNameParam"]}`; }
    if (paramsObj["genreParam"]) { pageHref += `&genreId=${paramsObj["genreParam"]}`; }
    if (paramsObj["startParam"]) { pageHref += `&start=${paramsObj["startParam"]}`; }
    if (paramsObj["limitParam"]) { pageHref += `&limit=${paramsObj["limitParam"]}`; }
    if (paramsObj["sortPriorityParam"]) { pageHref += `&sortPriority=${paramsObj["sortPriorityParam"]}`; }
    if (paramsObj["ratingOrderParam"]) { pageHref += `&ratingOrder=${paramsObj["ratingOrderParam"]}`; }
    if (paramsObj["titleOrderParam"]) { pageHref += `&titleOrder=${paramsObj["titleOrderParam"]}`; }

    logToConsole(FUNC, "Added pagination");
    if (pageNum > 1) {
        let pageHrefLeft = `index.html?page=${pageNum - 1}` + pageHref;

        pageDecAnchorNode = $(`<a href='${pageHrefLeft}'>\<</a>`);
        pageContainerNode.append(pageDecAnchorNode);
    }
    let pageHrefRight = `index.html?page=${pageNum + 1}` + pageHref;

    pageIncAnchorNode = $(`<a href='${pageHrefRight}'>\></a>`);
    pageContainerNode.append(pageIncAnchorNode);

    logToConsole(FUNC, "Pagination added");
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
function handleMovieResult(resultData) {
    const FUNC = "handleMovieResult";
    logToConsole(FUNC, "entered callback function w/ results:");
    console.log(resultData);

    let movieTableBodyNode = jQuery("#movie_table_body");

    for (let i = 0; i < resultData.length; i++) {
        let resObj = resultData[i];

        let movieId  = resObj["movie_id"];
        let title    = resObj["movie_title"];
        let year     = resObj["movie_year"];
        let director = resObj["movie_director"];
        let rating   = resObj["movie_rating"];

        let genreNameList = resObj["genre_name_list"].split(', ');
        let genreIdList   = resObj["genre_id_list"].split(', ');
        let starNameList  = resObj["star_name_list"].split(',');
        let starIdList    = resObj["star_id_list"].split(',');

        let movieTitleHref = `single-movie.html?id=${movieId}`;
        let starLink  = "";
        let genreLink = "";

        let tableRowHtml = "<tr>";

        tableRowHtml += `<td><a href="${movieTitleHref}">${title}</a></td>`;
        tableRowHtml += `<td>${year}</td>`;
        tableRowHtml += `<td>${director}</td>`;

        // hydrate stars data cell
        tableRowHtml += "<td>";
        for (let i = 0; i < starNameList.length; i++) {
            starLink = `single-star.html?id=${starIdList[i]}`;
            tableRowHtml += `<a href="${starLink}">${starNameList[i]}</a>`;

            if (i < starNameList.length - 1) {
                tableRowHtml += ', ';
            }
        }
        tableRowHtml += "</td>";

        // hydrate genres data cell
        tableRowHtml += "<td>";
        for (let g = 0; g < genreNameList.length; g++) {
            genreLink = `index.html?genreId=${genreIdList[g]}`;
            // tableRowHtml += `<button class="genre-button"><a href="${genreLink}">${genreNameList[g]}</a></button>`;
            tableRowHtml += `<a href="${genreLink}"><button class="genre-button">${genreNameList[g]}</button></a>`;
        }
        tableRowHtml += "</td>";

        tableRowHtml += `<td>${rating}</td>`;

        tableRowHtml += `<td>${makeAddToCartButton(movieId, title)}</td>`

        tableRowHtml += "</tr>";
        movieTableBodyNode.append(tableRowHtml);
    }
}

function makeAddToCartButton(movieId, movieTitle) {
    let buttonHtml = `<button onclick='addMovieToCart("${movieId}", "${movieTitle}")'>Add to Cart</button>`;
    return buttonHtml;
}

function addMovieToCart(mId, mTitle) {
    const FUNC = "addMovieToCart";

    let ADD_ACTION = "1";
    let addUrl = `api/Cart?action=${ADD_ACTION}&id=${mId}&title=${mTitle}`;

    payload = {
        dataType: "json",
        url:      addUrl,
        method:   "POST",
        success: (resObj) => {
            logToConsole(FUNC, `${mTitle} successfully added to cart!`);
            alert(`${mTitle} successfully added to cart!`);
        }
    }
    $.ajax(payload);
}

function addSortingAndLimit() {
    let url = window.location.url

    s = $("#search-form");


    window.location = url;
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


//---[ Ajax Calling Functions ]------------------------------------------------
function getURLPaginationSorting(paramObj) {
    let urlPageSort = `?`;

    // ?page=1&limit=10&sortPriority=rating&ratingOrder=desc&titleOrder=asc
    urlPageSort += `page=${paramObj["pageParam"]}`;
    urlPageSort += `&limit=${paramObj["limitParam"]}`;
    urlPageSort += `&sortPriority=${paramObj["sortPriorityParam"]}`;
    urlPageSort += `&ratingOrder=${paramObj["ratingOrderParam"]}`;
    urlPageSort += `&titleOrder=${paramObj["titleOrderParam"]}`;

    return urlPageSort;
}

function checkMovieServletConditions(paramObj) {
    return paramObj["movieTitleParam"] || paramObj["movieYearParam"] || paramObj["movieDirectorParam"] || paramObj["starNameParam"];
}

function getMovieSearchUrl(paramObj) {
    let searchUrlStr = `&movieTitle=${paramObj["movieTitleParam"]}`;
    searchUrlStr += `&movieYear=${paramObj["movieYearParam"]}`;
    searchUrlStr += `&movieDirector=${paramObj["movieDirectorParam"]}`;
    searchUrlStr += `&starName=${paramObj["starNameParam"]}`;

    return searchUrlStr;
}

function setPayload(paramObj) {
    const FUNC = "setPayload";

    let urlStem     = "api/movies";
    let urlPageSort = getURLPaginationSorting(paramObj);

    let payload = {
        dataType: "json",
        method:   "GET",
        url:      `${urlStem}${urlPageSort}`,
        success:  (resultData) => handleMovieResult(resultData)
    }

    if (paramObj.queryParam !== "") {
        // api/fulltextsearch?&query=bob
        urlStem = "api/fulltextsearch";
        payload["url"] = `${urlStem}${urlPageSort}&query=${paramObj.queryParam}`;
        logToConsole(FUNC, `Sending payload to FullTextSearchServlet through url:`);
        console.log(`${payload["url"]}`);
    }
    else if (checkMovieServletConditions(paramObj)) {
        urlStem = "api/search";
        let movieParams = getMovieSearchUrl(paramObj);
        payload["url"] = `${urlStem}${urlPageSort}${movieParams}`;
        logToConsole(FUNC, "Sending payload to SearchServlet through url:");
        console.log(payload["url"]);
    }
    else if (paramObj["genreParam"]) {
        urlStem = "api/browse-genres";
        payload["url"] = `${urlStem}${urlPageSort}&genreId=${paramObj["genreParam"]}`;
        logToConsole(FUNC, "Sending payload to BrowseGenreServlet");
    }
    else if (paramObj["startParam"]) {
        urlStem = "api/browse-title";
        payload["url"] = `${urlStem}${urlPageSort}&start=${paramObj["startParam"]}`;
        logToConsole(FUNC, "Sending payload to BrowseTitleServlet");
    }
    else {
        logToConsole(FUNC, "Sending payload to MoviesServlet");
    }

    return payload;
}

//---[ Ajax Calling Functions ]------------------------------------------------


//---[ Utility Functions ]-----------------------------------------------------
function getParameterByName(targetParam) {
    let url = window.location.href;
    targetParam = targetParam.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + targetParam + "(=([^&#]*)|&|#|$)");
    let results = regex.exec(url);

    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function checkMissingStr(str) {
    return str === undefined || str === null || str === "null";
}

function setDefaultParameter(paramObj) {
    let defaultMap = {
        "movieTitleParam":    "",
        "movieYearParam":     "",
        "movieDirectorParam": "",
        "starNameParam":      "",
        "genreParam":         "",
        "startParam":         "",
        "pageParam":          "1",
        "limitParam":         "10",
        "sortPriorityParam":  "rating",
        "ratingOrderParam":   "desc",
        "titleOrderParam":    "asc",
        "queryParam":         "",
    }

    for (const [k, v] of Object.entries(paramObj)) {
        if (checkMissingStr(v)) {
            paramObj[k] = defaultMap[k];
        }
    }
}

//---[ Utility Functions ]-----------------------------------------------------


//---[ Logging Functions ]-----------------------------------------------------
function logToConsole(func, msg) {
    const MODULE = "index.js";
    console.log(`${MODULE} - ${func}: ${msg}`);
}

function logVarToConsole(func, msg, variable) {
    const MODULE = "cart";
    console.log(`${MODULE} - ${func}: ${msg}:`);
    console.log(variable);
}

//---[ Logging Functions ]-----------------------------------------------------


//---[ Entry ]-----------------------------------------------------------------
main();
//---[ Entry ]-----------------------------------------------------------------
