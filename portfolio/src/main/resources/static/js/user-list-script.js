/**
 * Functions to be run when page has finished loading.
 */
$(document).ready(function () {


    /* Uses a DELETE request to remove the selected role from a user.
    * If the user's role cannot be deleted, display an informative alert in the centre of the screen. */
    $(".roleDeleteButton").click(function (event) {
        event.stopPropagation()
        let role = $(this).siblings().text();
        let userId = $(this).closest(".roleButtonsContainer").siblings(".userId").text(); // gets the user ID of the user being edited
        $.ajax("editUserRole", {
            type: "DELETE",
            data: {
                "userId": userId,
                "role": role
            },
            success: function () {
                location.reload()
            },
            error: function (error) {
                createAlert(error.responseText, true)
            }

        })
    })


    /* Toggles the list of roles that can be added. */
    $(".addRolePopUpButton").click(function (event) {
        event.stopPropagation()
        $(this).siblings(".collapse").collapse('toggle');
    })


    /* Uses a PUT request to add roles to a user. */
    $(".roleToAddButton").click(function (event) {
        event.stopPropagation()
        let role = $(this).text();
        let userId = $(this).closest(".roleButtonsContainer").siblings(".userId").text();
        $.ajax({
            url: "editUserRole",
            type: "PUT",
            data: {
                "userId": userId,
                "role": role
            },
            success: function () {
                location.reload()
            },
            error: function (error) {
                createAlert(error.responseText, true)
            }
        })
    });
})


/**
 * When a user is clicked, a call is made to retrieve the user's evidence page.
 */
$(document).on("click", ".userRoleRow", function() {
    let userId = $(this).find(".userId").text()
    window.location.href = "/evidence?userId=" + userId //redirect to the user's evidence page
})
