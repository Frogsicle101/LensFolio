$(document).ready(() => {
    //Jquery selectors to remove duplicity
    let shortName = $("#shortName")
    let longName = $("#longName")
    let formControl = $(".form-control");
    formControl.each(countCharacters)
    formControl.keyup(countCharacters)

    // On create group form submit
    $("#createGroupForm").submit(function (event) {
        event.preventDefault(); // Prevents submit

        let groupData = {
            "shortName": shortName.val(),
            "longName": longName.val(),
        }

        $.ajax({
            url: "groups/edit",
            type: "post",
            data: groupData,
            success: function () {
                location.href = "groups" // On success reloads page
            },
            error: function (error) {//Displays error in box on failure
                createAlert(error.responseText, true)
            }
        })
    })
})
