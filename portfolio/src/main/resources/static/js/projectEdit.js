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

        if (projectStart.val() < projectStart[0].min) {
            const minDateFormatted = new Date(projectStart[0].min).toLocaleDateString();
            projectStart[0].setCustomValidity("Date must be less than a year ago. Please select a date later than " + minDateFormatted);
        }
    })

    projectEnd.on("change", () => {
        checkDateOrder(projectStart.val(), projectEnd.val())
        if (projectEnd.val() < projectEnd[0].min) {
            const minDateFormatted = new Date(projectEnd[0].min).toLocaleDateString();
            projectEnd[0].setCustomValidity("There are sprints that end after that date. Please select a date after " + minDateFormatted);
        }
    })

    projectDescription.on("input", () => {
        let descText = projectDescription.val().toString()
        if (GENERAL_UNICODE_REGEX.test(descText)) {
            projectDescription[0].setCustomValidity("");
        } else {
            projectDescription[0].setCustomValidity("Project description can only contain unicode letters, numbers, punctuation, symbols (but not emojis) and whitespace");
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