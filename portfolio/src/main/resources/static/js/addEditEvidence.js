
/** For adding the skills to as the chips are added. */
let skillsToCreate = []

function validateSkillInput() {
    const skillsInput = $("#skillsInput")
    const inputValue = skillsInput.val()
    if (inputValue > 30) {
        skillsInput.addClass("skillChipInvalid")
        createAlert("Length of skill name should be less than 30", "failure")
        return false
    }
    if (RESERVED_SKILL_TAGS.includes(inputValue.toLowerCase())) {
        skillsInput.addClass("skillChipInvalid")
        addTooltip(parent, "This is a reserved tag and cannot be manually created")
        return false
    }
    return true
}

function handleSkillInputChange(event) {
    const skillsInput = $("#skillsInput")
    const inputValue = skillsInput.val().trim()
    const isValidSkillName = validateSkillInput()
    let needsUpdate = false
    console.log("Skills input value: " + skillsInput.val())
    if (event.key === "Backspace" && inputValue.length === 0 && skillsToCreate.length > 0) {
        skillsToCreate.pop()
        needsUpdate = true
    }
    if ((event.keyCode === 32 || event.keyCode === 13) && isValidSkillName) { // Spacebar or Enter
        if (! skillsToCreate.includes(inputValue)) {
            skillsToCreate.push(inputValue)
            needsUpdate = true
        }
        skillsInput.val("")
    }
    if (needsUpdate) {
        let chipDisplay = $("#tagInputChips")
        $('[data-toggle="tooltip"]').tooltip("hide")

        chipDisplay.empty()
        skillsToCreate.forEach(function (element) {
            element = element.replaceAll("_", " ");
            chipDisplay.append(createDeletableSkillChip(element))
        })
    }
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


/**
 * This function gets the input string from the skills input and trims off the extra whitespace
 * then it separates each word into an array and creates chips for them.
 */
function displayInputSkillChips() {
    checkToShowSkillChips()
    let skillsInput = $("#skillsInput")
    let inputArray = skillsInput.val().trim().split(/\s+/)
    let chipDisplay = $("#tagInputChips")

    $('[data-toggle="tooltip"]').tooltip("hide")

    chipDisplay.empty()
    inputArray.forEach(function (element) {
        element = element.replaceAll("_", " ");
        chipDisplay.append(createDeletableSkillChip(element))
    })
    chipDisplay.find(".chipText").each(function () {
        const parent = $(this).parent(".skillChip")
        if ($(this).text().length < 1) {
            parent.remove()
        }
        if ($(this).text().length > 30) {
            parent.addClass("skillChipInvalid")
            createAlert("Length of skill name should be less than 30", "failure")
        }
        if (RESERVED_SKILL_TAGS.includes($(this).text().toLowerCase())) {
            const parent = $(this).parent(".skillChip")
            parent.addClass("skillChipInvalid")
            addTooltip(parent, "This is a reserved tag and cannot be manually created")
        }
    })
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


// /**
//  * Listens out for a keyup event on the skills input.
//  * Renders the skill chips when it detects a keyup event.
//  */
// $(document).on("keyup", "#skillsInput", function () {
//     displayInputSkillChips()
// })


/**
 * Cleans up the duplicates in the input when the user clicks away from the input.
 */
$(document).on("click", () => {
    removeDuplicatesFromInput($("#skillsInput"))
    displayInputSkillChips()
})


// /**
//  * Triggers the rendering of the skill chips
//  */
// $(document).on("click", ".ui-autocomplete", () => {
//     removeDuplicatesFromInput($("#skillsInput"))
//     displayInputSkillChips()
// })


// $(document).on("change", "#skillsInput", (event) => {
//     console.log("Change event")
//     console.log(event.key === "Backspace" || event.key === "Delete")
//     displayInputSkillChips()
// })


/**
 * Listens out for a keydown event on the skills input.
 * If it is a delete button keydown then it removes the last word from the input box.
 * If it is a space, tab or enter then it checks for duplicates
 */
$(document).on("keydown", "#skillsInput", function (event) {
    handleSkillInputChange(event)
    let skillsInput = $("#skillsInput")
    if (event.key === "Delete") {
        event.preventDefault();
        let inputArray = skillsInput.val().trim().split(/\s+/)
        inputArray.pop()
        skillsInput.val(inputArray.join(" ") + " ")
    }
    if (event.key === " " || event.key === "Tab" || event.key === "Enter") {
        removeDuplicatesFromInput(skillsInput)
    }
    displayInputSkillChips()
})


/**
 * Runs the remove duplicates function after a paste event has occurred on the skills input
 */
$(document).on("paste", "#skillsInput", (event) => {
    handleSkillInputChange(event)
    console.log("Paste event")
    console.log(event.key === "Backspace" || event.key === "Delete")
    setTimeout(() => removeDuplicatesFromInput($("#skillsInput")), 0)
    // Above is in a timeout so that it runs after the paste event has happened
})


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