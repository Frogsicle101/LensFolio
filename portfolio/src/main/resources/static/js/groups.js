let selectedUserIds = [] // Group members who have been selected
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
    if (parseInt(groupId) === 0) {
        $("#groupRemoveUser").show();
        $(".controlButtons").hide();
    } else if (parseInt(groupId) === 1) {
        $("#groupRemoveUser").hide();
        $(".controlButtons").hide();
    } else {
        $("#groupRemoveUser").show();
        $(".controlButtons").show();
    }

    if (!checkPrivilege()) {
        $("#selectAllCheckboxHeader").hide();
        $("#groupDisplayOptions").hide();
    }

    selectedUserIds = [];
    $("#confirmationForm").slideUp();

    $(this).closest(".group").addClass("focusOnGroup");
})


/**
 * When at least one user is selected, the removal button appears and the selected user is added to the list of selected
 * users.
 * When there are no users selected, the "Remove" button is hidden.
 */
$(document).on("click", ".selectUserCheckboxGroups", function () {
    // if checkbox is now selected (i.e., selected == true)
    //     add the userid to the selectedUsersList
    //     if the number of checked boxes is the same as the number of rows (i.e., all rows selected)
    //         check the select All button
    // else
    //     remove the userid from the selectedUsersList
    //     remove the selected class/prop from the selected box (if not done by default)
    //     remove the selected class/prop from the selectAll button
    // update the number selected using the selected users list
    let row = $(this).parent().parent();
    let userId = row.find(".userId")[0].innerHTML;
    let isSelected = row[0].querySelector("#selectUserCheckboxGroups").checked;

    if (isSelected) { // adds the selected user is to the list of selected users
        selectedUserIds.push(parseInt(userId));
        $(this).closest("tr").addClass("selected")
    } else { // removes the user id from the list of selected users
        $(this).closest("tr").removeClass("selected")
        $("#selectAllCheckboxGroups").prop("checked", false);
        let indexOfId = selectedUserIds.indexOf(parseInt(userId));
        if (indexOfId > -1) {
            selectedUserIds.splice(indexOfId, 1);
        }
    }
    console.log("individualClick")
    console.log(selectedUserIds)
    updateNumberSelectedDisplay(selectedUserIds.length)
})


/**
 * When the remove button is clicked, a popup prompts confirmation of the action.
 */
$(document).on("click", "#groupRemoveUser", function () {
    document.getElementById("confirmationForm").style.visibility = "visible"
    $("#confirmationForm").slideDown();
})


/**
 * Toggles the member selection for the current group. makes either all members selected, or all unselected.
 */
$(document).on("click", "#selectAllCheckboxGroups", function () {
    // if the box is now selected
    //     foreach table row thats not the header
    //         add the userId to the selectedUsersList
    //         add the checked prop/class to all rows
    //     add the selected class/prop from the select all box (if not done by default)
    // else
    //     empty the selected users list (i.e., set it to an empty list)
    //     foreach table row thats not the header
    //         remove the checked prop/class from all rows
    //     remove the selected class/prop from the select all box (if not done by default)
    // update the selected rows count using the selectedUserList length
    $(".selectUserCheckboxGroups").prop("checked", $("#selectAllCheckboxGroups").prop("checked"))
    selectedUserIds = []
    if ($("#selectAllCheckboxGroups").prop("checked")) {
        $(".userId").each((id) => {
            selectedUserIds.push($(".userId")[id].innerHTML)
            $(this).closest("tr").addClass("selected")
        })
    } else {
        $(".userId").each((id) => {
            $(this).closest("tr").remove("selected")
        })
    }
    console.log("called")
    console.log(selectedUserIds)
    updateNumberSelectedDisplay(selectedUserIds.length);
})


// /**
//  * When a checkbox is toggled, the row is given the "selected" status, and the number of selected members is updated.
//  */
// $(document).on("change","input[type=checkbox]", function() {
//     let tableRow = $(this).closest("tr")
//     if (!tableRow.hasClass("tableHeader")) {
//         $(this).closest("tr").toggleClass("selected")
//     }
//     console.log("called but other")
//     console.log(selectedUserIds)
//
//
// })


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
            }, error: function (err) {
                $("#groupInformationContainer").append(`<div class="alert alert-danger alert-dismissible fade show" role="alert">
                                                       ${error.responseText}
                                                       <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                                     </div>`)

            }
        })
    }
})


/**
 * Updates the display showing the number of users currently selected in the group.
 *
 * @param value The number of users currently selected.
 */
function updateNumberSelectedDisplay(value) {
    console.log(value + " Selected")
    $(".numSelected").text(value + " Selected")
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
            $("#groupInformationContainer").append(`<div class="alert alert-danger alert-dismissible fade show" role="alert">
                                                       ${error.responseText}
                                                       <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                                     </div>`)
        }
    })
    $("#groupInformationContainer").slideDown()
}