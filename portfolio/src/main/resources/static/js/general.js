
$(document).ready(function() {
    // Checks to see if there is an error message to be displayed
    if (!$(".errorMessage").is(':empty'))  {
        $(".errorMessageParent").show();
    }
    if (!$(".infoMessage").is(':empty'))  {
        $(".infoMessageParent").show();
    }

    if (!$(".successMessage").is(':empty'))  {
        $(".successMessageParent").show();
    }


    let tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    let tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })
});



