let add_movie_form = $("#add_movie_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleAddMovieResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle add movie response");
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
function submitAddMovieForm(formSubmitEvent) {
    console.log("submit add movie form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();


    // Change the api link
    $.ajax(
        "api/add-movie", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: add_movie_form.serialize(),
            success: handleAddMovieResult
        }
    );
}

// Bind the submit action of the form to a handler function
add_movie_form.submit(submitAddMovieForm);

