
$(document).ready(function() {
    // Checks to see if there is an error message to be displayed
    if (!$(".errorMessage").is(':empty'))  {
        $(".errorMessageParent").slideDown();
    }
    if (!$(".successMessage").is(':empty'))  {
        $(".successMessageParent").slideDown().delay(1000).slideUp();
    }
    if (!$(".infoMessage").is(':empty'))  {
        $(".infoMessageParent").slideDown().delay(1000).slideUp();
    }



    /**
     * Binds an event handler to the "click".
     * https://api.jquery.com/click/
     * The handler looks for all props on any of the tags like <input> where there is a disabled prop.
     */
    $(".editUserButton").click(() => {
        $(".canDisable").prop("disabled",!$(".canDisable").prop("disabled"));
    })
    $(".editPasswordButton").click(() => {
        $(".canDisablePassword").prop("disabled",!$(".canDisablePassword").prop("disabled"));
    })


    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })
});


