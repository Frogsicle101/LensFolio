let selectedChip;

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
        skillsContainer.append(createSkillChip(skill.replaceAll("_", " ")))
        // skillsContainer.append(`
        //     <div class="skillListItem evidenceFilter ${skill === selectedChip ? 'selectedSkill' : ''}"
        //     id="SkillCalled${skill.replaceAll(" ", "_")}"> <!-- This ID has underscores instead of spaces  -->
        //     <p class="skillName">${skill.replaceAll("_", " ")}</p>
        //     </div>
        // `)
    }
}


/**
 * Adds the categories to the side bar of the evidence page to allow for easy navigation
 */
function addCategoriesToSidebar() {
    let categoriesList = $('#categoryList')
    for (let category of categoryArray) {
        categoriesList.append(createCategoryChip(category))
        // categoriesList.append(`
        //     <div class="categoryChip ${category === selectedChip ? 'selectedSkill' : ''}">
        //     <p class="skillName chipText">${category}</p>
        //     </div>
        // `)
    }
}


/**
 * Populates the evidence table with all pieces of evidence with that
 * specific skill.
 */
function showEvidenceWithSkill() {
    // Get all the pieces of evidence related to that skill
    $.ajax({
        url: "evidenceLinkedToSkill?skillName=" + selectedChip + "&userId=" + userBeingViewedId,
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
        url: "evidenceLinkedToCategory?category=" + selectedChip + "&userId=" + userBeingViewedId,
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
 * Updated which piece of evidence is currently selected
 */
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
    selectedChip = $(this).find('.skillName').text()

    let title = $(document).find(".evidenceTitle").first()
    title.text(selectedChip)

    showEvidenceWithSkill()
})


/**
 * When a category div in the sidebar is clicked, it becomes selected and is displays all evidence with that category.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from the previously selected div.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".categoryChip" , function (e) {
    let previouslySelectedDiv = $(this).parent().find(".selectedSkill").first()
    previouslySelectedDiv.removeClass("selectedSkill")

    selectedChip = $(this).find('.chipText').text()

    let title = $(document).find(".evidenceTitle").first()
    title.text(selectedChip)

    showEvidenceWithCategory()

    e.stopPropagation() //prevent evidence below chip from being selected
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
$(document).on("click", ".skillChip" , function (e) {
    let previouslySelectedDiv = $(document).find(".selectedSkill").first()
    previouslySelectedDiv.removeClass("selectedSkill")

    selectedChip = $(this).find('.chipText').text()
    let skillId = "#SkillCalled" + selectedChip.replaceAll(" ", "_") // The ID has underscores instead of spaces
    $(document).find(skillId).addClass("selectedSkill")

    let title = $(document).find(".evidenceTitle").first()
    title.text(selectedChip)

    showEvidenceWithSkill()

    e.stopPropagation() //prevent evidence below chip from being selected
})


$(document).on("click", "#showAllEvidence", () => getAndAddEvidencePreviews())