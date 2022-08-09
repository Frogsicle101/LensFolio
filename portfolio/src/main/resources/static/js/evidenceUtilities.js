/**
 * A JS file that contains utilities for pieces of evidence,
 * that can be used across multiple pages.
 */


/** the user id of the user whose evidence page if being viewed */
let userBeingViewedId;

/** A regex only allowing modern English letters */
const regExp = new RegExp('[A-Za-z]');

/** The id of the piece of evidence being displayed. */
let selectedEvidenceId;

let skillsArray = []


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
 * Adds the web links from the given request to the document.
 *
 * @param response The response from the backend, which contains the web links for a piece of evidence.
 */
function setHighlightedEvidenceWebLinks(response) {
    let webLinksDiv = $("#evidenceWebLinks")
    webLinksDiv.empty()
    console.log("hi")
    console.log(response)

    for (let index in response) {
        console.log(index)
        let webLink = response[index]
        webLinksDiv.append(webLinkElement(webLink.url, webLink.alias))
    }
    initialiseTooltips()
}



/**
 * Given a web url and an alias, creates and returns a web link element.
 * The main div will have the class 'secured' if it is https, or 'unsecured' otherwise
 *
 * If the url doesn't start with https, it will show an un-filled, unlocked icon.
 * If it does, it will show a locked, filled icon.
 *
 * @param url The web url of the web link
 * @param alias The alias/nickname of the web url. Everything before the first // occurrence will be cut off
 * (e.g. https://www.goggle.com becomes www.google.com)
 * @returns {string} A single-div webLink element, wrapped in ` - e.g. `<div>stuff!</div>`
 */
