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
        skillsContainer.append(createSkillChip(skill.replaceAll("_", " "), true))
    }
}


/**
 * Adds the categories to the side bar of the evidence page to allow for easy navigation
 */
function addCategoriesToSidebar() {
    let categoriesList = $('#categoryList')
    for (let category of categoryArray) {
        categoriesList.append(createCategoryChip(category, true))
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
 * When a chip div is clicked, it selects the skill/category in the sidebar and is displays all
 * evidence with that skill/category.
 *
 * There are 3 steps to this:
 *    1. remove the selected class from selected divs.
 *    2. Add the selected class to the clicked div, and assign it as selected
 *    3. Populate the display with the selected evidence details.
 */
$(document).on("click", ".chip" , function (event) {
    $(".selected").removeClass("selected")

    let clicked = $(this)
    selectedChip = clicked.find('.chipText').text()
    let isSkill = clicked.hasClass("skillChip")
    let chipId = isSkill ? ("#skillCalled" + selectedChip.replaceAll(" ", "_")) : ("#categoryCalled" + selectedChip)
    $(chipId).addClass("selected")

    let title = $(".evidenceTitle").first()
    title.text(selectedChip)
    if (isSkill) {
        showEvidenceWithSkill()
    } else {
        showEvidenceWithCategory()
    }
    event.stopPropagation() //prevent evidence below chip from being selected
})


$(document).on("click", "#showAllEvidence", () => getAndAddEvidencePreviews())