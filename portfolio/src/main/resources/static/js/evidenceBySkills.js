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

    if (userBeingViewedId !== userIdent) {
        $(".createEvidenceButton").hide();
    }
    getAndAddEvidencePreviews()
    addCategoriesToSidebar()
    getSkills(addSkillsToSideBar)
})


/**
 * Adds all the skills in the skills array to the sidebar
 * Note that the ID of each div is SkillCalled{skill_name} and includes underscores
 */
function addSkillsToSideBar() {
    let skillsContainer = $('#skillList')
    skillsContainer.empty()
    if (! skillsArray.includes("No Skill")) {
        skillsArray.unshift("No Skill")
    }
    for (let skill of skillsArray) {
        skillsContainer.append(`
            <div class="skillListItem evidenceFilter ${skill === selectedSkill ? 'selectedSkill' : ''}"
            id="SkillCalled${skill.replaceAll(" ", "_")}"> <!-- This ID has underscores instead of spaces  -->
            <p class="skillName">${skill.replaceAll("_", " ")}</p> 
            </div>
        `)
    }
}


function addCategoriesToSidebar() {
    let categoriesList = $('#categoryList')
    for (let category of categoryArray) {
        categoriesList.append(`
            <div class="categoryListItem evidenceFilter ${category === selectedSkill ? 'selectedSkill' : ''}">
            <p class="skillName skillChipText">${category}</p> 
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
    $.ajax({
        url: "evidenceLinkedToSkill?skillName=" + selectedSkill + "&userId=" + userBeingViewedId,
        success: function (response) {
            addEvidencePreviews(response)
            updateSelectedEvidence()
            showHighlightedEvidenceDetails()
        }, error: function (error) {
            createAlert(error.responseText, true)
        }
    })
}


/**
 * Populates the evidence table with all pieces of evidence with that
 * specific skill.
 */
function showEvidenceWithCategory() {
    // Get all the pieces of evidence related to that skill
    $.ajax({
        url: "evidenceLinkedToCategory?category=" + selectedSkill + "&userId=" + userBeingViewedId,
        success: function (response) {
            addEvidencePreviews(response)
            updateSelectedEvidence()
            showHighlightedEvidenceDetails()
        }, error: function (error) {
            createAlert(error.responseText, true)
        }
    })
}


function updateSelectedEvidence() {
    let previouslySelectedDiv = $(".selectedEvidence")
    previouslySelectedDiv.removeClass("selectedEvidence")

    let evidenceElements = $("#evidenceList").children()
    evidenceElements.first().addClass("selectedEvidence")
    selectedEvidenceId = evidenceElements.first().find(".evidenceId").text()
}



/* ------------ Event Listeners ----------------- */


/**
 * When a skill div in the sidebar is clicked, it becomes selected and is displays all evidence with that skill.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".skillListItem" , function () {
    let previouslySelectedDiv = $(this).parent().find(".selectedSkill").first()
    previouslySelectedDiv.removeClass("selectedSkill")

    $(this).addClass("selectedSkill")
    selectedSkill = $(this).find('.skillName').text()

    let title = $(document).find(".evidenceTitle").first()
    title.text(selectedSkill)

    showEvidenceWithSkill()
})


/**
 * When a skill div in the sidebar is clicked, it becomes selected and is displays all evidence with that skill.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".categoryListItem" , function () {
    let previouslySelectedDiv = $(this).parent().find(".selectedSkill").first()
    previouslySelectedDiv.removeClass("selectedSkill")

    selectedSkill = $(this).find('.skillName').text()

    let title = $(document).find(".evidenceTitle").first()
    title.text(selectedSkill)

    showEvidenceWithCategory()
})


/**
 * When a skill div in the sidebar is clicked, it becomes selected and is displays all evidence with that skill.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".categoryChip" , function () {
    let previouslySelectedDiv = $(this).parent().find(".selectedSkill").first()
    previouslySelectedDiv.removeClass("selectedSkill")

    selectedSkill = $(this).find('.skillChipText').text()

    let title = $(document).find(".evidenceTitle").first()
    title.text(selectedSkill)

    showEvidenceWithCategory()
})



/**
 * When a skill div inside a piece of evidence is clicked, it selects the skill in the
 * sidebar and is displays all evidence with that skill.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".skillChip" , function () {
    let previouslySelectedDiv = $(document).find(".selectedSkill").first()
    previouslySelectedDiv.removeClass("selectedSkill")

    selectedSkill = $(this).find('.skillChipText').text()
    let skillId = "#SkillCalled" + selectedSkill.replaceAll(" ", "_") // The ID has underscores instead of spaces
    console.log(skillId)
    $(document).find(skillId).addClass("selectedSkill")

    let title = $(document).find(".evidenceTitle").first()
    title.text(selectedSkill)
    showEvidenceWithSkill()
})


$(document).on("click", "#showAllEvidence", () => getAndAddEvidencePreviews())