function webLinkElement(url, alias) {
    let icon;
    let security = "unsecured"

    console.log(alias)
    if (url.startsWith("https://")) {
        security = "secured"
        icon = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-lock-fill lockIcon text-success" viewBox="0 0 16 16">
        <path d="M8 1a2 2 0 0 1 2 2v4H6V3a2 2 0 0 1 2-2zm3 6V3a3 3 0 0 0-6 0v4a2 2 0 0 0-2 2v5a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z"/>
        </svg>
        `
    } else {
        icon = `
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-unlock lockIcon text-danger" viewBox="0 0 16 16">
        <path d="M11 1a2 2 0 0 0-2 2v4a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h5V3a3 3 0 0 1 6 0v4a.5.5 0 0 1-1 0V3a2 2 0 0 0-2-2zM3 8a1 1 0 0 0-1 1v5a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V9a1 1 0 0 0-1-1H3z"/>
        </svg>
        `
    }

    let slashIndex = url.search("//") + 2
    if (slashIndex > 1) urlSlashed = url.slice(slashIndex) // Cut off the http:// or whatever else it might be

    return (`
        <div class="webLinkElement ${security}" data-value="${url}" >
            ${icon}
            <div class="addedWebLinkName" data-bs-toggle="tooltip" data-bs-placement="top" 
            data-bs-title="${urlSlashed}" data-bs-custom-class="webLinkTooltip">${alias}</div>
            <div class="addedWebLinkUrl" style="display: none">${url}</div>
        </div>
    `)
}



// --------------------------- Server Queries ------------------------------------


/**
 * This is called to show the evidence details for the selected piece of evidence.
 *
 * If the selectedEvidenceId is null or the server cannot find the evidence, it selected the first
 * piece of evidence in the table, and sets the details to that. If there is no evidence, the appropriate
 * message is displayed.
 */
function getHighlightedEvidenceDetails() {
    $.ajax({
        url: "evidencePiece?evidenceId=" + selectedEvidenceId, success: function (response) {
            console.log(response)
            setHighlightEvidenceAttributes(response)
            getHighlightedEvidenceWeblinks()
        }, error: function () {
            createAlert("Failed to receive active evidence", true)
        }
    })
}


/**
 * Makes a request to the backend to retrieve all the web links for a piece of evidence. If the request is successful,
 * a function is called to add the web links to the document.
 */
function getHighlightedEvidenceWeblinks() {
    $.ajax({
        url: "evidencePieceWebLinks?evidenceId=" + selectedEvidenceId, success: function (response) {
            setHighlightedEvidenceWebLinks(response)
        }, error: function (response) {
            if (response.status !== 404) {
                createAlert("Failed to receive evidence links", true)
            }
        }
    })
}


/**
 * Makes a call to the server and gets all the skills belonging to this user,
 * It then appends those skills to the list
 *
 * @param callback An optional callback function to be called upon successfully retrieving the skills
 */
function getSkills(callback = () => {}) {
    $.ajax({
        url: "skills?userId=" + userBeingViewedId, type: "GET", success: function (response) {
            $.each(response, function (i) {
                if (!skillsArray.includes(response[i].name)) {
                    skillsArray.push(response[i].name)
                }
                callback()
            })
        }, error: function (response) {
            console.log(response)
        }
    })
}


// --------------------------- Functional HTML Components ------------------------------------


/**
 * Sets the evidence details (big display) values to the given piece of evidence.
 *
 * @param evidenceDetails The title, date, and description for a piece of evidence.
 */
function setHighlightEvidenceAttributes(evidenceDetails) {
    let highlightedEvidenceTitle = $("#evidenceDetailsTitle")
    let highlightedEvidenceDate = $("#evidenceDetailsDate")
    let highlightedEvidenceDescription = $("#evidenceDetailsDescription")

    highlightedEvidenceTitle.text(evidenceDetails.title)
    highlightedEvidenceDate.text(evidenceDetails.date)
    highlightedEvidenceDescription.text(evidenceDetails.description)
    addSkillsToEvidence(evidenceDetails.skills)

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
 * Receives a list of skills and adds them to the focused evidence.
 *
 * @param skills The skills to be added.
 */
function addSkillsToEvidence(skills) {
    let highlightedEvidenceSkills = $("#evidenceDetailsSkills")
    highlightedEvidenceSkills.empty();
    console.log(skills)
    // Sorts in alphabetical order
    skills.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1)

    $.each(skills, function (i) {
        highlightedEvidenceSkills.append(`
                <div class="skillChip">
                    <p class="skillChipText">${skills[i].name}</p>
                </div>`)
    })
}


/**
 * Creates and returns an HTML element for an evidence preview
 *
 * @param evidence - A json object for a piece of evidence
 * @return the HTML component for previewing evidence of class evidenceListItem
 */
function createEvidencePreview(evidence) {
    return `
        <div class="box ${evidence.id === selectedEvidenceId ? 'selectedEvidence' : ''}">
            <div class="row evidenceListItemHeader">
                <p class="evidenceId" style="display: none">${evidence.id}</p>
                <p class="col evidenceListItemTitle">${evidence.title}</p>
                <p class="col evidenceListItemDate">${evidence.date}</p>
            </div>
            <p class="evidenceListItemInfo">${evidence.description}</p>
        </div>`
}


//---- Click Listeners ----


/**
 * When an evidence div is clicked, it becomes selected and is displayed on the main display.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".evidenceListItem", function () {
    let previouslySelectedDiv = $(this).parent().find(".selectedEvidence").first()
    previouslySelectedDiv.removeClass("selectedEvidence")

    let newSelectedDiv = $(this).addClass("selectedEvidence")
    selectedEvidenceId = newSelectedDiv.find(".evidenceId").text()

    showHighlightedEvidenceDetails()
})


/**
 * On the click of a web link name, a new tab is opened. The tab goes to the link associated with the web link.
 */
$(document).on('click', '.addedWebLinkName', function () {
    let destination = $(this).parent().find(".addedWebLinkUrl")[0].innerHTML
    window.open(destination, '_blank').focus();
})

//---- Tooltip Refresher----


/**
 * Refresh tooltip display
 */
function initialiseTooltips() {
    $('[data-bs-toggle="tooltip"]').tooltip();
}