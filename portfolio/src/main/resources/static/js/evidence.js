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
        userBeingViewedId = urlParams.get('userId')
    } else {
        userBeingViewedId = userIdent
    }

    if (userBeingViewedId !== userIdent) {
        $(".evidenceDeleteButton").hide()
        $(".createEvidenceButton").hide();
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
        success: function (response) {
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

    let previouslySelectedDiv = $(this).parent().find(".selectedEvidence").first()
    previouslySelectedDiv.removeClass("selectedEvidence")

    let newSelectedDiv = $(this).addClass("selectedEvidence")
    selectedEvidenceId = newSelectedDiv.find(".evidenceId").text()

    showHighlightedEvidenceDetails()
})



/**
 * Saves the evidence input during creating a new piece of evidence
 */
$(document).on("click", "#evidenceSaveButton", function (event) {
    event.preventDefault()
    let evidenceCreationForm = $("#evidenceCreationForm")[0]
    if (!evidenceCreationForm.checkValidity()) {
        evidenceCreationForm.reportValidity()
    } else {
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
            success: function (response) {
                selectedEvidenceId = response.id
                getAndAddEvidencePreviews()
                createAlert("Created evidence")
                $("#addEvidenceModal").modal('hide')
                clearAddEvidenceModalValues()
            },
            error: function (error) {
                createAlert(error.responseText, true)
            }
        })
    }
})


/**
 * Listens for when add web link button is clicked.
 * Slide-toggles the web link portion of the form.
 */
$(document).on('click', '.addWebLinkButton', function () {
    let button = $(".addWebLinkButton");
    if (button.hasClass("toggled")) {
        //validate the link
        validateWebLink()
    } else {
        webLinkButtonToggle()
    }
})


/**
 * Listen for a keypress in the weblink address field, and closes the alert box
 */
$(document).on('keypress', '#webLinkAddress', function () {
    $(".address-alert").alert('close')
})


/**
 * Listen for a keypress in the weblink name field, and closes the alert box
 */
