/**
 * When a user is clicked, a call is made to retrieve the user's evidence page.
 */
$(document).on("click", ".userRoleRow", function() {
    let userId = $(this).find(".userId").text()
    $.ajax({
        url: "evidenceData?userId=" + userId,
        success: function() {
            $.ajax({
            url: "evidence?userId=" + userId,
                success: function() {
                window.location.href = "/evidence?userId=" + userId //redirect to the user's evidence page
                }
            })
        },
        error: function (error) {
            console.log(error)
        }
    })
})
