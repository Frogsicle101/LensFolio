/**
 * Functions to be run when page has finished loading.
 */
$(document).ready(function () {

    /* Uses a DELETE request to remove the selected role from a user */
    $(".roleDeleteButton").click(function () {
        let role = $(this).siblings().text();
        let userId = $(this).closest(".roleButtonsContainer").siblings(".userId").text(); // gets the user ID of the user being edited
        $.ajax("/editUserRole", {
            type: "DELETE",
            data: {
                "userId": userId,
                "role": role
            },
            success: function () {
                location.reload()
            }
        })
    })

    /* Toggles the list of roles that can be added. */
    $(".addRolePopUpButton").click(function () {
        $(this).siblings(".collapse").collapse('toggle');
    })

    /* Uses a PUT request to add roles to a user.
    *  If the role cannot be added, an informative alert message is displayed. */
    $(".roleToAddButton").click(function () {
        let role = $(this).text();
        let userId = $(this).closest(".roleButtonsContainer").siblings(".userId").text();
        $.ajax({
            url: "/editUserRole",
            type: "PUT",
            data: {
                "userId": userId,
                "role": role
            },
            success: function () {
                location.reload()
            },
            error: function (response) {
                $(".title").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                ` + response.toString() + `
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            }
        })
    });
})