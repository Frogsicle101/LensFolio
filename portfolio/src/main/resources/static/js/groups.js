let selectedUserIds = [] // Group members who have been selected


/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup")
    let groupId = $(this).closest(".group").find(".groupId").text();
    displayGroupUsersList(groupId);

    $(this).closest(".group").addClass("focusOnGroup")
})


/**
 * When a user is selected, the removal button appears and the selected user is added to the list of selected users.
 * When there are no users selected, the "Remove" button is hidden.
 */
$(document).on("click", ".selectUserCheckboxGroups", function () {
    let row = $(this).parent().parent()
    let userId = row.find('.userId')[0].innerHTML
    let isSelected = row[0].querySelector("#selectUserCheckboxGroups").checked

    if (isSelected) { // adds the selected user is to the list of selected users
        selectedUserIds.push(parseInt(userId))
    } else { // removes the user id from the list of selected users
        let indexOfId = selectedUserIds.indexOf(parseInt(userId))
        if (indexOfId > -1) {
            selectedUserIds.splice(indexOfId, 1)
        }
    }

    let group = document.getElementsByClassName("focusOnGroup").item(0)
    let groupId = group.getElementsByClassName("groupId").item(0).innerHTML

    console.log(groupId)
    if (selectedUserIds.length > 0 && groupId !== '0' && groupId !== '1') { //toggles "Remove" button visibility based on whether any users are selected
        document.getElementById(`groupRemoveUser`).style.visibility = "visible"
    } else {
        document.getElementById(`groupRemoveUser`).style.visibility = "hidden"
    }

    console.log(selectedUserIds)
})


/**
 * When remove button is clicked, a request is made to remove the selected users from the group.
 */
$(document).on("click", "#groupRemoveUser", function () {
    let group = document.getElementsByClassName("focusOnGroup").item(0)
    let groupId = group.getElementsByClassName("groupId").item(0).innerHTML

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
})


function displayGroupUsersList(groupId) {
    let membersContainer = $("#groupTableBody")
    $.ajax({
        url: `group?groupId=${groupId}`,
        type: "GET",
        success: (response) => {
            $("#groupTableBody").empty();
            $("#groupInformationShortName").text(response.shortName);
            $("#groupInformationLongName").text(response.longName);
            for (let member in response.userList) {
                let imageSource;
                if (response.userList[member].imagePath.length === 0) {
                    imageSource = "defaultProfile.png"
                } else {
                    imageSource = response.userList[member].imagePath
                }
                membersContainer.append(
                 `<tr>
                     <th scope="row"><input id="selectUserCheckboxGroups" class="selectUserCheckboxGroups" type="checkbox"/></th>
                    <td class="userId">${response.userList[member].id}</td>
                    <td>
                        <img src=${imageSource} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                    </td>
                    <td>${response.userList[member].firstName}</td>
                    <td>${response.userList[member].lastName}</td>
                    <td>${response.userList[member].username}</td>
                    
                </tr>`
                )}
            $("#groupInformationContainer").slideDown()
        },
        error: (error) => {
            console.log(error);
        }
    })
}