$(document).on('keypress', '#webLinkName', function () {
    $(".weblink-name-alert").alert('close')
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

    if (userBeingViewedId === userIdent) {
        $(".evidenceDeleteButton").show()
    } else {
        $(".evidenceDeleteButton").hide()
    }
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
    $(".evidenceDeleteButton").hide()
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
        <div class="evidenceListItem ${evidence.id === selectedEvidenceId ? 'selectedEvidence' : ''}">
            <div class="row evidenceListItemHeader">
                <p class="evidenceId" style="display: none">${evidence.id}</p>
                <p class="col evidenceListItemTitle">${evidence.title}</p>
                <p class="col evidenceListItemDate">${evidence.date}</p>
            </div>
            <p class="evidenceListItemInfo">${evidence.description}</p>
        </div>`
}


/**
 * Validates the weblink.
 * Takes the URL and makes a call to the server to check if it's valid.
 * Then it does some basic title validation
 *
 * If there's an issue, it displays an alert
 * If it's valid, submits the weblink and toggles the form/button.
 */
function validateWebLink() {
    let address = $("#webLinkAddress").val()
    let alias = $("#webLinkName").val()
    let form = $(".weblink-form")
    $.ajax({
        url: `validateWebLink?address=${address}`,
        type: "GET",
        success: () => {
            //Do some title validation
            if (alias.length === 0) {
                $(".address-alert").alert('close') //Close any previous alerts
                form.append(`
                    <div class="alert alert-danger alert-dismissible show weblink-name-alert" role="alert">
                      Please include a name for your web link
                      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    `)
            } else {
                submitWebLink()
                webLinkButtonToggle()
            }
        },
        error: (error) => {
            $(".address-alert").alert('close') //Close any previous alerts
            switch (error.status) {
                case 400:
                    // The URL is invalid
                    form.append(`
                    <div class="alert alert-danger alert-dismissible show address-alert" role="alert">
                      Please enter a valid address, like https://www.w3.org/WWW/
                      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    `)
                    break
                default:
                    // A regular error
                    form.append(`
                    <div class="alert alert-danger alert-dismissible show address-alert" role="alert">
                      Something went wrong. Try again later.
                      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    `)
                    break
            }
        }
    })
}


/**
 * Toggles the add weblink button,
 * and slide-toggles the form
 */
function webLinkButtonToggle() {
    let button = $(".addWebLinkButton");
    $(".weblink-form").slideToggle();
    if (button.hasClass("toggled")) {
        button.text("Add Web Link")
        button.removeClass("toggled")
        button.removeClass("btn-primary")
        button.addClass("btn-secondary")
    } else {
        button.text("Save Web Link")
        button.addClass("toggled")
        button.removeClass("btn-secondary")
        button.addClass("btn-primary")
    }
}


/**
 * Appends a new link to the list of added links in the Add Evidence form.
 */
function submitWebLink() {
    let alias = $("#webLinkName")
    let address = $("#webLinkAddress")
    let addedWebLinks = $("#addedWebLinks")
    let webLinkTitle = $("#webLinkTitle")

    webLinkTitle.show()
    addedWebLinks.append(
        webLinkElement(address.val(), alias.val())
    )

    $('[data-bs-toggle="tooltip"]').tooltip(); //re-init tooltips so appended tooltip displays
    address.val("")
    alias.val("")
}


function clearAddEvidenceModalValues() {
    $("#evidenceName").val("")
    $("#evidenceDescription").val("")
    $("#webLinkAddress").val("")
    $("#webLinkName").val("")
    $("#addedWebLinks").empty()
    $("#webLinkTitle").empty()
    $(".alert").alert('close') //Close alerts
}

/**
 * Given a web address and an alias, creates and returns a web link element.
 * The main div will have the class 'secured' if it is https, or 'unsecured' otherwise
 *
 * If the address doesn't start with https, it will show an un-filled, unlocked icon.
 * If it does, it will show a locked, filled icon.
 *
 * @param address The web address of the web link
 * @param alias The alias/nickname of the web address. Everything before the first // occurrence will be cut off
 * (e.g. https://www.goggle.com becomes www.google.com)
 * @returns {string} A single-div webLink element, wrapped in ` - e.g. `<div>stuff!</div>`
 */
function webLinkElement(address, alias) {
    let icon;
    let security = "unsecured"
    if (address.startsWith("https://")) {
        security = "secured"
        icon = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-lock-fill lockIcon text-success" viewBox="0 0 16 16">
        <path d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"/>
        </svg>
        `
    } else {
        icon = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-unlock-fill lockIcon text-danger" viewBox="0 0 16 16">
  <path d="M11 1a2 2 0 0 0-2 2v4a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h5V3a3 3 0 0 1 6 0v4a.5.5 0 0 1-1 0V3a2 2 0 0 0-2-2z"/>
</svg>
        `
    }
    let slashIndex = address.search("//") + 2
    if (slashIndex > 1) address = address.slice(slashIndex) // Cut off the http:// or whatever else it might be
    return (`
<div class="webLinkElement ${security}" data-bs-toggle="tooltip" data-bs-placement="top" 
    data-bs-title="${address}" data-bs-custom-class="webLinkTooltip">
    ${icon}
    <div class="addedWebLinkName">
        ${alias}
    </div>
</div>
    `)
}


// -------------------------------------- Validation -----------------------------------


/**
 * Checks the form is valid, enables or disables the save button depending on validity.
 */
function disableEnableSaveButtonOnValidity() {
    if ($("#evidenceCreationForm")[0].checkValidity()) {
        $("#evidenceSaveButton").attr("disabled", false)
    } else {
        $("#evidenceSaveButton").attr("disabled", true)
    }
}

function checkTextInputRegex() {
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
$(document).on("keyup", ".text-input", function () {
    disableEnableSaveButtonOnValidity()
    checkTextInputRegex()
})


/**
 * Calls the validity checking function on change of form inputs.
 * This is different from keyup as it checks when the date changes.
 */
$(document).on("change", ".form-control", function () {
    disableEnableSaveButtonOnValidity()
    checkTextInputRegex()
})