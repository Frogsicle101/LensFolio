$(document).ready(function(){

    $("#sprintStartDate").on("change", function () {
        let sprintStart = $(this).val()
        let sprintEnd = $("#sprintEndDate").val()
        if (sprintStart >= sprintEnd) {
            $(this).closest(".col").append(`<div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You should probably make the start date be before the end date
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
            $(this).addClass("is-invalid")
        } else {
            $(".canDisable").attr("disabled", false)
            $(this).removeClass("is-invalid")
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
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
            $(this).addClass("is-invalid")
        } else {
            $(".canDisable").attr("disabled", false)
            $(this).removeClass("is-invalid")
            $(".alert-danger").remove()

        }
    })

    $("#sprintName").keyup(function() {
        let sprintName = $("#sprintName").val()

        if (sprintName.length === 0 || sprintName.trim().length === 0) {
            $(this).addClass("is-invalid")
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
        } else {
            $(this).removeClass("is-invalid")
            $(".canDisable").attr("disabled", false)
        }

    })


})