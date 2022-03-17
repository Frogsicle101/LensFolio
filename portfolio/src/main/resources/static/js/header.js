/**
 * Functions to be run when page has finished loading.
 */
$(document).ready(function() {
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

