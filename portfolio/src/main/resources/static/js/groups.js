let controlDown = false;
let shiftDown = false;
let selectedGroupId;
let lastSelectedRow;
let group;

const TEACHER_GROUP_ID = 1
const MWAG_GROUP_ID = 2

// ******************************* Functions *******************************

/**
 * Displays the options for what to do with selected users.
 * @param show a boolean of if to show or hide
 */
function showOptions(show) {
    if (show && (selectedGroupId !== TEACHER_GROUP_ID || isAdmin())) {
        $("#groupDisplayOptions").slideDown()
    } else {
        $("#groupDisplayOptions").slideUp()
    }
    $(".numSelected").text($(".selected").length + " Selected")
}


/**
 * Makes all the logic changes bootstrap does when changing the tab from settings to users.
 */
function changeToUsersTab() {
    let groupSettingsTab = $("#groupSettingsTab")
    let groupUsersTab = $("#groupUsersTab")
    let groupUsersButton = $("#pillsUsersTab")
    let groupSettingsButton = $("#pillsSettingsTab")
    let groupUsersPage = $("#pillsUsers")
    let GroupSettingsPage = $("#pillsSettings")

    groupUsersTab.attr("aria-selected", true)
    groupSettingsTab.attr("aria-selected", false)

    groupUsersButton.attr("aria-selected", true)
    groupUsersButton.addClass("active")
    groupSettingsButton.attr("aria-selected", false)
    groupSettingsButton.removeClass("active")

    groupUsersTab.addClass('active')
    groupSettingsTab.removeClass('active')
    groupUsersPage.addClass('show')
    groupUsersPage.addClass('active')
    GroupSettingsPage.removeClass('show')
    GroupSettingsPage.removeClass('active')
}


/**
 * Helper function that uses the amount of selected users to determine if to call the showOptions function
 */
function checkToSeeIfHideOrShowOptions() {
    let amountSelected = $(".selected").length
    if (amountSelected > 0) {
        showOptions(true)
    } else {
        showOptions(false)
    }
}


/**
 * Called when a group page is opened. This function sets the visibility of the group settings tab.
 * The visibility is true only if the user is in the group or is a teacher or admin.
 *
 * @param group - The newly selected group.
 */
function checkEditRights(group) {
    let groupSettingsTab = $("#groupSettingsTab")
    let groupId = group.id
    groupSettingsTab.hide()

    if (groupId === TEACHER_GROUP_ID) {
        $("#groupRemoveUser").show();
        $(".controlButtons").hide();
    } else if (groupId === MWAG_GROUP_ID) {
        $("#groupRemoveUser").hide();
        $(".controlButtons").hide();
    } else {
        $("#groupRemoveUser").show();
        $(".controlButtons").show();
    }

    // only show settings page if the active page is not MWAG or Teachers & if the user has read access
    // i.e., the user is an admin, teacher or member of the group.
    if (groupId !== MWAG_GROUP_ID  &&
        groupId !== TEACHER_GROUP_ID &&
        (checkPrivilege() || group.userList.some(member => member.id === userIdent)))
    {
        groupSettingsTab.show()
    } else {
        changeToUsersTab()
    }
}


/**
 * Makes an ajax get call to the server and gets all the information for a particular group.
 * Loops through the groups members and adds them to the table.
 * @param groupId the id of the group to fetch
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
            selectedGroupId = response.id;
            group = response;
            for (let member in response.userList) {
                let imageSource;
                let user = response.userList[member]
                if (user.imagePath.length === 0) {
                    imageSource = "defaultProfile.png"
                } else {
                    imageSource = user.imagePath
                }
                membersContainer.append(`
                    <tr class="userRow" userId=${user.id}>
                        <td>${user.id}</td>
                        <td>
                            <img src=${imageSource} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                        </td>
                        <td>${user.firstName}</td>
                        <td>${user.lastName}</td>
                        <td>${user.username}</td>
                    </tr>`
                )
            }
            $("#groupInformationContainer").slideDown()
            checkToSeeIfHideOrShowOptions()
            checkEditRights(response)
        },
        error: (error) => {
            createAlert(error.responseText, true)
        }
    })
}


/**
 * When the remove button is clicked, a popup prompts confirmation of the action.
 */
$(document).on("click", "#groupRemoveUser", function () {
    document.getElementById("confirmationForm").style.visibility = "visible"
    $("#confirmationForm").slideDown();
})


/**
 * Fires off when a click is detected on the delete button for the group. Sends an endpoint request to delete the
 * currently selected group.
 */
$(document).on("click", ".deleteButton", function () {
    if (window.confirm(`Are you sure you want to delete this group? ${group.userList.length} members will be removed. This action cannot be undone.`)) {
        $.ajax({
            url: `groups/edit?groupId=${group.id}`,
            type: "delete",
            success: function () {
                window.location.reload()
            }, error: function (error) {
                createAlert(error.responseText, true)
            }
        })
    }
})



/**
 * When member removal is confirmed, a request is made to remove the selected users from the group.
 */
