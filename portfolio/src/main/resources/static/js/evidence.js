/**
 * Runs when the page is loaded. This gets the user being viewed and adds dynamic elements.
 */
$(document).ready(function () {
    let userBeingViewedId;
    let urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has("userId")) {
        userBeingViewedId = urlParams.get('sent')
    } else {
        userBeingViewedId = userIdent
    }
    populateEvidencePage(userBeingViewedId)
})


function populateEvidencePage(userId) {
    $.ajax({
        url: "evidenceData?userId=" + userId,
        success: function(response) {
            addEvidencePreviews(response)
        },
        error: function (error) {
            createAlert(error.message, true)
        }
    })
}


function addEvidencePreviews(response) {
    let evidencePreviewsContainer = $("#evidenceList")
    for (let pieceOfEvidence in response) {
        evidencePreviewsContainer.append(createEvidencePreview(response[pieceOfEvidence]))
    }
}


/**
 * Saves the evidence input during creating a new piece of evidence
 */
$(document).on("click", "#evidenceSaveButton", function () {
    const title = $("#evidenceName").val()
    const date = $("#evidenceDate").val()
    const description = $("#evidenceDescription").val()
    const projectId = 1
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
            createAlert("Created evidence")
        },
        error: function (error) {
            createAlert(error.message, true)
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
            createAlert(error.message, true)
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
            <div class="row evidenceListItemHeader">
                <p class="col evidenceListItemTitle">${evidence.title}</p>
                <p class="col evidenceListItemDate">${evidence.date}</p>
            </div>
            <p class="evidenceListItemInfo">${evidence.description}</p>
        </div>
    `
}
