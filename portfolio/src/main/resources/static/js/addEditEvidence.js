
/** For adding the skills to as the chips are added. */
let skillsToCreate = []
const skillsInput = $("#skillsInput")
let oldInput = ""


/**
 * Adds a string to the skillsToCreate list if it is not present.
 * If there's a case-insensitive alternative in the skills array, use that instead.
 *
 * @param skillName Name of skill to be added
 * @returns {boolean} True if the skill was added, false otherwise
 */
function addUniqueSkill(skillName) {
    let skillNameFormatted = replaceWithStringFromSkillArray(skillName.replaceAll("_", " "))
    if (! skillsToCreate.includes(skillNameFormatted)) {
        if (skillNameFormatted.trim().length > 0) {
            skillsToCreate.push(skillNameFormatted)
            return true
        }
    }
    return false
}


/**
 * Checks that a skill name is between 1 and 30 characters (inclusive), and is not a reserved skill.
 * Creates error messages and adds error classes as required.
 *
 * @param inputValue The skill name ot be checked
 * @param showAlert Boolean value representing whether an alert will be shown on fail
 * @returns {boolean} True if the skill is valid, false otherwise
 */
function validateSkillInput(inputValue, showAlert) {
    if (inputValue.length > 30) {
        if (showAlert) {
            skillsInput.addClass("skillChipInvalid")
            createAlert("Maximum skill length is 30 characters", AlertTypes.Failure)
        }
        return false
    }
    if (inputValue.trim().length === 0) {
        return false
    }
    if (RESERVED_SKILL_TAGS.includes(inputValue.toLowerCase())) {
        if (showAlert) {
            skillsInput.addClass("skillChipInvalid")
            createAlert("This is a reserved tag and cannot be manually created", AlertTypes.Failure)
        }
        return false
    }
    skillsInput.removeClass("skillChipInvalid")
    removeAlert()
    return true
}


/**
 * Adds skill chips to the skill input.
 * Underscores are replaced with spaces.
 * Clears existing input.
 */
function updateSkillsInput() {
    skillsInput.val("")
    let chipDisplay = $("#tagInputChips")
    $('[data-toggle="tooltip"]').tooltip("hide")

    chipDisplay.empty()
    skillsToCreate.forEach(function (element) {
        element = element.replaceAll("_", " ");
        chipDisplay.append(createDeletableSkillChip(element))
    })
    oldInput = ""
}


/**
 * Takes an event, containing a keypress, and either adds the last skill as a tag, removes the skill from the input, or
 * does nothing.
 * Deletes if backspace is pressed.
 * Adds as a tag if space bar, enter, or tab is pressed.
 *
 * @param event The event containing the key press value
 */
function handleSkillInputKeypress(event) {
    const inputValue = skillsInput.val().trim()
    const isValidSkillName = validateSkillInput(inputValue, true)
    let needsUpdate = false

    if (event.key === "Backspace" && oldInput.length === 0 && skillsToCreate.length > 0) {
        skillsToCreate.pop()
        needsUpdate = true
    }

    if (event.key === " " || event.key === "Enter" || event.key === "Tab" ) {
        if (isValidSkillName) {
            needsUpdate = addUniqueSkill(inputValue)
        }
        skillsInput.removeClass("skillChipInvalid")
        skillsInput.val("")
    }
    oldInput = inputValue
    if (needsUpdate) {
       updateSkillsInput()
    }
}


/**
 * Makes a tag/deletes the input for each input in the skill input.
 * Inputs are split by whitespace characters, and verified as valid skills.
 * If any skills are invalid, an error message displays. If there are 4 or less invalid skills, the skill names (trimmed
 * to 30 chars) are displayed. Otherwise, a message telling the user the number of removed inputs is displayed.
 */
