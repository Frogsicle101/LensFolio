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

let skillsArray = ["ActionScript", "AppleScript", "Asp", "BASIC", "C", "C++", "Clojure", "COBOL", "ColdFusion", "Erlang", "Fortran", "Groovy", "Haskell", "Java", "JavaScript", "Lisp", "Perl", "PHP", "Python", "Ruby", "Scala", "Scheme"]


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

    for (let index in response) {
        let webLink = response[index]
        webLinksDiv.append(webLinkElement(webLink.url, webLink.alias))
    }
    initialiseTooltips()
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
            console.log(response)
            for (let skill of response) {
                if (!skillsArray.includes(skill.name)) {
                    skillsArray.push(skill.name)
                }
                callback()
            }
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


//---- Tooltip Refresher----


/**
 * Refresh tooltip display
 */
function initialiseTooltips() {
    $('[data-bs-toggle="tooltip"]').tooltip();
}