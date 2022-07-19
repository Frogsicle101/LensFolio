$(document).ready(() => {

    let projectName = $("#projectName")
    let projectStart = $("#projectStartDate")
    let projectEnd = $("#projectEndDate")
    let projectId = $("#projectId")
    let projectDescription = $("#projectDescription")
    let dateAlert = $(".dateAlert")


    // The following two chunks of code are related to the date inputs
    // They check that the projectStart or projectEnd are not the wrong way (start after end etc)
    projectStart.on("change", function () {
        let projectStart = $(this).val()
        let projectEnd = $("#projectEndDate").val()
        if (projectStart >= projectEnd) {
            dateAlert.slideUp()
            dateAlert.slideDown()
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
            $(this).addClass("is-invalid")
        } else {
            $(".canDisable").attr("disabled", false)
            $(this).removeClass("is-invalid")
            $(".dateAlert").slideUp()

        }
    })


    projectEnd.on("change", function () {
        let projectStart = $("#projectStartDate").val()
        let projectEnd = $(this).val()
        if (projectStart >= projectEnd) {
            dateAlert.slideUp()
            dateAlert.slideDown()
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
            $(this).addClass("is-invalid")
        } else {
            $(".canDisable").attr("disabled", false)
            $(".startDateAlert").slideUp()
            $(this).removeClass("is-invalid")
            $(".dateAlert").slideUp()

        }
    })


    //When the submit button is clicked on the form.
    $(".projectEditForm").submit(function (event) {
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
                console.log(error.responseText)
                $(".errorMessage").text(error.responseText)
                $(".errorMessageParent").slideUp()
                $(".errorMessageParent").slideDown()
            }
        })
    })
})