$(document).ready(() => {
    //Jquery selectors to remove duplicity
    let shortName = $("#shortName")
    let longName = $("#longName")
    let errorMessageParent = $(".errorMessageParent")
    let errorMessage = $(".errorMessage")

    // On account form submit
    $("#createGroupForm").submit(function (event) {
        event.preventDefault(); // Prevents submit

        let groupData = {
            "shortName": shortName.val(),
            "longName": longName.val(),
        }

        $.ajax({
            url: "edit",
            type: "post",
            data: groupData,
            success: function () {
                location.href = "/account" // On success reloads page
            },
            error: function (error) {//Displays error in box on failure
                errorMessage.text(error.responseText)
                errorMessageParent.slideUp()
                errorMessageParent.slideDown()
            }
        })
    })
})
