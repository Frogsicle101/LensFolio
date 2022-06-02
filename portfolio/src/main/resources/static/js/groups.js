/**
 * On page load, loads a list of all groups into the DOM.
 */
$(document).ready(() => {
    getGroups();
})

/**
 * To retrieve all groups and process the response.
 */
function getGroups() {
    $.ajax({
        url: 'groups',
        type: 'GET',
        success: function (response) {
            let groupsContainer = $(".groupsContainer");
            for (let index in response) {
                groupsContainer.append(appendGroup(response[index])); // to add each group to the DOM.
            }
            groupsContainer.slideDown();
        }
    })
}

/**
 * To add a given group object to the DOM.
 * The group's short name and long name are displayed.
 *
 * @param groupObject The group object to be added to the DOM.
 * @returns {string} The html fragment representing the group's information.
 */
function appendGroup(groupObject) {
    return `
             <div class="group">
                <p class="groupId" style="display: none">${groupObject.id}</p>
                <p class="groupShortName" style="display: none">${groupObject.shortName}</p>
                <p class="groupLongName" style="display: none">${groupObject.longName}</p>
                <div class="mb3">
                    <h2 class="groupShortName">${groupObject.shortName}</h2>
                    <h3 class="groupLongName">${groupObject.longName}</h3>
                </div>
             </div>`;
}

