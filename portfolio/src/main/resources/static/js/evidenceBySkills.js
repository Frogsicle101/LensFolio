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
    getUserSkills()
    addSkillsToSideBar()
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