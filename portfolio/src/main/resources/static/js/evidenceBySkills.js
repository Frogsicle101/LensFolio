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
    resetAddOrEditEvidenceForm()
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

function handleEvidenceEdit() {
    let evidenceHighlight = $("#evidenceDetailsContainer")
    resetAddOrEditEvidenceForm()
    resetEvidenceButtonsToEditing()
    retrieveEvidenceData()
    retrieveSkills( evidenceHighlight)
}


/**
 *  A Listener for the edit evidence button. This displays the modal and prevents the page below from scrolling
 */
$(document).on("click", "#editEvidenceButton" , handleEvidenceEdit)








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
    let deleteWebLinkButtons = document.getElementById("addOrEditEvidenceModal").querySelectorAll(".deleteWeblinkButton")
    for (let i = 0; i < deleteWebLinkButtons.length; i++) {
        deleteWebLinkButtons[i].style = "display;"
    }

    //users linked
    let uselinkedList = document.getElementById("evidenceDetailsLinkedUsers")
    document.getElementById("linkedUsersTitle").style = "display;"
    document.getElementById("linkedUsers").innerHTML = uselinkedList.innerHTML
    document.getElementById("addOrEditEvidenceModal").querySelector("#linkedUserId"+userIdent).parentElement.outerHTML = "";

    let deleteUserLinkedButtons = document.getElementById("addOrEditEvidenceModal").querySelectorAll('#deleteLinkedUser')
    for (let i = 0; i < deleteUserLinkedButtons.length; i++) {
        deleteUserLinkedButtons[i].style = "display;"
    }

    $("#addOrEditEvidenceModal").show()
    $(".modalContent").show("drop", {direction: "up"}, 200)
    $('body,html').css('overflow','hidden');



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
function resetAddOrEditEvidenceForm() {
    $("#evidenceName").val("")
    $("#evidenceDate").val(getTodayDate())
    $("#evidenceDescription").val("");
    $("#tagInputChips").empty();     // clean up skills
    $("#addedWebLinks").empty();

    $(".evidenceFormCategoryButton").each((i, button) => {
        button.className = "btn inlineText evidenceFormCategoryButton btn-secondary"
        button.find(".evidenceCategoryTickIcon").hide()

    })

    $("#linkedUsers").empty()
    $("#evidenceSaveButton").prop("disabled", true)
}

/**
 * reset the evidence buttons to editing evidence.
 */
function resetEvidenceButtonsToEditing(){
    $("#addOrEditEvidenceTitle").html( "Edit Evidence");
    $("#evidenceSaveButton").html("Save Changes");
    $("#evidenceSaveButton").prop("disabled", false);
}

/**
 * retrieve evidence name, date, and description.
 */
function retrieveEvidenceData() {
    const currentEvidenceTitle =  sanitise($("#evidenceDetailsTitle").html())
    const currentEvidenceDate =  $("#evidenceDetailsDate").html()
    const currentEvidenceDescription =  sanitise($("#evidenceDetailsDescription").html())
    $("#evidenceName").val(currentEvidenceTitle)
    $("#evidenceDate").val(currentEvidenceDate);
    $("#evidenceDescription").val(currentEvidenceDescription);
}

function retrieveSkills( evidenceHighlight) {
    const currentSkillsList = evidenceHighlight.find(".skillChip")
    currentSkillsList.each((i,skill)=>{
        const skillName = sanitise(skill.find(".chipText").html())
        const skillChip = createDeletableSkillChip(skillName)
        $("#tagInputChips").append(skillChip);

    })
}


/**
 *  Revers translation string to html.
 */
function reversTranslationHTML(strHTML) {
    let doc = new DOMParser().parseFromString(strHTML, 'text/html')
    return doc.documentElement.textContent
}