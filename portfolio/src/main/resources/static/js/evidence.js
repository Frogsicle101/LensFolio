
$(document).ready(function () {
    let userBeingViewed;
    let urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has("userId")) {
        userBeingViewed = urlParams.get('sent')
    } else {
        userBeingViewed = userIdent
    }

})

/**
 * Saves the evidence input during creating a new piece of evidence
 */
$(document).on("click", "#evidenceSaveButton", function () {
    const title = $("#evidenceName").val()
    const date = $("#evidenceDate").val()
    const description = $("#evidenceDescription").val()
    const projectId = 1
    console.log(date)
    $.ajax({
        url: "evidence",
        type: "POST",
        data: {
            title,
            date,
            description,
            projectId
        },
        success: function() {
            console.log("hello?")
            createAlert("Created evidence")
        },
        error: function (error) {
            console.log("goodbye?")
            createAlert(error.message)
        }
    })
})


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


/**
 * Creates and returns an HTML element for an evidence preview
 *
 * @param evidence - A json object for a piece of evidence
 * @return the HTML component for previewing evidence of class evidenceListItem
 */
function createEvidencePreview(evidence) {
    return `
        <div class="evidenceListItem">
            <p class="evidenceListItemId" style="display: none">${evidence.id}</p>
            <div class="row evidenceListItemHeader">
                <p class="col evidenceListItemTitle">${evidence.title}</p>
                <p class="col evidenceListItemDate">${evidence.date}</p>
            </div>
            <p class="evidenceListItemInfo">${evidence.description}</p>
        </div>
    `
}
