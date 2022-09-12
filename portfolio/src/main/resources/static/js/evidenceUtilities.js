/**
 * A JS file that contains utilities for pieces of evidence,
 * that can be used across multiple pages.
 */

/** A regex only allowing English characters, numbers, hyphens and underscores */
const regexSkills = new RegExp("[A-Za-z0-9_-]+");

/** the user id of the user whose evidence page if being viewed */
let userBeingViewedId;

/** The id of the piece of evidence being displayed. */
let selectedEvidenceId;

/** WebLinksCount is used to restrict the amount of weblinks on a piece of evidence*/
let webLinksCount = 0;

/** The existing skills of the user, updated as the user's evidence is retrieved */
let skillsArray = []

/** Provides the options of categories and maps them to user-friendly strings */
let categoriesMapping = new Map([
    ["QUALITATIVE", "Qualitative"],
    ["QUANTITATIVE", "Quantitative"],
    ["SERVICE", "Service"]
])

$(() => {
        // Counting characters
        let textInput = $(".text-input");
        textInput.each(countCharacters)
        textInput.on("keyup", countCharacters)
    }
)


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
    let urlSlashed
    if (slashIndex > 1) {
        urlSlashed = url.slice(slashIndex) // Cut off the http:// or whatever else it might be
    } else {
        urlSlashed = url // The url does not have a protocol attached to it
    }

    return (`
        <div class="webLinkElement ${security}" data-value="${sanitise(url)}" >
            ${icon}
            <div class="addedWebLinkName" data-bs-toggle="tooltip" data-bs-placement="top" 
            data-bs-title="${urlSlashed}" data-bs-custom-class="webLinkTooltip">${sanitise(alias)}</div>
            <div class="addedWebLinkUrl" style="display: none">${sanitise(url)}</div>
        </div>
    `)
}


// --------------------------- Server Queries ------------------------------------


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

    let title = $(document).find(".evidenceTitle").first()
    title.text("Evidence");
    $(".selected").removeClass("selected")

    $.ajax({
        url: "evidenceData?userId=" + userBeingViewedId,
        success: function (response, status, xhr) {
            displayNameOrButton(xhr)
            addEvidencePreviews(response)
            updateSelectedEvidence();
            showHighlightedEvidenceDetails()
        }, error: function () {
            createAlert("Could not retrieve evidence data", "failure")
        }
    })
}


/**
 *  Displays the create evidence button if the evidence being viewed is the logged-in user otherwise it displays the
 *  name of the user
 */
function displayNameOrButton(response) {
    let nameHolder = $("#nameHolder")
    if (userBeingViewedId !== userIdent) {
        $("#createEvidenceButton").remove();
        let usersName = response.getResponseHeader("Users-Name");
        nameHolder.html("Viewing evidence for " + usersName)
        nameHolder.show()
    } else{
        nameHolder.hide()
        $("#createEvidenceButton").show();
    }
}


/**
 * This is called to show the evidence details for the selected piece of evidence.
 *
 * If the selectedEvidenceId is null or the server cannot find the evidence, it selected the first
 * piece of evidence in the table, and sets the details to that. If there is no evidence, the appropriate
 * message is displayed.
 */
