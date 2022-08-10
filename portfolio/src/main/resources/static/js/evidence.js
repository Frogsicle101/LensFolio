let webLinksCount = 0;

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
    let textInput = $(".text-input");
    textInput.each(countCharacters)
    textInput.keyup(countCharacters)
    checkToShowSkillChips()
    getSkills()
})


/**
 * When the page loads this makes a call to the server to get a list of the users skills they already have
 * this helps the autocomplete functionality on the skill input
 */
function getSkills() {
    $.ajax({
        url: "skills?userId=" + userBeingViewedId,
        type: "GET",
        success: function (response) {
            response.forEach(skill => {
                skillsArray.push(skill.name.replaceAll(" ", "_"));
            })
        }, error: function (response) {
            // Log this
        }
    })
}


/**


/**
 * Check the number of Weblink, if it is more than 9, then the Add Web Link button not show
 */
function checkWeblinkCount() {
    let addWeblinkButton = $("#addWebLinkButton")
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
 * reset the weblinks count
 */
function resetWeblink() {
    let addWeblinkButton = $("#addWebLinkButton")
    let weblinkFullTab = $("#webLinkFull")

    addWeblinkButton.show()
    weblinkFullTab.hide()
    webLinksCount = 0
}


/**
 * Retrieves the added web links and creates a list of them in DTO form.
 *
 * @returns {string} A list of web links matching the web link DTO format.
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
 * Saves the evidence input during creating a new piece of evidence
 */
$(document).on("click", "#evidenceSaveButton", function (event) {
    event.preventDefault()
    removeDuplicatesFromInput($("#skillsInput"))
    let evidenceCreationForm = $("#evidenceCreationForm")[0]
    if (!evidenceCreationForm.checkValidity()) {
        evidenceCreationForm.reportValidity()
    } else {
        const title = $("#evidenceName").val()
        const date = $("#evidenceDate").val()
        const description = $("#evidenceDescription").val()
        const projectId = 1
        let webLinks = getWeblinksList()

        const skills = $("#skillsInput").val().split(" ")
        skillsArray = skillsArray.concat(skills);
        $.each(skills, function (i) {
            skills[i] = skills[i].replaceAll("_", " ")
        })

        const categories = getCategories();
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
            url: `evidence`, type: "POST", contentType: "application/json", data, success: function (response) {
                selectedEvidenceId = response.id
                getAndAddEvidencePreviews()
                createAlert("Created evidence")
                $("#addEvidenceModal").modal('hide')
                clearAddEvidenceModalValues()
                disableEnableSaveButtonOnValidity() //Gets run to disable the save button on form clearance.
                $(".address-alert").alert('close') // Close any web link alerts
                $(".weblink-name-alert").alert('close')
                resetWeblink()
            }, error: function (error) {
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
        let address = $("#webLinkUrl").val()
        let alias = $("#webLinkName").val()
        let form = $(".webLinkForm")
        console.log(address)
        validateWebLink(form, alias, address)
    } else {
        webLinkButtonToggle()
    }
})


/**
 * Listen for a keypress in the weblink address field, and closes the alert box
 */
$(document).on('keypress', '#webLinkUrl', function () {
    $(".address-alert").alert('close')
})


/**
 * Listen for a keypress in the weblink name field, and closes the alert box
 */
$(document).on('keypress', '#webLinkName', function () {
    $(".weblink-name-alert").alert('close')
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
        if (event.keyCode === $.ui.keyCode.TAB && $(this).autocomplete("instance").menu.active) {
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
    if (event.keyCode === $.ui.keyCode.DELETE) {
        event.preventDefault();
        let inputArray = skillsInput.val().trim().split(/\s+/)
        inputArray.pop()
        skillsInput.val(inputArray.join(" "))
    }
    if (event.keyCode === $.ui.keyCode.SPACE || event.keyCode === $.ui.keyCode.TAB || event.keyCode === $.ui.keyCode.ENTER) {
        removeDuplicatesFromInput(skillsInput)
    }
    displaySkillChips()
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
 * The below listeners trigger the rendering of the skill chips
 */
$(document).on("change", "#skillsInput", () => displaySkillChips())
$(document).on("click", ".ui-autocomplete", () => {
    removeDuplicatesFromInput($("#skillsInput"))
    displaySkillChips()
})


/**
 * Cleans up the duplicates in the input when the user clicks away from the input.
 */
$(document).on("click", () => {
    removeDuplicatesFromInput($("#skillsInput"))
    displaySkillChips()
})


/**
 * This function gets the input string from the skills input and trims off the extra whitespace
 * then it separates each word into an array and creates chips for them.
 */
function displaySkillChips() {
    checkToShowSkillChips()
    let skillsInput = $("#skillsInput")
    let inputArray = skillsInput.val().trim().split(/\s+/)
    let chipDisplay = $("#skillChipDisplay")
    chipDisplay.empty()
    inputArray.forEach(function (element) {
        element = element.split("_").join(" ")
        chipDisplay.append(createChip(sanitise(element)))
    })
    chipDisplay.find(".skillChipText").each(function () {
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
 * @param element the name of the skill
 * @returns {string} the html for the chip
 */
function createChip(element) {
    return `<div class="skillChip">
                <p class="skillChipText">${element}</p>  
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
$(document).on("click", ".chipDelete", function () {
    let skillText = $(this).parent().find(".skillChipText").text().trim().split(" ").join("_")
    let skillsInput = $("#skillsInput")
    let inputArray = skillsInput.val().trim().split(/\s+/).filter(function (value) {
        return value.toLowerCase() !== skillText.toLowerCase()
    })
    skillsInput.val(inputArray.join(" "))
    displaySkillChips()
})




// --------------------------- Functional HTML Components ------------------------------------


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
 * Handles a web link validated by the back end.
Validates the alias and then displays an error message or saves the web link and toggles the web link form.
 */
function validateWebLink(form, alias, address) {
    //Do some title validation
    if (alias.length === 0) {
        $(".weblink-name-alert").alert('close') //Close any previous alerts
        form.append(`
                    <div class="alert alert-danger alert-dismissible show weblink-name-alert" role="alert">
                      Please include a name for your web link
                      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    `)
    } else if (address.search("://") === -1) {
        $(".address-alert").alert('close') //Close any previous alerts
        form.append(`
                    <div class="alert alert-danger alert-dismissible show address-alert" role="alert">
                      That address is missing a "://" - did you make a typo?
                      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                    `)
    }
    else {
        validateWebLinkAtBackend()
    }
}


/**
 * Handles the error messages for an invalid web link.
 */
function handleInvalidWebLink(form, error) {
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


/**
 * Validates the weblink server-side.
 * Takes the URL and makes a call to the server to check if it's valid.
 * If valid, save the web link and toggle the form.
 *
 * If there's an issue, or it's not valid, calls a function to display an alert
 */
function validateWebLinkAtBackend() {
    let address = $("#webLinkUrl").val()
    let form = $(".webLinkForm")
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
    let button = $(".addWebLinkButton");
    $(".webLinkForm").slideToggle();
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
        createAlert("Weblink name needs to be 1 char", true);
    }
}


/**
 * Clears all fields (except the date field) in the "Add Evidence" form.
 */
function clearAddEvidenceModalValues() {
    $("#evidenceName").val("")
    $("#evidenceDescription").val("")
    $("#webLinkUrl").val("")
    $("#webLinkName").val("")
    $("#addedWebLinks").empty()
    $("#webLinkTitle").empty()
    $("#skillsInput").val("")
    $(".btn-success").addClass("btn-secondary").removeClass("btn-success")
    $(".evidenceCategoryTickIcon").hide();
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


/**
 * Checks that the name and description of a piece of evidence match the required regex.
 */
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

