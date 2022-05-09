$(document).ready(function () {

    let sprintId = $("#sprintId")
    let sprintName = $("#sprintName")
    let sprintStartDate = $("#sprintStartDate")
    let sprintEndDate = $("#sprintEndDate")
    let sprintDescription = $("#sprintDescription")
    let sprintColour = $("#sprintColour")
    let dateAlert = $(".dateAlert")


    // Checks when the start date changes that its not past the end date.
    $("#sprintStartDate").on("change", function () {
        let sprintStart = $(this).val()
        let sprintEnd = $("#sprintEndDate").val()
        if (sprintStart >= sprintEnd) {
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


    // Checks when the sprint end date changes that its not before the start date.
    $("#sprintEndDate").on("change", function () {
        let sprintStart = $("#sprintStartDate").val()
        let sprintEnd = $(this).val()
        if (sprintStart >= sprintEnd) {
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


    // When submit button is clicked on sprint edit form
    $(".sprintEditForm").submit(function (event) {
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
                window.history.back();
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