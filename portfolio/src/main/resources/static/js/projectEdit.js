$(document).ready(function(){

    $("#projectStartDate").on("change", function () {
        let projectStart = $(this).val()
        let projectEnd = $("#projectEndDate").val()
        if (projectStart >= projectEnd) {
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

    $("#projectEndDate").on("change", function () {
        let projectStart = $("#projectStartDate").val()
        let projectEnd = $(this).val()
        if (projectStart >= projectEnd) {
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


})