function getHighlightedEvidenceDetails() {
    if (selectedEvidenceId !== "") {
        $.ajax({
            url: "evidencePiece?evidenceId=" + selectedEvidenceId, success: function (response) {
                setHighlightEvidenceAttributes(response)
                getHighlightedEvidenceWeblinks()
            }, error: function (error) {
                console.log(error)
                createAlert("Failed to receive active evidence", "failure")
            }
        })
    } else {
        setDetailsToNoEvidenceExists()
    }
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
                createAlert("Failed to receive evidence links", "failure")
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
function getSkills(callback = () => {
}) {
    $.ajax({
        url: "skills?userId=" + userBeingViewedId, type: "GET",
        success: function (response) {
            skillsArray = []
            $.each(response, function (i) {
                if (!skillsArray.includes(response[i].name)) {
                    skillsArray.push(response[i].name)
                }
            })
            callback()
        },
        error: function (response) {
            console.log(response)
        }
    })
}


// --------------------------- Functional HTML Components ------------------------------------


/**
 *  A helper function to take a response from an ajax call and add it to the array of skills
 */
function addSkillResponseToArray(response) {
    let skills = []
    for (let i in response.skills) {
        skills.push(response.skills[i].name)
    }
    skillsArray = [...new Set(skillsArray.concat(skills))];
}


/**
 * Sets the evidence details (big display) values to the given piece of evidence.
 *
 * @param evidenceDetails The title, date, and description, skills, and categories for a piece of evidence.
 */
function setHighlightEvidenceAttributes(evidenceDetails) {
    let highlightedEvidenceId = $("#evidenceDetailsId")
    let highlightedEvidenceTitle = $("#evidenceDetailsTitle")
    let highlightedEvidenceDate = $("#evidenceDetailsDate")
    let highlightedEvidenceDescription = $("#evidenceDetailsDescription")

    highlightedEvidenceId.text(evidenceDetails.id)
    highlightedEvidenceTitle.text(evidenceDetails.title)
    highlightedEvidenceDate.text(evidenceDetails.date)
    highlightedEvidenceDescription.text(evidenceDetails.description)
    addSkillsToEvidence(evidenceDetails.skills)

    highlightedEvidenceTitle.show()
    highlightedEvidenceDate.show()
    highlightedEvidenceDescription.show()
    addCategoriesToEvidence(evidenceDetails.categories)

    if (userBeingViewedId === userIdent) {
        $("#deleteEvidenceButton").show()
    } else {
        $("#deleteEvidenceButton").hide()
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

    // Sorts in alphabetical order
    skills.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1)
    if (skills.length < 1) {
        highlightedEvidenceSkills.append(createSkillChip("No Skill", false))
    } else {
        $.each(skills, function (i) {
            highlightedEvidenceSkills.append(createSkillChip(skills[i].name, false))
        })
    }
}


/**
 * A function to display all the categories for a piece of evidence
 *
 * @param categories A list of categories associated with a piece of evidence
 */
function addCategoriesToEvidence(categories) {
    let highlightedEvidenceCategories = $("#evidenceDetailsCategories")
    highlightedEvidenceCategories.empty();
    $.each(categories, function (category) {
        let categoryText = categoriesMapping.get(categories[category]);
        highlightedEvidenceCategories.append(createCategoryChip(categoryText, false))
    })
}


/**
 * Creates and returns an HTML element for an evidence preview
 *
 * @param evidence - A json object for a piece of evidence
 * @return the HTML component for previewing evidence of class evidenceListItem
 */
function createEvidencePreview(evidence) {
    let skills = getSkillTags(evidence.skills)
    let categories = getCategoryTags(evidence.categories)
    return `
        <div class="box evidenceListItem ${evidence.id === selectedEvidenceId ? 'selectedEvidence' : ''}">
            <div class="row evidenceListItemHeader">
                <p class="evidenceId" style="display: none">${sanitise(evidence.id)}</p>
                <p class="col evidenceListItemTitle">${sanitise(evidence.title)}</p>
                <p class="col evidenceListItemDate">${sanitise(evidence.date)}</p>
            </div>
            <div class="evidencePreviewTags categoryChipDisplay">${categories}</div>
            <div class="evidencePreviewTags skillChipDisplay">${skills}</div>
        </div>`
}


/**
 * Produces the HTML for the skill chip for each skill in the provided skills list.
 *
 * @param skills The skills to be added to the result.
 * @returns {string} HTMl to render the given skill names as skill chips.
 */
function getSkillTags(skills) {
    skills.sort((a, b) => a.name.toLowerCase() > b.name.toLowerCase() ? 1 : -1)
    let skillsHTML = ``
    $.each(skills, function (i) {
        skillsHTML += createSkillChip(skills[i].name, false)
    })
    return skillsHTML
}


/**
 * Produces the HTML for the skill chip for each category in the categories.
 *
 * @param categories The categories to be added to the result.
 * @returns {string} HTMl to render the given category names as skill chips.
 */
function getCategoryTags(categories) {
    categories.sort((a, b) => a.toLowerCase() > b.toLowerCase() ? 1 : -1)
    let categoriesHTML = ``
    $.each(categories, function (i) {
        categoriesHTML += createCategoryChip(categoriesMapping.get(categories[i]), false)
    })
    return categoriesHTML
}


/**
 * Hides the date and description fields and sets the Title field to no information.
 *
 * This function is called when the page is rendered and no evidence exists.
 */
function setDetailsToNoEvidenceExists() {
    let highlightedEvidenceTitle = $("#evidenceDetailsTitle")

    highlightedEvidenceTitle.text("No Evidence")
    highlightedEvidenceTitle.show()
    $("#evidenceDetailsDate").hide()
    $("#evidenceDetailsDescription").hide()
    $("#deleteEvidenceButton").hide()
    $("#evidenceDetailsCategories").empty()
    $("#evidenceWebLinks").empty()
    $("#evidenceDetailsSkills").empty()

}


//---- Tooltip Refresher----


/**
 * Refresh tooltip display
 */
function initialiseTooltips() {
    $('[data-bs-toggle="tooltip"]').tooltip();
}


/**
 * Check the number of Weblink, if it is more than 9, then the Add Web Link button not show
 */
function checkWeblinkCount() {
    let addWeblinkButton = $("#addWeblinkButton")
    let weblinkFullTab = $("#webLinkFull")
    if (webLinksCount > 9) {
        addWeblinkButton.hide()
        weblinkFullTab.show()
    } else {
        addWeblinkButton.show()
        weblinkFullTab.hide()
    }
}


/**
 * Resets the weblink count
 */
function resetWeblink() {
    let addWeblinkButton = $("#addWeblinkButton")
    let weblinkFullTab = $("#webLinkFull")
    addWeblinkButton.show()
    weblinkFullTab.hide()
    webLinksCount = 0
}


/**
 * Retrieves the added web links and creates a list of them in DTO form.
 *
 * @returns {*[]} A list of web links matching the web link DTO format.
 */
function getWeblinksList() {
    let evidenceCreationForm = $("#evidenceCreationForm")
    let weblinks = evidenceCreationForm.find(".webLinkElement")
    let weblinksList = []

    $.each(weblinks, function () {
        let weblinkDTO = {
            "url": this.querySelector(".addedWebLinkUrl").innerHTML,
            "name": this.querySelector(".addedWebLinkName").innerHTML
        }
        weblinksList.push(weblinkDTO)
    })
    return weblinksList
}


/**
 * Gets all the selected categories from the categories form
 *
 * @return a list of categories e.g., ["SERVICE", "QUANTITATIVE"]
 */
function getCategories() {
    let categoryButtons = $("#evidenceFormCategories")
    let selectedButtons = categoryButtons.find(".btn-success")
    let categories = []

    $.each(selectedButtons, function (button) {
        categories.push($(selectedButtons[button]).val())
    })
    return categories
}


// --------------------------------- Click listeners -----------------------------------------


/**
 * Toggles category button appearance on the evidence creation form.
 */
$(".evidenceFormCategoryButton").on("click", function () {
    let button = $(this)
    if (button.hasClass("btn-secondary")) {
        button.removeClass("btn-secondary")
        button.addClass("btn-success")
        button.find(".evidenceCategoryTickIcon").show("slide", 200)
    } else {
        button.removeClass("btn-success")
        button.addClass("btn-secondary")
        button.find(".evidenceCategoryTickIcon").hide("slide", 200)
    }
})


/**
 * The below listener trigger the rendering of the skill chips
 */
$(document).on("click", ".ui-autocomplete", () => {
    removeDuplicatesFromInput($("#skillsInput"))
    displayInputSkillChips()
})


/**
 * Cleans up the duplicates in the input when the user clicks away from the input.
 */
$(document).on("click", () => {
    removeDuplicatesFromInput($("#skillsInput"))
    displayInputSkillChips()
})


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


/**
 * Listen for a keypress in the weblink address field, and closes the alert box
 */
$(document).on('keypress', '#webLinkUrl', function () {
    $("#weblinkAddressAlert").alert('close')
})


/**
 * Listen for a keypress in the weblink name field, and closes the alert box
 */
$(document).on('keypress', '#webLinkName', function () {
    $("#weblinkNameAlert").alert('close')
})


/**
 * Listens for a click on the chip delete buttons, removes all the elements from the skill input that match the
 * skill we are deleting.
 */
$(document).on("click", ".chipDelete", function () {
    let skillText = $(this).parent().find(".skillChipText").text().trim().split(" ").join("_")
    let skillsInput = $("#skillsInput")
    let inputArray = skillsInput.val().trim().split(/\s+/).filter(function (value) {
        return value.toLowerCase() !== skillText.toLowerCase()
    })
    skillsInput.val(inputArray.join(" "))
    displayInputSkillChips()
})


// --------------------------------- Autocomplete -----------------------------------------


/** This split function splits the text by its spaces*/
function split(val) {
    return val.split(/\s+/);
}


/** this function splits the input by its spaces then returns the last word */
function extractLast(term) {
    return split(term).pop();
}


/**
 * Autocomplete widget provided by jQueryUi
 * https://jqueryui.com/autocomplete/
 */
$("#skillsInput")
    // don't navigate away from the field on tab when selecting an item
    .on("keydown", function (event) {
        if (event.key === $.ui.keyCode.TAB && $(this).autocomplete("instance").menu.active) {
            event.preventDefault();
        }
    })
    .autocomplete({
        autoFocus: true, // This default selects the top result
        minLength: 1,
        source: function (request, response) {
            // delegate back to autocomplete, but extract the last term
            let responseList = $.ui.autocomplete.filter(skillsArray, extractLast(request.term))
            response(responseList.sort((element1, element2) => {
                // This sorts the response list (the drop-down list) so that it shows the shortest match first
                return element1.length - element2.length
            }));
        },
        focus: function () {
            // prevent value inserted on focus
            return false;
        },
        select: function (event, ui) {
            let terms = split(this.value);
            // remove the current input
            terms.pop();
            // add the selected item
            terms.push(ui.item.value);
            // add placeholder to get the space at the end
            terms.push("");
            this.value = terms.join(" ");
            return false;
        },
        appendTo: ".modal-content"
    })
    .data('ui-autocomplete')._renderItem = function (ul, item) {
    //This handles the display of the drop-down menu.
    return $("<li></li>")
        .data("ui-autocomplete-item", item)
        .append('<a>' + item.label + '</a>')
        .appendTo(ul);
};


/**
 * Autocomplete widget provided by jQueryUi
 * https://jqueryui.com/autocomplete/
 */
$("#linkUsersInput")
    .autocomplete({
        autoFocus: true, // This default selects the top result
        minLength: 1,
        delay: 700,
        appendTo: ".modal-content",
        source: function (request, response) {
            $.ajax({
                url: 'filteredUsers?name=' + request.term.toString(), type: "GET", contentType: "application/json", success: function (res) {
                    let users = [];
                    $.each(res, function (i) {
                        let user = {label: `${res[i].firstName} ${res[i].lastName}`, value: res[i] }
                        users.push(user)
                    })
                    response(users)
                }, error: function (error) {
                    createAlert(error.responseText, "failure", ".modal-body")
                }
            })
        },
        focus: function () {
            // prevent value inserted on focus
            return false;
        },
        select: function (event, ui) {
            let terms = split(this.value);
            // remove the current input
            terms.pop();
            // add the selected item
            let user = ui.item.value
            addLinkedUser(user);
            $(this).val('')
            return false;
        }
    })
    .data('ui-autocomplete')._renderItem = function (ul, item) {
    //This handles the display of the drop-down menu.
    return $("<li></li>")
        .data("ui-autocomplete-item", item)
        .append('<a>' + item.label + '</a>')
        .appendTo(ul);
};


/**
 * Listens out for a keyup event on the skills input.
 * If it is a delete button keydown then it removes the last word from the input box.
 * If it is a space, tab or enter then it checks for duplicates
 */
$(document).on("keyup", "#skillsInput", function (event) {
    let skillsInput = $("#skillsInput")
    if (event.key === "Backspace") {
        event.preventDefault();
        let inputArray = skillsInput.val().trim().split(/\s+/)
        inputArray.pop()
        skillsInput.val(inputArray.join(" "))
    }
    if (event.key === " " || event.key === "Tab" || event.key === "Enter") {
        removeDuplicatesFromInput(skillsInput)
    }
    displayInputSkillChips()
})


/**
 * Runs the remove duplicates function after a paste event has occurred on the skills input
 */
$(document).on("paste", "#skillsInput", () => {
    setTimeout(() => removeDuplicatesFromInput($("#skillsInput")), 0)
    // Above is in a timeout so that it runs after the paste event has happened
})


/**
 * Splits the input into an array and then creates a new array and pushed the elements too it if they don't already
 * exist in it, it checks for case insensitivity as well.
 *
 * @param input the jQuery call to the input to check
 */
function removeDuplicatesFromInput(input) {
    let inputArray = input.val().trim().split(/\s+/)
    let newArray = []

    inputArray.forEach(function (element) {
        if (regexSkills.test(element)) {
            while (element.slice(-1) === "_") {
                element = element.slice(0, -1)
            }
            while (element.slice(0, 1) === "_") {
                element = element.slice(1, element.length)
            }
            element = element.replaceAll("_", " ")
                .replace(/\s+/g, ' ')
                .trim()
                .replaceAll(" ", "_")
            if (element.length > 30) { //Shortens down the elements to 30 characters
                element = element.split("").splice(0, 30).join("")
            }
            if (!(newArray.includes(element) || newArray.map((item) => item.toLowerCase()).includes(element.toLowerCase()))) {
                newArray.push(element)
            }
        }
    })

    newArray.forEach(function (element, index) {
        skillsArray.forEach(function (alreadyExistingSkill) {
            if (element.toLowerCase() === alreadyExistingSkill.toLowerCase()) {
                newArray[index] = alreadyExistingSkill;
            }
        })
    })

    input.val(newArray.join(" ") + " ")
}


/**
 * Triggers the rendering of the skill chips
 */
$(document).on("change", "#skillsInput", () => displayInputSkillChips())
$(document).on("click", ".ui-autocomplete", () => {
    removeDuplicatesFromInput($("#skillsInput"))
    displayInputSkillChips()
})


/**
 * Cleans up the duplicates in the input when the user clicks away from the input.
 */
$(document).on("click", () => {
    removeDuplicatesFromInput($("#skillsInput"))
    displayInputSkillChips()
})


/**
 * This function gets the input string from the skills input and trims off the extra whitespace
 * then it separates each word into an array and creates chips for them.
 */
function displayInputSkillChips() {
    checkToShowSkillChips()
    let skillsInput = $("#skillsInput")
    let inputArray = skillsInput.val().trim().split(/\s+/)
    let chipDisplay = $("#skillChipDisplay")
    chipDisplay.empty()
    inputArray.forEach(function (element) {
        element = element.split("_").join(" ")
        chipDisplay.append(createDeletableSkillChip(sanitise(element)))
    })
    chipDisplay.find(".chipText").each(function () {
        if ($(this).text().length < 1) {
            $(this).parent(".skillChip").remove()
        }
        if ($(this).text().length > 30) {
            $(this).parent(".skillChip").addClass("skillChipInvalid")
        }
    })
}


/**
 * Simple function that checks if the skill chip display should be visible or not.
 */
function checkToShowSkillChips() {
    let chipDisplay = $("#skillChipDisplay")
    let skillsInput = $("#skillsInput")
    if (skillsInput.val().trim().length > 0) {
        chipDisplay.show()
    } else {
        chipDisplay.hide()
    }
}


/**
 * This function returns the html for the chips
 *
 * @param element the name of the skill
 * @returns {string} the html for the chip
 */
function createDeletableSkillChip(element) {
    return `<div class="chip skillChip">
                <p class="chipText">${sanitise(element)}</p>  
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x-circle chipDelete" viewBox="0 0 16 16">
                            <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                </svg>
                </div>`
}


/**
 * Listens for a click on the chip delete buttons, removes all the elements from the skill input that match the
 * skill we are deleting.
 */
$(document).on("click", ".chipDelete", function (event) {
    event.stopPropagation()
    let skillText = $(this).parent().find(".chipText").text().trim().split(" ").join("_")
    let skillsInput = $("#skillsInput")
    let inputArray = skillsInput.val().trim().split(/\s+/).filter(function (value) {
        return value.toLowerCase() !== skillText.toLowerCase()
    })
    skillsInput.val(inputArray.join(" "))
    displayInputSkillChips()
})


/**
 * Due to a weird bug where the page would reload if you closed an alert if the
 * alert was open in a modal, this was added to stop the form from submitting which
 * seemed to be the cause of the issue.
 */
$(document).on("submit", "#evidenceCreationForm", function(e) {
    e.preventDefault()
})


/**
 * Saves the evidence input during creating a new piece of evidence
 */
$(document).on("click", "#evidenceSaveButton", function (event) {
    event.preventDefault()
    let skillsInput = $("#skillsInput")
    removeDuplicatesFromInput(skillsInput)
    let evidenceCreationForm = $("#evidenceCreationForm")[0]

    if (!evidenceCreationForm.checkValidity()) {
        evidenceCreationForm.reportValidity()
    } else {
        const title = $("#evidenceName").val()
        const date = $("#evidenceDate").val()
        const description = $("#evidenceDescription").val()
        const projectId = 1
        let webLinks = getWeblinksList();
        const categories = getCategories();

        const skills = skillsInput.val().split(" ").filter(skill => skill.trim() !== "")
        $.each(skills, function (i) {
            skills[i] = skills[i].replaceAll("_", " ")
        })

        let data = JSON.stringify({
            "title": title,
            "date": date,
            "description": description,
            "projectId": projectId,
            "webLinks": webLinks,
            "skills": skills,
            "categories": categories
        })
        $.ajax({
            url: 'evidence', type: "POST", contentType: "application/json", data, success: function (response) {
                selectedEvidenceId = response.id
                getAndAddEvidencePreviews()
                addSkillResponseToArray(response)
                addSkillsToSideBar();
                createAlert("Created evidence", "success")
                closeModal()
                clearAddEvidenceModalValues()
                disableEnableSaveButtonOnValidity() //Gets run to disable the save button on form clearance.
                $("#weblinkAddressAlert").alert('close') // Close any web link alerts
                $("#weblinkNameAlert").alert('close')
                resetWeblink()
            }, error: function (error) {
                createAlert(error.responseText, "failure", ".modal-body")
            }
        })
    }
})


/**
 * Listens for when add web link button is clicked.
 * Slide-toggles the web link portion of the form.
 */
$(document).on('click', '#addWeblinkButton', function () {
    let button = $("#addWeblinkButton");
    if (button.hasClass("toggled")) {
        //validate the link
        let address = $("#webLinkUrl").val()
        let alias = $("#webLinkName").val()
        let form = $("#weblinkForm")
        validateWebLink(form, alias, address)
    } else {
        webLinkButtonToggle()
    }
})


/**
 * Closes the add weblink form and resets weblink form buttons when the weblink add is cancelled.
 */
$(document).on('click', '#cancelWeblinkButton', () => {
    webLinkButtonToggle()
})


/**
 * Prevents the add evidence modal from being closed if an alert is present.
 */
$('#addEvidenceModal').on('hide.bs.modal', function (e) {
    let alert = $("#alertPopUp")
    if (alert.is(":visible") && alert.hasClass("backgroundRed") ){
        alert.effect("shake")
        e.preventDefault();
        e.stopPropagation();
        return false;
    }
});


/**
 * Handles a web link validated by the back end.
 Validates the alias and then displays an error message or saves the web link and toggles the web link form.
 */
function validateWebLink(form, alias, address) {
    if (alias.length === 0) {
        $("#weblinkNameAlert").alert('close') //Close any previous alerts
        form.append(`
                    <div id="weblinkNameAlert" class="alert alert-danger alert-dismissible show weblinkAlert" role="alert">
                      Please include a name for your web link
                      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    `)
    } else if (address.search("://") === -1) {
        $("#weblinkAddressAlert").alert('close') //Close any previous alerts
        form.append(`
                    <div id="weblinkAddressAlert" class="alert alert-danger alert-dismissible show weblinkAlert" role="alert">
                      That address is missing a protocol (the part that comes before "://") - did you make a typo?
                      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    `)
    } else {
        validateWebLinkAtBackend()
    }
}


/**
 * Handles the error messages for an invalid web link.
 */
function handleInvalidWebLink(form, error) {
    $("#weblinkAddressAlert").alert('close') //Close any previous alerts
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


/**
 * Validates the weblink server-side.
 * Takes the URL and makes a call to the server to check if it's valid.
 * If valid, save the web link and toggle the form.
 *
 * If there's an issue, or it's not valid, calls a function to display an alert
 */
function validateWebLinkAtBackend() {
    let address = $("#webLinkUrl").val()
    let form = $("#weblinkForm")
    let data = JSON.stringify({
        "url": address,
        "name": $("#webLinkName").val()
    })
    $.ajax({
        url: `validateWebLink`,
        type: "POST",
        contentType: "application/json",
        data,
        success: () => {
            submitWebLink()
            webLinkButtonToggle()
        },
        error: (error) => {
            handleInvalidWebLink(form, error)
        }
    })
}


/**
 * Toggles the add weblink button,
 * and slide-toggles the form
 */
function webLinkButtonToggle() {
    let saveButton = $("#addWeblinkButton");
    let cancelButton = $("#cancelWeblinkButton")
    $("#weblinkForm").slideToggle();
    if (saveButton.hasClass("toggled")) {
        saveButton.text("Add Web Link")
        saveButton.removeClass("toggled")
        saveButton.removeClass("btn-primary")
        saveButton.addClass("btn-secondary")
        $(".weblinkAlert").alert('close')
        cancelButton.hide()
    } else {
        saveButton.text("Save Web Link")
        saveButton.addClass("toggled")
        saveButton.removeClass("btn-secondary")
        saveButton.addClass("btn-primary")
        cancelButton.show()
    }
}


/**
 * Toggles the add Linked Users button and slide-toggles the form
 */
$(document).on('click', '#linkUsersToEvidenceButton', function () {
    let linkedUsersForm = $("#linkUsersForm")
    let linkButton = $("#linkUsersToEvidenceButton");
    if (linkButton.hasClass("toggled")) {
        linkButton.text("Link Users")
        linkButton.removeClass("toggled")
    } else {
        linkButton.text("Cancel")
        linkButton.addClass("toggled")
    }
    linkedUsersForm.slideToggle();
})


/**
 * Appends a new link to the list of added links in the Add Evidence form.
 */
function submitWebLink() {
    let alias = $("#webLinkName")
    let url = $("#webLinkUrl")
    let addedWebLinks = $("#addedWebLinks")
    let webLinkTitle = $("#webLinkTitle")

    if (alias.val().length > 0) {
        webLinkTitle.show()
        addedWebLinks.append(webLinkElement(url.val(), alias.val()))
        initialiseTooltips()
        url.val("")
        alias.val("")
        webLinksCount += 1
        checkWeblinkCount()
        $('[data-bs-toggle="tooltip"]').tooltip(); //re-init tooltips so appended tooltip displays
    } else {
        createAlert("Weblink name needs to be 1 char", "failure");
    }
}


/**
 * Adds the user to be linked to the create evidence modal
 */
function addLinkedUser(user) {
    let linkedUsersDiv = $("#linkedUsers")
    $("#linkedUsersTitle").show()
    if ($('#linkedUserId' + user.id).length === 0) {
        linkedUsersDiv.append(linkedUserElement(user))
    }
}


/**
 * Creates the element for displaying the linked user
 */
function linkedUserElement(user) {
    return `<div id=linkedUserId${user.id}>${user.firstName} ${user.lastName} (${user.username})</div>`
}


/**
 * Clears all fields (except the date field) in the "Add Evidence" form.
 */
function clearAddEvidenceModalValues() {
    $("#evidenceName").val("")
    $("#evidenceDescription").val("")
    $("#webLinkUrl").val("")
    $("#webLinkName").val("")
    $("#evidenceDate").val(todaysDate)
    $("#addedWebLinks").empty()
    $("#linkedUsers").empty()
    $("#webLinkTitle").hide()
    $("#skillsInput").val("")
    $("#linkedUsersTitle").hide()
    $(".btn-success").addClass("btn-secondary").removeClass("btn-success")
    $(".evidenceCategoryTickIcon").hide();
    $(".countCharName").html("50 characters remaining")
    $(".countCharDescription").html("500 characters remaining")
}


// -------------------------------------- Validation -----------------------------------


/**
 * Checks the form is valid, enables or disables the save button depending on validity.
 */
function disableEnableSaveButtonOnValidity() {
    if ($("#evidenceCreationForm")[0].checkValidity()) {
        $("#evidenceSaveButton").prop("disabled", false)
    } else {
        $("#evidenceSaveButton").prop("disabled", true)
    }
}


/**
 * Checks that the name and description of a piece of evidence match the required regex.
 */
function checkTextInputRegex() {
    let name = $("#evidenceName")
    let description = $("#evidenceDescription")
    let nameVal = name.val()
    let descriptionVal = description.val()

    if (!regex.test(nameVal) || !regex.test(descriptionVal)) {
        $("#evidenceSaveButton").prop("disabled", true)
    }

    if (!regex.test(nameVal) && nameVal.length > 0) {
        name.addClass("invalid")
    } else {
        name.removeClass("invalid")
    }

    if (!regex.test(descriptionVal) && descriptionVal.length > 0) {
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


/**
 * Pops up a confirmation message on the click of evidence deletion. If the confirmation is accepted,
 * then the delete request is sent. On a successful request the page is reloaded and an alert is made.
 */
$(document).on("click", "#deleteEvidenceButton", function () {
    const evidenceId = $("#evidenceDetailsId").text()
    const evidenceName = $("#evidenceDetailsTitle").text()
    if (window.confirm(`Are you sure you want to delete the evidence \n${evidenceName}`)) {
        $.ajax({
            url: `evidence?evidenceId=${evidenceId}`,
            type: "DELETE",
            success: () => {
                selectedEvidenceId = null
                getAndAddEvidencePreviews()
                getSkills(addSkillsToSideBar)
                createAlert("Successfully deleted evidence: " + sanitise(evidenceName), "success")
            }, error: (response) => {
                createAlert(response.responseText, "failure")
            }
        })
    }
})


/**
 * Creates HTMl for a skill chip with the given skill name.
 *
 * @param skillName The name to be displayed in the skill chip.
 * @param isMenuItem Boolean value reflecting whether the chip will be displayed in the menu bar.
 * @returns {string} The string of HTMl representing the skill chip.
 */
function createSkillChip(skillName, isMenuItem) {
    if (isMenuItem) {
        return `
            <div id=${sanitise("skillCalled" + skillName.replaceAll(" ", "_"))} class="chip skillChip">
                <p class="chipText">${sanitise(skillName)}</p>
            </div>`
    } else {
        return `
            <div class="chip skillChip">
                <p class="chipText">${sanitise(skillName)}</p>
            </div>`
    }
}



/**
 * Creates HTMl for a category chip with the given category name.
 *
 * @param categoryName The name to be displayed in the category chip.
 * @param isMenuItem Boolean value reflecting whether the chip will be displayed in the menu bar.
 * @returns {string} The string of HTMl representing the category chip.
 */
function createCategoryChip(categoryName, isMenuItem) {
    if (isMenuItem) {
        return `
            <div id=${sanitise("categoryCalled" + categoryName.replaceAll(" ", "_"))} class="chip categoryChip">
                <p class="chipText">${sanitise(categoryName)}</p>
            </div>`
    } else {
        return `
            <div class="chip categoryChip">
                <p class="chipText">${sanitise(categoryName)}</p>
            </div>`
    }
}
