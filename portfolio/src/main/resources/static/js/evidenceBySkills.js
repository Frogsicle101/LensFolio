let selectedSkill;

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
    addSkillsToSideBar() // Add the first/default batch of skills
    getSkills(addSkillsToSideBar) // Get any extras and add those as well
})


/**
 * Adds all the skills in the skills array to the sidebar
 */
function addSkillsToSideBar() {
    let skillsContainer = $('#skillList')
    skillsContainer.empty()
    for (let skill of skillsArray) {
        skillsContainer.append(`
            <div class="skillListItem ${skill === selectedSkill ? 'selectedSkill' : ''}">
            <p class="skillName">${skill}</p> 
            </div>
        `)
    }
}


/**
 * Populates the evidence table with all pieces of evidence with that
 * specific skill.
 */
function showEvidenceWithSkill() {
    // Get all the pieces of evidence related to that skill
    console.log(selectedSkill)
    $.ajax({
        url: "evidenceLinkedToSkill?skillName=" + selectedSkill, success: function (response) {
            addEvidencePreviews(response)
            showHighlightedEvidenceDetails()
        }, error: function (error) {
            createAlert(error.responseText, true)
        }
    })
}


/* ------------ Event Listeners ----------------- */


/**
 * When a skill div is clicked, it becomes selected and is displays all evidence with that skill.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".skillListItem", function () {
    let previouslySelectedDiv = $(this).parent().find(".selectedSkill").first()
    previouslySelectedDiv.removeClass("selectedSkill")

    $(this).addClass("selectedSkill")
    selectedSkill = $(this).find('.skillName').text()

    showEvidenceWithSkill()
})