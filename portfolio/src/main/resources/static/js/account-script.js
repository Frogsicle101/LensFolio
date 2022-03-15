/**
 * Functions to be run when page has finished loading.
 */
$(document).ready(function() {
    let username = $(".username").html() // Gets username
    $(".username").html(username.charAt(0).toUpperCase() + username.slice(1)) // Sets username to capitalized.

    $(".profileDropdown").click(() => {
        toggleDropDown()
    })

    $(".editUserButton").click(() => {
        $(".canDisable").prop("disabled",false);
    })
})

function toggleDropDown() {
    $(".dropdown-content").slideToggle();

    return $(this);
}

