 $(document).ready(function(){

     let sprintId = $("#sprintId")
     let sprintName = $("#sprintName")
     let sprintStartDate = $("#sprintStartDate")
     let sprintEndDate = $("#sprintEndDate")
     let sprintDescription = $("#sprintDescription")
     let sprintColour = $("#sprintColour")
     let dateAlert = $(".dateAlert")

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


    $(".sprintEditForm").submit(function(event){
        event.preventDefault()



        let dataToSend= {
            "sprintId" : sprintId.val(),
            "sprintName" : sprintName.val(),
            "sprintStartDate" : sprintStartDate.val(),
            "sprintEndDate" : sprintEndDate.val(),
            "sprintDescription" : sprintDescription.val(),
            "sprintColour" : sprintColour.val()
        }

        $.ajax({
            url: "/sprintSubmit",
            type: "post",
            data: dataToSend,
            success: function(){
                window.history.back();
            },
            error: function(error){
                console.log(error.responseText)
                $(".errorMessage").text(error.responseText)
                $(".errorMessageParent").slideUp()
                $(".errorMessageParent").slideDown()
            }
        })
    })


})