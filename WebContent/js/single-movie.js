//---[ Main Function ]---------------------------------------------------------
function main() {
    let movieId = getParameterByName("id");

    let payload = {
        dataType: "json",
        method:   "GET",
        url:      `api/single-movie?id=${movieId}`,
        success:  (resData) => { handleMovieData(resData); }
    };
    jQuery.ajax(payload);
}
//---[ Main Function ]---------------------------------------------------------


function getParameterByName(targetParam) {
    let url = window.location.href;
    targetParam = targetParam.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + targetParam + "(=([^&#]*)|&|#|$)");
    let results = regex.exec(url);

    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleMovieData(resArr) {
    logToConsole("single-movie", "handleMovieData", "Entered function");
    let resObj = resArr[0];
    console.log(resObj);

    const movieTitleContainerNode = jQuery(".movie-title-container");
    const directorContainerNode   = jQuery(".director-container");
    const ratingContainerNode     = jQuery(".rating-container");
    const genresTextNode          = jQuery(".genres-text");
    const starsContainerNode      = jQuery(".stars-container");

    const addToCartButtonNode     = $(".add-to-cart-button");

    let movieTitle    = resObj["movie_title"];
    let movieId       = resObj["movie_id"];
    let movieYear     = resObj["movie_year"];
    let movieRating   = resObj["movie_rating"];
    let movieDirector = resObj["movie_director"];
    let genreNameList = resObj["genre_name_list"].split(',');
    let genreIdList   = resObj["genre_id_list"].split(',');
    let starNameList  = resObj["star_name_list"].split(',');
    let starIdList    = resObj["star_id_list"].split(',');

    document.title = movieTitle;

    let buttonHtml = makeAddToCartButton(movieId, movieTitle);
    console.log(buttonHtml);
    logToConsole("single-movie", "handleMovieData", "adding button");
    console.log(movieId);
    addToCartButtonNode.append(buttonHtml);

    movieTitleContainerNode.append(`<p class="movie-title-text">${movieTitle} (${movieYear})</p>`);
    ratingContainerNode.append(`<p class="rating-text">${movieRating}</p>`);
    directorContainerNode.append(`<p class="director-name">${movieDirector}</p>`);

    // hydrating genres HTML
    let htmlStr = "";
    for (let i = 0; i < genreNameList.length; i++) {
        htmlStr = `<a href="index.html?genreId=${genreIdList[i]}"><button class='genre-button'>${genreNameList[i]}</button></a>`;
        genresTextNode.append(htmlStr);
    }

    // hydrating stars HTML
    htmlStr = "<div class='stars-text'>";
    for (let i = 0; i < starNameList.length; i++) {
        let starHref = `single-star.html?id=${starIdList[i]}`;
        htmlStr += `<a href="${starHref}">${starNameList[i]}</a>`;
        if (i < starNameList.length - 1) { htmlStr += " &#183 "; }
    }
    htmlStr += "</div>";
    starsContainerNode.append(htmlStr);
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


//---[ Logging ]---------------------------------------------------------------
function logToConsole(module, func, msg) {
    const MODULE = "single-movie";
    console.log(`${MODULE} - ${func}: ${msg}`);
}

//---[ Logging ]---------------------------------------------------------------


//---[ Entry ]-----------------------------------------------------------------
main();
//---[ Entry ]-----------------------------------------------------------------
