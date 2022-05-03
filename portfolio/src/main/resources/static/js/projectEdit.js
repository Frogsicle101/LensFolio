$(document).ready(function(){

    let projectName = $("#projectName")
    let projectStart = $("#projectStartDate")
    let projectEnd = $("#projectEndDate")
    let projectId = $("#projectId")
    let projectDescription = $("#projectDescription")


    $("#projectStartDate").on("change", function () {
        let projectStart = $(this).val()
        let projectEnd = $("#projectEndDate").val()
        if (projectStart >= projectEnd) {
            $(".dateAlert").slideUp()
            $(".dateAlert").slideDown()
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
            $(this).addClass("is-invalid")
        } else {
            $(".canDisable").attr("disabled", false)
            $(this).removeClass("is-invalid")
            $(".dateAlert").slideUp()

        }
    })

    $("#projectEndDate").on("change", function () {
        let projectStart = $("#projectStartDate").val()
        let projectEnd = $(this).val()
        if (projectStart >= projectEnd) {
            $(".dateAlert").slideUp()
            $(".dateAlert").slideDown()
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

    $("#projectName").keyup(function() {
        let projectName = $("#projectName").val()

        if (projectName.length === 0 || projectName.trim().length === 0) {
            $(this).addClass("is-invalid")
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
        } else {
            $(this).removeClass("is-invalid")
            $(".canDisable").attr("disabled", false)
        }

    })

    $(".projectEditForm").submit(function(event){
        event.preventDefault()

        let dataToSend = {
            "projectId": projectId.val(),
            "projectName": projectName.val(),
            "projectStartDate" : projectStart.val(),
            "projectEndDate": projectEnd.val(),
            "projectDescription": projectDescription.val()
        }
        $.ajax({
            url: "/projectEdit",
            data: dataToSend,
            type: "post",
            success: function() {

            },
            error: function(error) {
                console.log(error.responseText)
                $(".errorMessage").text(error.responseText)
                $(".errorMessageParent").slideUp()
                $(".errorMessageParent").slideDown()
            }
        })
    })



})