/** A regex only allowing English characters */
const regExp = new RegExp('[A-Za-z]');



let webLinksCount = 0;

let categoriesMapping = new Map([
    ["SERVICE", "Service"],
    ["QUALITATIVE", "Qualitative"],
    ["QUANTITATIVE", "Quantitative"]
])


/**
 * Runs when the page is loaded. This gets the user being viewed and adds dynamic elements.
 */
$(document).ready(function () {
    let urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has("userId")) {
        userBeingViewedId = urlParams.get('userId')
    } else {
        userBeingViewedId = userIdent
    }

    if (userBeingViewedId !== userIdent) {
        $(".evidenceDeleteButton").hide()
        $(".createEvidenceButton").hide();
    }
    resetWeblink()
    getAndAddEvidencePreviews()
    checkToShowSkillChips()
    getSkills()
})



// --------------------------- Functional HTML Components ------------------------------------







