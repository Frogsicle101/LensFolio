/** the user id of the user whose evidence page if being viewed */
let userBeingViewedId;


/**
 * Runs when the page is loaded. This gets the user being viewed and adds dynamic elements.
 */
$(document).ready(function () {
    let urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has("userId")) {
        userBeingViewedId = urlParams.get('sent')
    } else {
        userBeingViewedId = userIdent
    }
    getAndAddEvidenceData()
})


/**
 * Gets the evidence data for the chosen user and adds it to the page.
 */
function getAndAddEvidenceData() {
    $.ajax({
        url: "evidenceData?userId=" + userBeingViewedId,
        success: function(response) {
            addEvidencePreviews(response)
        },
        error: function (error) {
            createAlert(error.message, true)
        }
    })
}


/**
 * Takes the response from an evidence list get request and adds the evidence previews to the left
 * side of the page.
 *
 * @param response - The response from GET /evidenceData
 */
function addEvidencePreviews(response) {
    let evidencePreviewsContainer = $("#evidenceList")
    evidencePreviewsContainer.empty()
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
            getAndAddEvidenceData()
            createAlert("Created evidence")
        },
        error: function (error) {
            createAlert(error.message, true)
        }
    })
})

/**
 * Listens for when add web link button is clicked.
 * Slide-toggles the web link portion of the form.
 */
$(document).on('click', '.addWebbLinkButton', function () {
    $(".weblink-form").slideToggle();
    let button = $(".addWebbLinkButton");
    if (button.hasClass("toggled")) {
        //Un-toggle the button
        button.text("Add Web Link")
        button.removeClass("toggled")
        button.removeClass("btn-primary")
        button.addClass("btn-secondary")
        //TODO: Add the code for submitting the web link here
    } else {
        button.text("Save Web Link")
        button.addClass("toggled")
        button.removeClass("btn-secondary")
        button.addClass("btn-primary")
    }
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
        </div>`
}