$(document).on("click", "#confirmRemoval", function () {
    let arrayOfIds = [];
    $(".selected").each(function() {
        arrayOfIds.push($(this).attr("userId"))
    })
    $.ajax({
        url: `groups/removeUsers?groupId=${selectedGroupId}&userIds=${arrayOfIds}`,
        type: "DELETE",
        success: () => {
            displayGroupUsersList(selectedGroupId)
            createAlert("User removed", false)
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
 * Makes an ajax get call to the server and gets all the information for a particular group.
 * Loops through the groups members and adds them to the table.
 * @param groupId the id of the group to fetch
 */
$(document).on("click", "#pillsSettingsTab", function () {
    let membersContainer = $("#gitRepo")
    $.ajax({
        url: `getRepo?groupId=${selectedGroupId}`,
        type: "GET",
        success: (response) => {
            if (response.length === 0){
                membersContainer.empty();
                membersContainer.append(`
                    <h3 id="groupSettingsPageRepoName">No Repository</h3>`
                )
            }
            else {
                let repo = response[0];
                membersContainer.empty();
                membersContainer.append(`
                        <h3 id="the-group-settings-page-repo-name">${repo.alias}</h3>
                             <div class="row margin-sides-1">
                                <div class="inline-text col">
                                    <p>Project Id:&nbsp;</p>
                                    <p class="group-settings-page-projectId grey-text" >${repo.projectId}</p>
                                </div>
                                <div class="inline-text col">
                                    <p>Access Token:&nbsp;</p>
                                    <p class="group-settings-page-access_token grey-text" >${repo.accessToken}</p>
                                </div>
                             </div>`
                )
            }
        },
        error: (error) => {

        },
    })
})


// ******************************* Click listeners *******************************


/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup")
    let newFocusOnGroup = $(this).closest(".group")

    newFocusOnGroup.addClass("focusOnGroup")
    let groupId = newFocusOnGroup.find(".groupId").text();

    $("#selectAllCheckboxGroups").prop("checked", false);
    displayGroupUsersList(groupId)
    $("#confirmationForm").slideUp();
})


/**
 * Ajax post request to the server for moving users from one group to another
 */
$(document).on("click", "#moveUsersButton", function() {
    let arrayOfIds = [];
    $(".selected").each(function() {
        arrayOfIds.push($(this).attr("userId"))
    })
    $.ajax({
        url: `/groups/addUsers?groupId=${$("#newGroupSelector").val()}&userIds=${arrayOfIds}`,
        type: "post",
        success: function(event) {
            displayGroupUsersList(selectedGroupId)
            createAlert(event, false)
        },
        error: function() {
            createAlert("Error moving users", true)
        }
    })
})

/**
 * selects every single user in the group when the button is clicked.
 */
$(document).on("click", "#selectAllCheckboxGroups", function() {
    let isChecked = $("#selectAllCheckboxGroups").prop("checked")
    $(".selectUserCheckboxGroups").prop("checked", isChecked)
    if (isChecked) {
        $(".userRow").addClass("selected")
    } else {
        $(".userRow").removeClass("selected")
    }
    checkToSeeIfHideOrShowOptions()

})

/**
 * handles all the functionality for selecting users.
 * Allows for using shift+click and ctrl+click functionality.
 */
$(document).on("click", ".userRow", function() {

    if (!controlDown && !shiftDown) {
        $(".selected").each(function() {
            $(this).removeClass("selected")
            $(this).find("input[type=checkbox]").prop("checked", false)
        })
    }
    if (shiftDown) {
        let boundaries = []; // Boundaries in this case are the first user, and the last user, used to select everything between.
        boundaries.push(lastSelectedRow)
        boundaries.push($(this).attr("userId"));
        boundaries[0] = parseInt(boundaries[0])
        boundaries[1] = parseInt(boundaries[1])
        boundaries = boundaries.sort()
        $(".userRow").each(function() {
            if ($(this).attr("userId") >= boundaries[0] && $(this).attr("userId") <= boundaries[1]) {
                $(this).addClass("selected")
                $(this).find("input[type=checkbox]").prop("checked", true)
            }
        })
    } else {
        if ($(this).hasClass("selected")) {
            $(this).removeClass("selected")
            $(this).find("input[type=checkbox]").prop("checked", false)
        } else {
            $(this).addClass("selected")
            $(this).find("input[type=checkbox]").prop("checked", true)
        }
    }
    lastSelectedRow = $(this).attr("userId")
    checkToSeeIfHideOrShowOptions()

})

// ******************************* Keydown listeners *******************************

$(document).keydown(function(event) {
    if (event.key === "Control") {
        controlDown = true;
    }
})

$(document).keyup(function(event) {
    if (event.key === "Control") {
        controlDown = false;
    }
})


$(document).keydown(function(event) {
    if (event.key === "Shift") {
        shiftDown = true;
    }
})

$(document).keyup(function(event) {
    if (event.key === "Shift") {
        shiftDown = false;
    }
})

// ******************************* Change listeners *******************************

$(document).on("change","input[type=checkbox]", function() {
    let tableRow = $(this).closest("tr")
    if (!tableRow.hasClass("tableHeader")) {
        $(this).closest("tr").toggleClass("selected")
    }
    checkToSeeIfHideOrShowOptions()

})
