$(() => {

    let sprintId = $("#sprintId")
    let sprintName = $("#sprintName")
    let sprintStartDate = $("#sprintStartDate")
    let sprintEndDate = $("#sprintEndDate")
    let sprintDescription = $("#sprintDescription")
    let sprintColour = $("#sprintColour")

    // Checks when the start date changes that its not past the end date.
    sprintStartDate.on("change", () => checkDateOrder(sprintStartDate.val(), sprintEndDate.val()))


    sprintEndDate.on("change", () => checkDateOrder(sprintStartDate.val(), sprintEndDate.val()))

    sprintDescription.on("input", () => {
        let descText = sprintDescription.val().toString()
        if (GENERAL_UNICODE_REGEX.test(descText)) {
            sprintDescription[0].setCustomValidity("");
        } else {
            sprintDescription[0].setCustomValidity("Sprint description can only contain unicode letters, numbers, punctuation, symbols (but not emojis) and whitespace");

        }
    })


    // When submit button is clicked on sprint edit form
    $(".editForm").on("submit", function (event) {
        event.preventDefault()

        let dataToSend = {
            "sprintId": sprintId.val(),
            "sprintName": sprintName.val(),
            "sprintStartDate": sprintStartDate.val(),
            "sprintEndDate": sprintEndDate.val(),
            "sprintDescription": sprintDescription.val(),
            "sprintColour": sprintColour.val()
        }

        $.ajax({
            url: "sprintSubmit",
            type: "post",
            data: dataToSend,
            success: function () {
                sendNotification("sprint", sprintId.val(), "update")
                window.history.back();
            },
            error: function (error) {
                createAlert(error.responseText, "failure")
            }
        })
    })
})