let selectedUserIds = [] // IDs of selected group members
let group;


/**
 * Checks if a user has a role above student.
 *
 * @returns {boolean} returns true if userRole is above student.
 */
function checkPrivilege() {
    return userRoles.includes('COURSE_ADMINISTRATOR') || userRoles.includes('TEACHER');
}


/**
 * On page load, removes user selection header if th logged-in user does not have editing permissions.
 */
$(document).ready(() => {
    if (!checkPrivilege()) {
        document.getElementById("selectAllCheckboxHeader").remove();
    }
})


/**
 * When group div is clicked, the members for that group are retrieved and any existing group member selections are
 * removed.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup");
    let groupId = $(this).closest(".group").find(".groupId").text();
    $("#selectAllCheckboxGroups").prop("checked", false);

    displayGroupUsersList(groupId);
    $(this).closest(".group").addClass("focusOnGroup")

    if (parseInt(groupId) === 0) { // teacher group
        $("#groupRemoveUser").show();
        $(".controlButtons").hide();
    } else if (parseInt(groupId) === 1) { // non-group group
        $("#groupRemoveUser").hide();
        $(".controlButtons").hide();
    } else {
        $("#groupRemoveUser").show();
        $(".controlButtons").show();
    }

    if (!checkPrivilege()) { //if not a teacher or admin role
        $("#selectAllCheckboxHeader").hide();
        $("#groupDisplayOptions").hide();
    }

    selectedUserIds = [];
    $("#confirmationForm").slideUp();
    $(this).closest(".group").addClass("focusOnGroup");
    updateNumberSelectedDisplay()
})


/**
 * On the checkbox selection of a user, the user's ID is added to the list of selected users.
 * When there are no users selected, the "select all" checkbox is unchecked. When all users in the group are selected,
 * the "select all" checkbox is selected by default.
 * Then, calls a method to update the displayed number of selected users.
 */
$(document).on("click", ".selectUserCheckboxGroups", function () {
    let thisRow = $(this).parent().parent();
    let userId = parseInt(thisRow.find(".userId")[0].innerHTML);
    let isSelected = thisRow[0].querySelector("#selectUserCheckboxGroups").checked;

    if (isSelected && selectedUserIds.indexOf(userId) === -1) {
        selectedUserIds.push(userId);
        let numRows = thisRow.parent()[0].rows.length
        if (selectedUserIds.length === numRows) {
            $("#selectAllCheckboxGroups").prop("checked", true);
        }

    } else { // removes the user id from the list of selected users
        let indexOfId = selectedUserIds.indexOf(userId);
        if (indexOfId > -1) {
            selectedUserIds.splice(indexOfId, 1);
        }
        $("#selectAllCheckboxGroups").prop("checked", false);
    }

    updateNumberSelectedDisplay();
})


/**
 * When the remove button is clicked, a popup prompts confirmation of the action.
 */
$(document).on("click", "#groupRemoveUser", function () {
    document.getElementById("confirmationForm").style.visibility = "visible"
    $("#confirmationForm").slideDown();
})


/**
 * Toggles the member selection for the current group. Makes either all members selected, or all unselected, then calls
 * a method to update the displayed number of selected users.
 */
$(document).on("click", "#selectAllCheckboxGroups", function () {
    let table = $("#groupTableBody tr")
    if ($("#selectAllCheckboxGroups").prop("checked")) { // if all are selected
        table.each((i) => {
            let userId = parseInt(table[i].getElementsByTagName("td")[0].innerHTML);

            if (selectedUserIds.indexOf(userId) === -1) {
                selectedUserIds.push(userId)
                table[i].getElementsByTagName("th")[0].firstChild.checked = true // sets checkbox to checked
            }
        })

    } else { // if all are unselected
        selectedUserIds = []
        table.each((i) => {
            table[i].getElementsByTagName("th")[0].firstChild.checked = false // sets checkbox to unchecked
        })
    }

    updateNumberSelectedDisplay();
})


