let selectedChip;

/**
 * Runs when the page is loaded. This gets the user being viewed and adds dynamic elements.
 */
$(() => {
    let urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has("userId")) {
        userBeingViewedId = parseInt(urlParams.get('userId'))
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

    skillsContainer.append(createSkillChip("No Skill"))
    for (let skill of skillsArray) {
        skillsContainer.append(createSkillChip(skill.replaceAll("_", " ")))
    }
}


/**
 * Adds the categories to the side bar of the evidence page to allow for easy navigation
 */
function addCategoriesToSidebar() {
    let categoriesList = $('#categoryList')
    for (let category of categoriesMapping.values()) {
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
        url: "evidenceLinkedToSkill?userId=" + userBeingViewedId,
        type: "GET",
        data: {
            "skillName": selectedChip
        },
        success: function (response) {
            addEvidencePreviews(response)
            updateSelectedEvidence()
            showHighlightedEvidenceDetails()
        }, error: function (error) {
            createAlert(error.responseText, AlertTypes.Failure)
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
            createAlert(error.responseText, AlertTypes.Failure)
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


/**
 *  A Listener for the create evidence button. This displays the modal and prevents the page below from scrolling
 */
$(document).on("click", "#createEvidenceButton" , () => {

    // Reset addOrEditEvidenceModal without pre-filling evidence details
    resetAddOrEditEvidencePage()
    document.getElementById("addOrEditEvidenceTitle").innerHTML = "Add Evidence";
    document.getElementById("evidenceSaveButton").innerHTML = "Create";

    $("#addOrEditEvidenceModal").show()
    $(".modalContent").show("drop", {direction: "up"}, 200)
    $('body,html').css('overflow','hidden');
})


/**
 *  A listener for the cancel create evidence button. Calls the function to close the modal
 */
$(document).on("click", "#evidenceCancelButton", function () {
    closeModal()
})


/**
 *  When the mouse is clicked, if the modal is open, the click is outside the modal, and the click is not on an alert,
 *  calls the function to close the modal.
 */
window.onmousedown = function(event) {
    let modalDisplay = $("#addOrEditEvidenceModal").css("display")
    if (modalDisplay === "block" && !event.target.closest(".modalContent") && !event.target.closest(".alert")) {
        closeModal()
    }
}


/**
 *  Closes the modal and allows the page below to scroll again
 */
function closeModal() {
    $(".modalContent").hide("drop", {direction: "up"}, 200, () => {$("#addOrEditEvidenceModal").hide()})
    $('body,html').css('overflow','auto');
}


// -------------------------------------- Evidence Editing -----------------------------------


/**
 *  A Listener for the edit evidence button. This displays the modal and prevents the page below from scrolling
 */
$(document).on("click", "#editEvidenceButton" , () => {

    //clean up the edit evidence page
    resetAddOrEditEvidencePage()
    document.getElementById("addOrEditEvidenceTitle").innerHTML = "Edit Evidence";
    document.getElementById("evidenceSaveButton").innerHTML = "Save Changes";

    // Reset addOrEditEvidenceModal with pre-filling evidence details
    let evidenceHighlight = document.querySelector(".evidenceDetailsContainer")
    let currentEvidenceId = document.getElementById("evidenceDetailsId").innerHTML

    //name, date, description
    let currentEvidenceTitle =  document.getElementById("evidenceDetailsTitle").innerHTML
    let currentEvidenceDate =  document.getElementById("evidenceDetailsDate").innerHTML
    let currentEvidenceDescription =  document.getElementById("evidenceDetailsDescription").innerHTML
    document.getElementById("evidenceName").value = currentEvidenceTitle;
    document.getElementById("evidenceDate").value = currentEvidenceDate;
    document.getElementById("evidenceDescription").innerHTML = currentEvidenceDescription;

    //skills
    let currentSkillsList = evidenceHighlight.querySelectorAll(".skillChip")
    for (let i = 0; i < currentSkillsList.length; i++) {
        let skillName = currentSkillsList[i].querySelector(".chipText").innerHTML
        let skillChip = `
                <div class="chip skillChip">
                    <p class="chipText">${sanitise(skillName)}</p>  
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-x-circle chipDelete" viewBox="0 0 16 16">
                                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                                <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                    </svg>
                </div>`
        document.getElementById("tagInputChips").innerHTML += skillChip;
    }

    //categories
    let currentCategoriesList = evidenceHighlight.querySelectorAll(".categoryChip")
    for (let i = 0; i < currentCategoriesList.length; i++) {
        let categoryName = currentCategoriesList[i].querySelector(".chipText").innerHTML
        let categoryButton =  document.getElementById("button"+categoryName)
        categoryButton.className = "btn inlineText evidenceFormCategoryButton btn-success"
        categoryButton.querySelector(".evidenceCategoryTickIcon").style = "display: inline-block;"
    }

    //webLinks
    let webLinksList = evidenceHighlight.querySelectorAll(".webLinkElement")
    document.getElementById("webLinkTitle").style = "display;"
    for (let i = 0; i < webLinksList.length; i++) {
        document.getElementById("addedWebLinks").innerHTML += webLinksList[i].outerHTML;
    }
    let deleteButtons = document.getElementById("addOrEditEvidenceModal").querySelectorAll(".deleteWeblinkButton")
    for (let i = 0; i < deleteButtons.length; i++) {
        deleteButtons[i].style = "display;"
    }

    //user link
    let uselinkedList = document.getElementById("evidenceDetailsLinkedUsers")
    document.getElementById("linkedUsersTitle").style = "display;"
    document.getElementById("linkedUsers").innerHTML = uselinkedList.innerHTML

    $("#addOrEditEvidenceModal").show()
    $(".modalContent").show("drop", {direction: "up"}, 200)
    $('body,html').css('overflow','hidden');
})


/**
 *  Get today date as format of yyyy-mm-dd
 */
function getTodayDate() {
    let today = new Date()
    let year = today.getFullYear()
    let month = String(today.getMonth() + 1).padStart(2,'0')
    let day = String(today.getDate()).padStart(2, '0')
    return year + '-' + month + '-' +day
}


/**
 *  Clean up the evidence adding and editing page.
 */
function resetAddOrEditEvidencePage() {

    document.getElementById("evidenceName").value = "";
    document.getElementById("evidenceDate").value = getTodayDate();
    document.getElementById("evidenceDescription").innerHTML = "";
    document.getElementById("tagInputChips").innerHTML ="";     // clean up skills
    document.getElementById("addedWebLinks").innerHTML ="";
    let categories = document.querySelectorAll(".evidenceFormCategoryButton")
    for (let i = 0; i < categories.length; i++) {
        categories[i].className = "btn inlineText evidenceFormCategoryButton btn-secondary"
        categories[i].querySelector(".evidenceCategoryTickIcon").style = "display: none;"
    }
    document.getElementById("linkedUsers").innerHTML = "";
}
