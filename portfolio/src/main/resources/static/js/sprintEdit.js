$(document).ready(function(){

    $("#sprintStartDate").on("change", function () {
        let sprintStart = $(this).val()
        let sprintEnd = $("#sprintEndDate").val()
        if (sprintStart >= sprintEnd) {
            $(this).closest(".col").append(`<div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You should probably make the start date be before the end date
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            $(".submitButton").attr("disabled", true)
        } else {
            $(".submitButton").attr("disabled", false)
            $(".alert-danger").remove()

        }
    })

    $("#sprintEndDate").on("change", function () {
        let sprintStart = $("#sprintStartDate").val()
        let sprintEnd = $(this).val()
        if (sprintStart >= sprintEnd) {
            $(this).closest(".col").append(`<div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You should probably make the end date be after the start date
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            $(".submitButton").attr("disabled", true)
        } else {
            $(".submitButton").attr("disabled", false)
            $(".alert-danger").remove()

        }
    })

    $("#sprintName").keyup(function() {
        let sprintName = $("#sprintName").val()
        console.log(sprintName.length)
        if (sprintName.length === 0 || sprintName.trim().length === 0) {
            $(this).addClass("is-invalid")
            $(".submitButton").attr("disabled", true)
        } else {
            $(this).removeClass("is-invalid")
            $(".submitButton").attr("disabled", false)
        }

    })


})