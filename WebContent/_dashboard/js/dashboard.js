function main() {
    const FUNC = "main";
    logToConsole(FUNC, "Entered method");

    let payloadUrl = "api/fetch-metadata";
    let payload = {
        datatype: "json",
        method:   "GET",
        url:      payloadUrl,

        success: (resArr) => { populateDashboard(resArr); }
    };
    logToConsole(FUNC, "Sending payload to api/fetch-metadata");
    $.ajax(payload);
}

function populateDashboard(resArr) {
    const FUNC = "populateDashboard";
    logToConsole(FUNC, "Entered method");

    console.log(resArr);

    const tablesContainerNode = $(".tables-container");

    let htmlStr = "";

    for (let i = 0; i < resArr.length; i++) {
        let resObj = resArr[i];
        let tableName = resObj["table_name"];
        let tableDataArr = resObj["table_metadata"];

        htmlStr += `<table>${tableName}`;
        for (let j = 0; j < tableDataArr.length; j++) {
            let tableRowObj = tableDataArr[j];
            let rowAttr = tableRowObj["attribute"];
            let rowType = tableRowObj["type"];

            htmlStr += `<tr><td>${rowAttr}</td>`;
            htmlStr += `<td>${rowType}</td></tr>`;
        }
        htmlStr += `</table><br>`;
    }
    tablesContainerNode.append(htmlStr);
}


//---[ Utility Functions ]-----------------------------------------------------
function logToConsole(func, msg) {
    const MODULE = "dashboard.js";
    console.log(`${MODULE} - ${func}: ${msg}`);
}
//---[ Utility Functions ]-----------------------------------------------------

//---[ Entry ]-----------------------------------------------------------------
main();
//---[ Entry ]-----------------------------------------------------------------