function handleSkillInputPaste() {
    const inputValues = skillsInput.val().trim().split(/\s+/)
    const invalidSkillNames = new Set()

    inputValues.forEach(skillName => {
        if (validateSkillInput(skillName, false)) {
            addUniqueSkill(skillName)
        } else {
            invalidSkillNames.add(skillName.length > 30 ? skillName.substring(0, 27) + "..." : skillName)
        }
    })

    updateSkillsInput()
    skillsInput.val("")
    console.log(invalidSkillNames.size)
    if (invalidSkillNames.size > 0) {
        if (invalidSkillNames.size < 5) {
            let skillNamesString = []
            invalidSkillNames.forEach( (el) => {
                skillNamesString.push("\n" + el)
            })
            createAlert("Invalid skill(s) not added: " + skillNamesString, AlertTypes.Failure)
        } else {
            createAlert("Discarded " + invalidSkillNames.size + " invalid skills", AlertTypes.Failure)
        }
    }
}


/**
 * Removes the selected skill from the skills input and from the list of skills to be saved.
 *
 * @param event The click event on the skill delete button.
 */
function handleChipDelete(event) {
    event.stopPropagation()
    const skillName = $(this).siblings(".chipText").text()
    skillsToCreate = skillsToCreate.filter(addedSkill => addedSkill !== skillName)

    updateSkillsInput()
}



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
            if (element.match(emojiRegx)) {
                createAlert("Emojis not allowed in Skill name", AlertTypes.Failure)
            }
            if (element.length > 30) { //Shortens down the elements to 30 characters
                element = element.split("").splice(0, 30).join("")
                createAlert("Length of skill name should be less than 30", AlertTypes.Failure)
            }
            if (!(newArray.includes(element) || newArray.map((item) => item.toLowerCase()).includes(element.toLowerCase()))) {
                newArray.push(element)
            }
        } else if (element.length > 0) {
            createAlert("Skill names containing only special symbols are not allowed.", AlertTypes.Failure)
        }
    })

    newArray.forEach(function (element, index) {
        skillsArray.forEach(function (alreadyExistingSkill) {
            if (element.toLowerCase() === alreadyExistingSkill.toLowerCase()) {
                newArray[index] = alreadyExistingSkill;
            }
        })
    })

    input.val(newArray.join(" "))
}


// --------------------------------------------------- Autocomplete ----------------------------------------------------


/**
 * Autocomplete widget provided by jQueryUi
 * https://jqueryui.com/autocomplete/
 */
skillsInput
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
            let filteredSkills = $.ui.autocomplete.filter(skillsArray, extractLast(request.term))
            let existingSkills = [];
            $.each(skillsToCreate , function (i, element) {
                existingSkills.push(element.toLowerCase())
            })
            filteredSkills = filteredSkills.filter(element => !existingSkills.includes(element.toLowerCase()))
            response(filteredSkills.sort());
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
        appendTo: ".modalContent"
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
        appendTo: ".modalContent",
        source: function (request, response) {
            $.ajax({
                url: 'filteredUsers?name=' + request.term.toString(),
                type: "GET",
                contentType: "application/json",
                success: function (res) {
                    let users = [];
                    $.each(res, function (i) {
                        linkedUserIdsArray.push(userIdent)
                        if (!linkedUserIdsArray.includes(res[i].id)){
                            let user = {label: `${res[i].firstName} ${res[i].lastName}`, value: res[i]}
                            users.push(user)
                        }
                    })
                    response(users)
                }, error: function (error) {
                    createAlert(error.responseText, AlertTypes.Failure, ".modalBody")
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


// --------------------------------------------------- Event listeners -------------------------------------------------

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
 * Listens out for a keydown event on the skills input.
 * If it is a delete button keydown then it removes the last word from the input box.
 * If it is a space, tab or enter then it checks for duplicates
 */
$(document).on("keydown", "#skillsInput", (event) => {
    setTimeout(() => handleSkillInputKeypress(event), 0)
})


/**
 * Runs the remove duplicates function after a paste event has occurred on the skills input
 */
$(document).on("paste", "#skillsInput", () => {
    setTimeout(() => handleSkillInputPaste(), 0)
    // Above is in a timeout so that it runs after the paste event has happened
})


/**
 * The below listener trigger the rendering of the skill chips
 */
$(document).on("click", ".ui-autocomplete", () => {
    removeDuplicatesFromInput($("#skillsInput"))
})


/**
 * Listens for a click on the chip delete buttons, removes all the elements from the skill input that match the
 * skill we are deleting.
 */
$(document).on("click", ".chipDelete", handleChipDelete)