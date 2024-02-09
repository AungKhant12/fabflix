function handleStarResult(resultData) {
    logToConsole("index", "handleStarResult", "Entered function")
    let starTableBodyElement = jQuery("#star_table_body");

    for (let i = 0; i < Math.min(10, resultData.length); i++) {
        let starName = resultData[i]["star_name"];
        let starId = resultData[i]['star_id'];
        let starAnchorHref = `single-star.html?id=${starId}`;
        let starDob = resultData[i]["star_dob"];

        let rowHTML = "<tr>";
        rowHTML += `<th><a href=\"${starAnchorHref}\">${starName}</a></th>`;
        rowHTML += `<th>${starDob}</th>`;
        rowHTML += "</tr>";

        starTableBodyElement.append(rowHTML);
    }

    logToConsole("index", "handleStarResult", "Appended all row children to stars table body");
}

function logToConsole(module, func, msg) { console.log(`${module} - ${func}: ${msg}`); }

let payload = {
    dataType: "json",
    method:   "GET",
    url:      "api/stars",
    success: (resultData) => handleStarResult(resultData)
};
logToConsole("index", "global scope", "Sending payload to StarsServlet");
jQuery.ajax(payload);