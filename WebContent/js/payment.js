//---[ Main Function ]---------------------------------------------------------
function main() {
    fillSubtotal();
}
//---[ Main Function ]---------------------------------------------------------



//---[ Payment Functions ]-----------------------------------------------------
function sendPayment() {
    const FUNC = "sendPayment";
    logToConsole(FUNC, "Entered function");

    let customerInfo = {
        "firstName": $("#first-name-input").val(),
        "lastName": $("#last-name-input").val(),
        "creditCard": $("#credit-card-input").val(),
        "expirationDate": $("#expiration-date-input").val(),
    }
    logObjToConsole(FUNC, "Customer data harvested to customerInfo:", customerInfo);

    logToConsole(FUNC, "Sending payload to PaymentServlet");
    let payload = {
        dataType: "JSON",
        url:      getPaymentUrl(customerInfo),
        method:   "POST",

        success: (resObj) => {
            logToConsole(FUNC, "Entered success. Entering verifyPayment()");
            verifyPayment(resObj);
        },
        error: (resObj) => {
            logToConsole(FUNC, "Entered error. Alert and page reload.");
            alert("Something went wrong. Please Try again.");
            // location.reload();
        }
    }
    $.ajax(payload);
}

function getPaymentUrl(custObj) {
    let url = "api/payment";
    url += `?firstName=${custObj.firstName}`;
    url += `&lastName=${custObj.lastName}`;
    url += `&creditCard=${custObj.creditCard}`;
    url += `&expirationDate=${custObj.expirationDate}`;
    return url;
}
//---[ Payment Functions ]-----------------------------------------------------


//---[ Info Display Functions ]------------------------------------------------
function fillSubtotal() {
    let subtotalNode = $(".subtotal-field");

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
//---[ Info Display Functions ]------------------------------------------------



//---[ Callback Functions ]----------------------------------------------------
function handleCartResults(resObj) {
    const FUNC = "handleCartResults";
    logToConsole(FUNC, "Entered method. Cart object received:");
    console.log(resObj);

    let totalPrice = resObj["total"];
    logToConsole(FUNC, "Subtotal gotten: " + totalPrice);

    let subtotalNode = $(".subtotal-field");
    subtotalNode.text(`\$${totalPrice}.00`);
}

function verifyPayment(resObj) {
    if (resObj["errorMessage"]) {
        console.log(resObj["errorMessage"]);
        alert(resObj["errorMessage"]);
        location.reload();
    }
    else {
        alert("Successful Order!");
    }
}
//---[ Callback Functions ]----------------------------------------------------



//---[ Logging ]---------------------------------------------------------------
function logToConsole(func, msg) {
    const MODULE = "payment";
    console.log(`${MODULE} - ${func}: ${msg}`);
}

function logVarToConsole(func, msg, val) {
    const MODULE = "payment";
    console.log(`${MODULE} - ${func}: ${msg}: ${val}`);
}

function logObjToConsole(func, msg, obj) {
    const MODULE = "payment";
    console.log(`${MODULE} - ${func}: ${msg}`);

    for (const [k, v] of Object.entries(obj)) {
        console.log(`${k}: ${v}`);
    }
}
//---[ Logging ]---------------------------------------------------------------

main();
