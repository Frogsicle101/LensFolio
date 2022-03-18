
$(document).ready(function() {
    // Checks to see if there is an error message to be displayed
    if (!$(".errorMessage").is(':empty'))  {
        $(".alert").show();
    }


    /**
     * Binds an event handler to the "click".
     * https://api.jquery.com/click/
     * The handler looks for all props on any of the tags like <input> where there is a disabled prop.
     */
    $(".editUserButton").click(() => {
        $(".canDisable").prop("disabled",!$(".canDisable").prop("disabled"));
    })
});


