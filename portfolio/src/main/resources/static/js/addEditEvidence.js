
/** For adding the skills to as the chips are added. */
let skillsToCreate = []

function addUniqueSkill(skillName) {
    if (! skillsToCreate.includes(skillName)) {
        skillsToCreate.push(skillName.replaceAll("_", " "))
        return true
    }
    return false
}


function validateSkillInput(inputValue, showAlert) {
    const skillsInput = $("#skillsInput")
    if (inputValue.length > 30) {
        if (showAlert) {
            skillsInput.addClass("skillChipInvalid")
            createAlert("Length of skill name should be less than 30", "failure")
        }
        return false
    }
    if (inputValue.trim().length === 0) {
        return false
    }
    if (RESERVED_SKILL_TAGS.includes(inputValue.toLowerCase())) {
        if (showAlert) {
            skillsInput.addClass("skillChipInvalid")
            createAlert("This is a reserved tag and cannot be manually created", "failure")
        }
        return false
    }
    skillsInput.removeClass("skillChipInvalid")
    return true
}


function updateSkillsInput() {
    let chipDisplay = $("#tagInputChips")
    $('[data-toggle="tooltip"]').tooltip("hide")

    chipDisplay.empty()
    skillsToCreate.forEach(function (element) {
        element = element.replaceAll("_", " ");
        chipDisplay.append(createDeletableSkillChip(element))
    })
}


function handleSkillInputKeypress(event) {
    const skillsInput = $("#skillsInput")
    const inputValue = skillsInput.val().trim()
    const isValidSkillName = validateSkillInput(inputValue, true)
    let needsUpdate = false

    if (event.key === "Backspace" && inputValue.length === 0 && skillsToCreate.length > 0) {
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

    if (needsUpdate) {
       updateSkillsInput()
    }
}


function handleSkillInputPaste() {
    const skillsInput = $("#skillsInput")
    const inputValues = skillsInput.val().trim().split(/\s+/)
    const invalidSkillNames = new Set()

    console.log(inputValues)
    inputValues.forEach(skillName => {
        if (validateSkillInput(skillName, false)) {
            addUniqueSkill(skillName)
        } else {
            invalidSkillNames.add(skillName.length > 30 ? skillName.substring(0, 27) + "..." : skillName)
        }
    })

    updateSkillsInput()
    skillsInput.val("")

    if (invalidSkillNames.length > 0) {
        if (invalidSkillNames.length < 5) {
            createAlert("Invalid skills not added: " + invalidSkillNames.join(", "), "failure")
        } else {
            createAlert("Discarded " + invalidSkillNames.length + " invalid skills", "failure")
        }
    }
}


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
                createAlert("Emojis not allowed in Skill name", "failure")
            }
            if (element.length > 30) { //Shortens down the elements to 30 characters
                element = element.split("").splice(0, 30).join("")
                createAlert("Length of skill name should be less than 30", "failure")
            }
            if (!(newArray.includes(element) || newArray.map((item) => item.toLowerCase()).includes(element.toLowerCase()))) {
                newArray.push(element)
            }
        } else if (element.length > 0) {
            createAlert("Skill names containing only special symbols are not allowed.", "failure")
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
                    createAlert(error.responseText, "failure", ".modalBody")
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
$(document).on("keydown", "#skillsInput", function (event) {
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