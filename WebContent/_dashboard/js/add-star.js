let add_star_form = $("#add_star_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleAddStarResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle add star response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") {
        $("#response_message").text(resultDataJson["message"]);
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#response_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitAddStarForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();


    // Change the api link
    $.ajax(
        "api/add-star", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: add_star_form.serialize(),
            success: handleAddStarResult
        }
    );
}

// Bind the submit action of the form to a handler function
add_star_form.submit(submitAddStarForm);

