$(() => {

    let projectName = $("#projectName")
    let projectStart = $("#projectStartDate")
    let projectEnd = $("#projectEndDate")
    let projectId = $("#projectId")
    let projectDescription = $("#projectDescription")

    projectStart.on("change", () => {
        checkDateOrder(projectStart.val(), projectEnd.val())
        if (projectStart.val() > projectStart[0].max) {
            const maxDateFormatted = new Date(projectStart[0].max).toLocaleDateString();
            projectStart[0].setCustomValidity("There are sprints that start before that date. Please select a date earlier than " + maxDateFormatted);
        }
    })

    projectEnd.on("change", () => {
        checkDateOrder(projectStart.val(), projectEnd.val())
        if (projectEnd.val() < projectEnd[0].min) {
            const minDateFormatted = new Date(projectEnd[0].min).toLocaleDateString();
            projectEnd[0].setCustomValidity("There are sprints that end before that date. Please select a date after " + minDateFormatted);
        }
    })

    //When the submit button is clicked on the form.
    $(".editForm").on("submit", function (event) {
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