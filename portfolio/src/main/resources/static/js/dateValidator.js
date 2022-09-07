/**
 * Compares the values of the start and end date inputs, and if they are invalid (the start date after the end date),
 * displays an error message
 */
function checkDateOrder(startDate, endDate) {
    let pickers = $(".date").get()
    for (let picker of pickers) {
        picker.setCustomValidity("");
    }
    if (startDate >= endDate) {
        for (let picker of pickers) {
            picker.setCustomValidity("Start date must be before end date");
        }
    }
}