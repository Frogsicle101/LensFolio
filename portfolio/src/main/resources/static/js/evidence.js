/** the user id of the user whose evidence page if being viewed */
let userBeingViewedId;


const regExp = new RegExp('[A-Za-z]');

/** The id of the piece of evidence being displayed. */
let selectedEvidenceId;


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
    getAndAddEvidencePreviews()
    let textInput = $(".text-input");
    textInput.each(countCharacters)
    textInput.keyup(countCharacters)

})




/**
 * Gets the evidence data for the chosen user and adds it to the page.
 *
 * On successful retrieval, this adds the elements and calls the functions to populate the page.
 * To see these functions:
 *     - addEvidencePreviews(response): Populates the left side evidence menus
 *     - showHighlightedEvidenceDetails(): Populates the right side, the details of the highlighted evidence.
 *
 * Note: by default the first element is the highlighted element.
 */
function getAndAddEvidencePreviews() {
    $.ajax({
        url: "evidenceData?userId=" + userBeingViewedId,
        success: function(response) {
            addEvidencePreviews(response)
            showHighlightedEvidenceDetails()
        },
        error: function (error) {
            createAlert(error.responseText, true)
        }
    })
}


/**
 * This function is responsible for displaying the selected piece of evidence.
 *
 * If nothing is selected, it will default to either the first piece of evidence,
 * or a 'No evidence' display if none exist.
 *
 * It then calls the appropriate function for displaying said evidence.
 */
function showHighlightedEvidenceDetails() {
    if (selectedEvidenceId != null) {
        getHighlightedEvidenceDetails()
        return
    }
    let evidenceElements = $("#evidenceList").children()
    if (evidenceElements.length > 0) {
        selectedEvidenceId = evidenceElements.first().find(".evidenceId").text()
        getHighlightedEvidenceDetails()
    } else {
        setDetailsToNoEvidenceExists()
    }
}


/**
 * This is called so show the evidence details for the selected piece of evidence.
 *
 * If the selectedEvidenceId is null or the server cannot find the evidence, it selected the first
 * piece of evidence in the table, and sets the details to that. If there is no evidence, the appropriate
 * message is displayed.
 */
function getHighlightedEvidenceDetails() {
    $.ajax({
        url: "evidencePiece?evidenceId=" + selectedEvidenceId,
        success: function(response) {
            setHighlightEvidenceAttributes(response)
        },
        error: function () {
            createAlert("Failed to receive active evidence", true)
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
    let evidenceCreationForm = $("#evidenceCreationForm")
     if (!evidenceCreationForm[0].checkValidity()){
         evidenceCreationForm[0].reportValidity()
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
             success: function(response) {
                 selectedEvidenceId = response.id
                 getAndAddEvidencePreviews()
                 createAlert("Created evidence")
                $("#addEvidenceModal").modal('hide')
             },
             error: function (error) {
                 createAlert(error.message, true)
             }
         })
     }

})

// --------------------------------- Click listeners -----------------------------------------


/**
 * When an evidence div is clicked, it becomes selected and is displayed on the main display.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".evidenceListItem", function() {

    let previouslySelectedDiv = $(this).parent().find(".selected").first()
    previouslySelectedDiv.removeClass("selected")

    let newSelectedDiv = $(this).addClass("selected")
    selectedEvidenceId = newSelectedDiv.find(".evidenceId").text()

    showHighlightedEvidenceDetails()
})


// --------------------------- Functional HTML Components ------------------------------------

/**
 * Sets the evidence details (big display) values to the given piece of evidence.
 *
 * @param evidenceDetails
 */
function setHighlightEvidenceAttributes(evidenceDetails) {
    let highlightedEvidenceTitle = $("#evidenceDetailsTitle")
    let highlightedEvidenceDate = $("#evidenceDetailsDate")
    let highlightedEvidenceDescription = $("#evidenceDetailsDescription")

    highlightedEvidenceTitle.text(evidenceDetails.title)
    highlightedEvidenceDate.text(evidenceDetails.date)
    highlightedEvidenceDescription.text(evidenceDetails.description)

    highlightedEvidenceTitle.show()
    highlightedEvidenceDate.show()
    highlightedEvidenceDescription.show()
}


/**
 * Hides the date and description fields and sets the Title field to no information.
 *
 * This function is called when the page is rendered and no evidence exists.
 */
function setDetailsToNoEvidenceExists() {
    let highlightedEvidenceTitle = $("#evidenceDetailsTitle")
    let highlightedEvidenceDate = $("#evidenceDetailsDate")
    let highlightedEvidenceDescription = $("#evidenceDetailsDescription")

    highlightedEvidenceTitle.text("No Evidence")

    highlightedEvidenceTitle.show()
    highlightedEvidenceDate.hide()
    highlightedEvidenceDescription.hide()
}


/**
 * Creates and returns an HTML element for an evidence preview
 *
 * @param evidence - A json object for a piece of evidence
 * @return the HTML component for previewing evidence of class evidenceListItem
 */
function createEvidencePreview(evidence) {
    return `
        <div class="evidenceListItem ${evidence.id === selectedEvidenceId ? 'selected' : ''}">
            <div class="row evidenceListItemHeader">
                <p class="evidenceId" style="display: none">${evidence.id}</p>
                <p class="col evidenceListItemTitle">${evidence.title}</p>
                <p class="col evidenceListItemDate">${evidence.date}</p>
            </div>
            <p class="evidenceListItemInfo">${evidence.description}</p>
        </div>`
}

// -------------------------------------- Validation -----------------------------------


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

function checkTextInputRegex(){
    let name = $("#evidenceName")
    let description = $("#evidenceDescription")
    let nameVal = name.val()
    let descriptionVal = description.val()

    if (!regExp.test(nameVal) || !regExp.test(descriptionVal)) {
        $("#evidenceSaveButton").attr("disabled", true)
    }

    if (!regExp.test(nameVal) && nameVal.length > 0) {
        name.addClass("invalid")
    } else {
        name.removeClass("invalid")
    }

    if (!regExp.test(descriptionVal) && descriptionVal.length > 0) {
        description.addClass("invalid")

    } else {
        description.removeClass("invalid")
    }
}
/**
 * Calls the validity checking function on keyup of form inputs.
 */
$(document).on("keyup", ".text-input", function() {
    disableEnableSaveButtonOnValidity()
    checkTextInputRegex()
})

/**
 * Calls the validity checking function on change of form inputs.
 * This is different from keyup as it checks when the date changes.
 */
$(document).on("change", ".form-control", function() {
    disableEnableSaveButtonOnValidity()
    checkTextInputRegex()
})