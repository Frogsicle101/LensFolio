

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
 * When a user is selected, the removal button appears.
 */
$(document).on("click", ".selectUserCheckboxGroups", function () {
    document.getElementById(`groupRemoveUser`).style.visibility = "visible"
})

/**
 * When remove button is clicked, a request is made to remove the users from the group.
 */
$(document).on("click", "#groupRemoveUser", function () {
    let groupId = $(this).find('.groupId');
    let groupMembers = [];
    $(this).closest(`#groupTableBody > tr`).each(() => { //this doesn't work
        groupMembers.push($(this).attr('id'))
    })
    console.log(groupMembers)
    $.ajax({
        url: `groups/removeUsers`,
        type: "DELETE",
        data: {'groupId': groupId, 'userIds': groupMembers},
        success: (response) => {

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
                     <th scope="row"><input class="selectUserCheckboxGroups" type="checkbox"/></th>
                    <td>${response.userList[member].id}</td>
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



