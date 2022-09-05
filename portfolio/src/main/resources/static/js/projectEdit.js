$(() => {

    let projectName = $("#projectName")
    let projectStart = $("#projectStartDate")
    let projectEnd = $("#projectEndDate")
    let projectId = $("#projectId")
    let projectDescription = $("#projectDescription")

    // The following two chunks of code are related to the date inputs
    // They check that the projectStart or projectEnd are not the wrong way (start after end etc)
    projectStart.on("change", () => checkDateOrder(projectStart.val(), projectEnd.val()))


    projectEnd.on("change", () => checkDateOrder(projectStart.val(), projectEnd.val()))


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