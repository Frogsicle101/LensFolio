/**
 * Compares the values of the start and end date inputs, and if they are invalid (the start date after the end date),
 * displays an error message
 */
function checkDateOrder(startDate, endDate) {
    let dateAlert = $(".dateAlert")
    if (startDate >= endDate) {
        dateAlert.slideDown()
        $(".canDisable").attr("disabled", true)
        $(".date").addClass("is-invalid")
        $(".editForm").removeClass("was-validated")
    } else {
        $(".canDisable").attr("disabled", false)
        $(".date").removeClass("is-invalid")
        $(".editForm").addClass("was-validated")
        dateAlert.slideUp();
    }
}