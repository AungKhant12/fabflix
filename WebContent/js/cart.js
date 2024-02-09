//---[ Main Function ]---------------------------------------------------------
function main() {
    const FUNC = "main";
    logToConsole(FUNC, "Entered method");

    displayCart();
}
//---[ Main Function ]---------------------------------------------------------

//---[ Cart Functions ]--------------------------------------------------------
function displayCart() {
    const ACTION_PARAM = "0"
    let cartUrl = `api/Cart?action=${ACTION_PARAM}`;

    let payload = {
        dataType: "json",
        url:      cartUrl,
        method:   "GET",
        success: (resObj) => { handleCartResults(resObj); }
    }
    $.ajax(payload);
}

function makeAddToCartButton(movieId, movieTitle) {
    let buttonHtml = `<button onclick='addMovieToCart("${movieId}", "${movieTitle}")'>Add One</button>`;
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
            location.reload();
        }
    }
    $.ajax(payload);
}

function makeTakeFromCartButton(movieId, movieTitle) {
    let buttonHtml = `<button onclick='takeMovieFromCart("${movieId}", "${movieTitle}")'>Remove One</button>`;
    return buttonHtml;
}

function takeMovieFromCart(mId, mTitle) {
    const FUNC = "takeMovieFromCart";

    let ADD_ACTION = "2";
    let addUrl = `api/Cart?action=${ADD_ACTION}&id=${mId}&title=${mTitle}`;

    payload = {
        dataType: "json",
        url:      addUrl,
        method:   "POST",
        success: (resObj) => {
            logToConsole(FUNC, `${mTitle} successfully removed from cart once!`);
            alert(`${mTitle} successfully removed from cart once!`);
            location.reload();
        }
    }
    $.ajax(payload);
}

function makeDeleteButton(movieId, movieTitle) {
    let buttonHtml = `<button onclick='deleteFromCart("${movieId}", "${movieTitle}")'>Remove from Cart</button>`;
    return buttonHtml;
}

function deleteFromCart(mId, mTitle) {
    const FUNC = "addMovieToCart";

    let ADD_ACTION = "3";
    let addUrl = `api/Cart?action=${ADD_ACTION}&id=${mId}&title=${mTitle}`;

    payload = {
        dataType: "json",
        url:      addUrl,
        method:   "POST",
        success: (resObj) => {
            logToConsole(FUNC, `${mTitle} successfully removed from cart!`);
            alert(`${mTitle} successfully removed from cart!`);
            location.reload();
        }
    }
    $.ajax(payload);
}
//---[ Cart Functions ]--------------------------------------------------------



//---[ Callback Function ]-----------------------------------------------------
function handleCartResults(resObj) {
    const FUNC = "handleCartResults";
    logVarToConsole(FUNC, "Entered method. Cart object received:", resObj);

    let itemsArr = JSON.parse(resObj["items"]);
    let totalPrice = resObj["total"];

    const tBodyNode = $(".cart-table-body");

    // fill table
    for (let i = 0; i < itemsArr.length; i++) {
        let item = itemsArr[i];
        let tableRowHtml = getRowHTML(item);

        tBodyNode.append(tableRowHtml);
    }

    // display subtotal
    const subtotalNode = $(".subtotal");
    subtotalNode.text(`\$${totalPrice}.00`);
}
//---[ Callback Function ]-----------------------------------------------------


//---[ Helper Functions ]------------------------------------------------------
function getRowHTML(item) {
    let title    = item["title"];
    let id       = item["id"];
    let quantity = item["count"];
    let price    = item["price"];

    let tableRowHtml = "<tr>";

    tableRowHtml += `<td>${title}</td>`;
    tableRowHtml += `<td>${quantity}</td>`;
    tableRowHtml += `<td>\$${price}.00</td>`;

    tableRowHtml += `<td>${makeAddToCartButton(id, title)}</td>`;
    tableRowHtml += `<td>${makeTakeFromCartButton(id, title)}</td>`;
    tableRowHtml += `<td>${makeDeleteButton(id, title)}</td>`;

    tableRowHtml += "</tr>";

    return tableRowHtml;
}
//---[ Helper Functions ]------------------------------------------------------


//---[ Logging ]---------------------------------------------------------------
function logToConsole(func, msg) {
    const MODULE = "cart";
    console.log(`${MODULE} - ${func}: ${msg}`);
}

function logVarToConsole(func, msg, variable) {
    const MODULE = "cart";
    console.log(`${MODULE} - ${func}: ${msg}:`);
    console.log(variable);
}
//---[ Logging ]---------------------------------------------------------------


//---[ Entry ]-----------------------------------------------------------------
main();
//---[ Entry ]-----------------------------------------------------------------
