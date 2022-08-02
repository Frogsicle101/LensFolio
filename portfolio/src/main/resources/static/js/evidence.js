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
    let textInput = $(".text-input");
    textInput.each(countCharacters)
    textInput.keyup(countCharacters)

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
$(document).on("click", "#evidenceSaveButton", function (event) {
    event.preventDefault()
     if (!$("#evidenceCreationForm")[0].checkValidity()){
         $("#evidenceCreationForm")[0].reportValidity()
     } else {
         const title = $("#evidenceName").val()
         const date = $("#evidenceDate").val()
         const description = $("#evidenceDescription").val()
         const projectId = 1
         if(title.length < 2 || date.length === 0 || description.length === 0) {

         }
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
                $("#addEvidenceModal").modal('hide')
             },
             error: function (error) {
                 createAlert(error.message, true)
             }
         })
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


/**
 * Function that gets the maxlength of an input and lets the user know how many characters they have left.
 */
function countCharacters() {
    let maxlength = $(this).attr("maxLength")
    let lengthOfCurrentInput = $(this).val().length;
    let counter = maxlength - lengthOfCurrentInput;
    let helper = $(this).next(".form-text"); //Gets the next div with a class that is form-text

    //If one character remains, changes from "characters remaining" to "character remaining"
    if (counter !== 1) {
        helper.text(counter + " characters remaining")
    } else {
        helper.text(counter + " character remaining")
    }
}

/**
 * Checks the form is valid, enables or disables the save button depending on validity.
 */
function disableEnableSaveButtonOnValidity() {
    if ($("#evidenceCreationForm")[0].checkValidity()){
        $("#evidenceSaveButton").attr("disabled", false)
    } else {
        $("#evidenceSaveButton").attr("disabled", true)
    }
}
/**
 * Calls the validity checking function on keyup of form inputs.
 */
$(document).on("keyup", ".form-control", function() {
    disableEnableSaveButtonOnValidity()
})

$(document).on("change", ".form-control", function() {
    disableEnableSaveButtonOnValidity()
})