/**
 * Fires off when a click is detected on the delete button for the group. Sends an endpoint request to delete the
 * currently selected group.
 */
$(document).on("click", ".deleteButton", function () {
    if (window.confirm(`Are you sure you want to delete this group? ${group.userList.length} members will be removed. This action cannot be undone.`)) {
        $.ajax({
            url: `/groups/edit?groupId=${group.id}`,
            type: "delete",
            success: function () {
                window.location.reload()
            }, error: function () {
                $("#groupInformationContainer").append(
                    `<div class="alert alert-danger alert-dismissible fade show" role="alert">
                     ${error.responseText}
                     <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>`
                )
            }
        })
    }
})


/**
 * Updates the display showing the number of users currently selected in the group.
 *
 * @param value The number of users currently selected.
 */
function updateNumberSelectedDisplay() {
    $(".numSelected").text(selectedUserIds.length + " Selected")
}


/**
 * When member removal is confirmed, a request is made to remove the selected users from the group.
 */
$(document).on("click", "#confirmRemoval", function () {
    let group = document.getElementsByClassName("focusOnGroup").item(0);
    let groupId = group.getElementsByClassName("groupId").item(0).innerHTML;

    $.ajax({
        url: `groups/removeUsers?groupId=${groupId}&userIds=${selectedUserIds}`,
        type: "DELETE",
        success: () => {
            displayGroupUsersList(groupId)
        },
        error: (error) => {
            console.log(error);
        }
    })
    $("#confirmationForm").slideUp();
})


/**
 * When removal is cancelled, the confirmation popup form is hidden.
 */
$(document).on("click", "#cancelRemoval", function () {
    $("#confirmationForm").slideUp();
})


/**
 * Appends each member's ID, name, username, and profile image to the members container.
 *
 * @param response The list of users to be appended to the members container.
 * @param membersContainer The container which displays the members of each group.
 */
function appendMembersToList(response, membersContainer) {
    for (let member in response.userList) {
        let imageSource;
        if (response.userList[member].imagePath.length === 0) {
            imageSource = "defaultProfile.png"
        } else {
            imageSource = response.userList[member].imagePath;
        }
        let userRow;

        if (checkPrivilege()) {
            userRow = `<tr class="tableRowGroups">
                    <th scope="row"><input id="selectUserCheckboxGroups" class="selectUserCheckboxGroups" type="checkbox"/></th>
                    <td class="userId">${response.userList[member].id}</td>
                    <td>
                        <img src=${imageSource} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                    </td>
                    <td>${response.userList[member].firstName}</td>
                    <td>${response.userList[member].lastName}</td>
                    <td>${response.userList[member].username}</td>
                </tr>`
        } else {
            userRow = `<tr class="tableRowGroups">
                    <td class="userId">${response.userList[member].id}</td>
                    <td>
                        <img src=${imageSource} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                    </td>
                    <td>${response.userList[member].firstName}</td>
                    <td>${response.userList[member].lastName}</td>
                    <td>${response.userList[member].username}</td>
                </tr>`
        }
        membersContainer.append(userRow)
    }
}


/**
 * Retrieves and displays info for each member of the group with the given ID, by appending a row for each user to the
 * table of group members.
 *
 * @param groupId The ID of the group for which users are being displayed.
 */
function displayGroupUsersList(groupId) {
    let membersContainer = $("#groupTableBody")

    $.ajax({
        url: `group?groupId=${groupId}`,
        type: "GET",
        success: (response) => {
            $("#groupTableBody").empty();
            $("#groupInformationShortName").text(response.shortName);
            $("#groupInformationLongName").text(response.longName);
            group = response
            appendMembersToList(response, membersContainer)
        },
        error: (error) => {
            $("#groupInformationContainer").append(
                `<div class="alert alert-danger alert-dismissible fade show" role="alert">
                 ${error.responseText}
                 <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                 </div>`
            )
        }
    })
    $("#groupInformationContainer").slideDown()
}