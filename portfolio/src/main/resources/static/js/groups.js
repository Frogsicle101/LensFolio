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
        document.getElementById("selectAllCheckboxHeader").remove()
    }
})


/**
 * When group div is clicked, the members for that group are retrieved and any existing group member selections are
 * removed.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup");
    let groupId = $(this).closest(".group").find(".groupId").text();
    displayGroupUsersList(groupId);

    $(this).closest(".group").addClass("focusOnGroup")
    if (parseInt(groupId) === 0 || parseInt(groupId) === 1) {
        $(".controlButtons").hide()
    } else {
        $(".controlButtons").show()
    }
    selectedUserIds = [];
    document.getElementById("groupRemoveUser").style.visibility = "hidden";
    $("#confirmationForm").slideUp();

    $(this).closest(".group").addClass("focusOnGroup");
})


/**
 * When at least one user is selected, the removal button appears and the selected user is added to the list of selected
 * users.
 * When there are no users selected, the "Remove" button is hidden.
 */
$(document).on("click", ".selectUserCheckboxGroups", function () {
    let row = $(this).parent().parent();
    let userId = row.find(".userId")[0].innerHTML;
    let isSelected = row[0].querySelector("#selectUserCheckboxGroups").checked;

    if (isSelected) { // adds the selected user is to the list of selected users
        selectedUserIds.push(parseInt(userId));
    } else { // removes the user id from the list of selected users
        let indexOfId = selectedUserIds.indexOf(parseInt(userId));
        if (indexOfId > -1) {
            selectedUserIds.splice(indexOfId, 1);
        }
    }

    let group = document.getElementsByClassName("focusOnGroup").item(0);
    let groupId = group.getElementsByClassName("groupId").item(0).innerHTML;

    if (selectedUserIds.length > 0 && groupId !== "0" && groupId !== "1") { //toggles "Remove" button visibility based on whether any users are selected
        document.getElementById("groupRemoveUser").style.visibility = "visible";
    } else {
        document.getElementById("groupRemoveUser").style.visibility = "hidden";
    }
})


/**
 * When the remove button is clicked, a popup prompts confirmation of the action.
 */
$(document).on("click", "#groupRemoveUser", function () {
    document.getElementById("confirmationForm").style.visibility = "visible"
    $("#confirmationForm").slideDown();
})

$(document).on("click", "#selectAllCheckboxGroups", function () {
    $(".selectUserCheckboxGroups").prop("checked", $("#selectAllCheckboxGroups").prop("checked"))
    updateNumberSelectedDisplay($("input[type=checkbox]").length)

})

$(document).on("change","input[type=checkbox]", function() {
    let tableRow = $(this).closest("tr")
    if (!tableRow.hasClass("tableHeader")) {
        $(this).closest("tr").toggleClass("selected")
    }
    updateNumberSelectedDisplay($(".selected").length)

})

/**
 * Fires off when a click is detected on the delete button for the group.
 */
$(document).on("click", ".deleteButton", function () {

    if (window.confirm(`Are you sure you want to delete this group? ${group.userList.length} members will be removed. This action cannot be undone.`)) {
        $.ajax({
            url: `/groups/edit?groupId=${group.id}`,
            type: "delete",
            success: function () {
                window.location.reload()
            }, error: function (err) {
                console.log(err)

            }
        })
    }
})


function updateNumberSelectedDisplay(value) {
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
    document.getElementById("groupRemoveUser").style.visibility = "hidden";
})


/**
 * When removal is cancelled, the popup form is hidden.
 */
$(document).on("click", "#cancelRemoval", function () {
    $("#confirmationForm").slideUp();
})


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
        },

        error: (error) => {
            console.log(error);
        }

    })
    $("#groupInformationContainer").slideDown()

}


