$(() => {

    let projectName = $("#projectName")
    let projectStart = $("#projectStartDate")
    let projectEnd = $("#projectEndDate")
    let projectId = $("#projectId")
    let projectDescription = $("#projectDescription")

    // The following two chunks of code are related to the date inputs
    // They check that the projectStart or projectEnd are not the wrong way (start after end etc)
    projectStart.on("change", checkDateOrder)


    projectEnd.on("change", checkDateOrder)


    //When the submit button is clicked on the form.
    $(".projectEditForm").on("submit", function (event) {
        event.preventDefault()

        let dataToSend = {
            "projectId": projectId.val(),
            "projectName": projectName.val(),
            "projectStartDate": projectStart.val(),
            "projectEndDate": projectEnd.val(),
            "projectDescription": projectDescription.val()
        }
        $.ajax({
            url: "projectEdit",
            data: dataToSend,
            type: "post",
            success: function () {
                location.href = "portfolio?projectId=" + projectId.val()
            },
            error: function (error) {
                createAlert(error.responseText, "failure")
            }
        })
    })
})

/**
 * Compares the values of the start and end date inputs, and if they are invalid (the start date after the end date),
 * displays an error message
 */
function checkDateOrder() {
    let dateAlert = $(".dateAlert")
    let projectStart = $("#projectStartDate").val()
    let projectEnd = $("#projectEndDate").val()
    if (projectStart >= projectEnd) {
        dateAlert.slideDown()
        $(".canDisable").attr("disabled", true)
        $(".date").addClass("is-invalid")
    } else {
        $(".canDisable").attr("disabled", false)
        $(".date").removeClass("is-invalid")
        dateAlert.slideUp();
    }
}