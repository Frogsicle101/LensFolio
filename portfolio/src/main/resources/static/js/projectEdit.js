$(() => {

    let projectName = $("#projectName")
    let projectStart = $("#projectStartDate")
    let projectEnd = $("#projectEndDate")
    let projectId = $("#projectId")
    let projectDescription = $("#projectDescription")
    let projectForm = $("#projectEditForm")

    const checkDates = () => {
        let startErrorDiv = $("#projectStartDateFeedback")
        let endErrorDiv = $("#projectEndDateFeedback")
        if (checkDateOrder(projectStart.val(), projectEnd.val())) {
            startErrorDiv.text("Start date must be before end date.")
            endErrorDiv.text("Start date must be before end date.")
        } else {
            startErrorDiv.text(`Please select a date between ${minStartDate} and ${maxStartDate}`)
            endErrorDiv.text(`Please select a date no sooner than ${minEndDate}`)
        }
    }

    projectStart.on("change", () => {
        checkDates()
        let startErrorDiv = $("#projectStartDateFeedback")
        if (projectStart.val() > maxStartDate) {
            const maxDateFormatted = new Date(projectStart[0].max).toLocaleDateString();
            projectStart[0].setCustomValidity("There are sprints that start before that date. Please select a date earlier than " + maxDateFormatted);
            startErrorDiv.text("There are sprints that start before that date. Please select a date earlier than " + maxDateFormatted)
        }

        if (projectStart.val() < minStartDate) {
            const minDateFormatted = new Date(projectStart[0].min).toLocaleDateString();
            projectStart[0].setCustomValidity("Date must be less than a year ago. Please select a date later than " + minDateFormatted);
            startErrorDiv.text("Date must be less than a year ago. Please select a date later than " + minDateFormatted)
        }
    })

    projectEnd.on("change", () => {
        checkDates()
        let endErrorDiv = $("#projectEndDateFeedback")
        if (projectEnd.val() < minEndDate) {
            const minDateFormatted = new Date(projectEnd[0].min).toLocaleDateString();
            projectEnd[0].setCustomValidity("There are sprints that end after that date. Please select a date after " + minDateFormatted);
            endErrorDiv.text("There are sprints that end after that date. Please select a date after " + minDateFormatted)
        }
    })

    projectDescription.on("input", () => {
        let descText = projectDescription.val().toString()
        if (GENERAL_UNICODE_REGEX.test(descText)) {
            projectDescription[0].setCustomValidity("");
        } else {
            projectDescription[0].setCustomValidity("Project description " + GENERAL_UNICODE_REQUIREMENTS);
        }
    })

    projectName.on("input", () => {
        let nameText = projectName.val().toString()
        let errorDiv = $("#nameError")
        if (nameText.trim().length === 0) {
            projectName[0].setCustomValidity("Project name cannot be empty")
            errorDiv.text("Project name cannot be empty")
        } else {
            projectName[0].setCustomValidity("")
            errorDiv.text("Project name " + GENERAL_UNICODE_REQUIREMENTS)
        }
    })

    //When the submit button is clicked on the form.
    projectForm.on("submit", function (event) {
        event.preventDefault()

        if (projectForm[0].checkValidity()) {
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
                    createAlert(error.responseText, AlertTypes.Failure)
                }
            })
        } else {
            event.stopPropagation();
            const errorElements = projectForm.find(".form-control:invalid")
            $('html, body').animate({
                scrollTop: $(errorElements[0]).offset().top - 100
            }, 50); //Scrolls to the first invalid field of the form
        }
    })
})