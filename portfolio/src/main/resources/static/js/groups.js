let selectedGroupId
let group
const TEACHER_GROUP_ID = 1
const MWAG_GROUP_ID = 2

$(function () {
    if (!checkPrivilege()) {
        return
    }

    manageTableSelection()

    let listOfGroupDivs = $(".group") // gets a list of divs that have the class group
    for (let i = 0; i < listOfGroupDivs.length; i++) { // Loops over each div
        /**
         * Adds the droppable pluggin to each element that it loops over
         * https://api.jqueryui.com/droppable/
         */
        $(listOfGroupDivs[i]).droppable({
            /**
             * Triggered when an accepted draggable is dragged over the droppable (based on the tolerance option).
             * https://api.jqueryui.com/droppable/#event-over
             */
            over: function () {
                $(this).effect("shake")
                //https://api.jqueryui.com/category/effects/
            },

            /**
             * Triggered when an accepted draggable is dropped on the droppable (based on the tolerance option).
             * https://api.jqueryui.com/droppable/#event-drop
             */
            drop: function () {
                addUsers($(this).attr("id"))
                showDraggableIcons()
            },
            tolerance: "pointer"
        })
    }
})

//----------------------- jQuery UI User Selection -------------------------


/**
 * Implements the selectable widget from jQuery UI to enable the selection of users in the group members list.
 */
function manageTableSelection() {
    let anchorRow

    $( "#groupTableBody" ).selectable({
        filter: ":not(td)",

        /**
         * Overrides the selected method for the jQuery UI selectable widget, to enable shift clicking.
         *
         * A non-shift select sets an "anchor" row. Shift clicking on either side of the anchor row selects rows between the
         * anchor row and the selected row (inclusive).
         * Ctrl clicks allow non-adjacent rows to be selected and deselected.
         * Ctrl clicks followed by a shift click will deselect all but the latest ctrl click.
         *
         * @param e An event (e.g. a key press)
         * @param ui The latest selected row
         */
        selected: function (e, ui) {  // overrides library function to enable shift clicking
            let currentRow = $(ui.selected)

            if (e.shiftKey) {
                let currentId = parseInt(currentRow.attr("userId"))
                let lastId

                if (typeof anchorRow == "undefined") {  // if first selection on table, set anchor to this row
                    anchorRow = $(ui.selected)
                    lastId = currentId
                } else {
                    lastId = parseInt(anchorRow.attr("userId"))
                }

                if (currentId > lastId) {  // latest selected row is below the previous selected row
                    currentRow.prevUntil(anchorRow).each((i, row) => {  //for every row between the current and last selected rows
                        $(row).addClass("ui-selected")
                    })
                } else if (currentId < lastId)  {  // latest selected row is above the previous selected row
                    currentRow.nextUntil(anchorRow).each((i, row) => {
                        $(row).addClass("ui-selected")
                    })
                }

                currentRow.addClass("ui-selected")
                anchorRow.addClass("ui-selected")
            }
            checkToSeeIfHideOrShowOptions()
            addDraggable()
            showDraggableIcons()
            anchorRow = currentRow
        },

        /**
         * Overrides the unselected method for the jQuery UI selectable widget.
         * Hides the drag grip on each row that has been unselected.
         *
         * @param e An event (unused)
         * @param ui The unselected rows
         */
        unselected: function(e, ui) {
            let unselected = $(ui.unselected)
            $(unselected).each(function () {
                $(this).find(".dragGrip").hide()
            })
        }
    })
}


/**
 * Implements the jQuery UI draggable widget to enable the dragging of group members between groups.
 * Reference: https://api.jqueryui.com/draggable/
 */
function addDraggable() {
    $(".dragGrip").draggable({
        helper: function () {
            let helper = $("<table class='table colourForDrag'/>")
            return helper.append($(".ui-selected").clone())
        },
        revert: true,
        appendTo: "body"
    })
}


/**
 * Displays the grip element on each jQuery UI selected row.
 */
function showDraggableIcons() {
    $(".ui-selected").find(".dragGrip").show()
}


//------------------------ Other Functions ------------------------------


/**
 * Ajax post request to the server for moving users from one group to another.
 */
function addUsers(groupId) {
    let arrayOfIds = [];
    let selected = $(".ui-selected")
    selected.each(function () {
        arrayOfIds.push($(this).attr("userId"))
    })
    arrayOfIds = Array.from(new Set(arrayOfIds))
    selected.removeClass("selected")
    $.ajax({
        url: `groups/addUsers?groupId=${groupId}&userIds=${arrayOfIds}`,
        type: "post",
        success: function () {
            displayGroupUsersList()
            if (parseInt(groupId) === MWAG_GROUP_ID) {
                createAlert("User(s) moved, and teachers role remains", "success")
            } else {
                createAlert("User(s) moved", "success")
            }
        },
        statusCode: {
          401: function () {
              createAlert("You don't have permission to move users. This could be because " +
                  "your roles have been updated. Try refreshing the page", "failure")
          }
        },
        error: function () {
            createAlert("Couldn't move users", "failure")
        }
    })
}


/**
 * Displays the options for what to do with selected users.
 *
 * @param show a boolean of if to show or hide
 */
function showOptions(show) {
    let groupsDisplayOptions = $("#groupDisplayOptions")
    if (groupsDisplayOptions.is(':hidden')) {
        if (show && (selectedGroupId !== TEACHER_GROUP_ID || isAdmin())) {
            groupsDisplayOptions.slideDown()
        } else {
            groupsDisplayOptions.slideUp()
        }

    }
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
    let amountSelected = $(".ui-selected").length
    showOptions(amountSelected > 0)
}


/**
 * Called when a group page is opened. This function sets the visibility of the group settings tab.
 * The visibility is true only if the user is in the group or is a teacher or admin.
 *
 * @param group - The newly selected group.
 */
function checkEditRights(group) {
    let groupSettingsTab = $("#groupSettingsTab")
    let groupEditButton = $("#editGroupNameButton")
    let groupId = group.id
    groupSettingsTab.hide()
    groupEditButton.hide()

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
    if (groupId !== MWAG_GROUP_ID &&
        groupId !== TEACHER_GROUP_ID &&
        (checkPrivilege() || group.userList.some(member => member.id === userIdent))) {
        groupSettingsTab.show()
        groupEditButton.show()
        //show edit button
    } else {
        changeToUsersTab()
    }
}


/**
 * Makes an ajax get call to the server and gets all the information for a particular group.
 * Loops through the groups members and adds them to the table.
 */
function displayGroupUsersList() {
    $.ajax({
        url: `group?groupId=${selectedGroupId}`,
        type: "GET",
        success: (response) => {
            $("#groupTableBody").empty();
            $("#groupInformationShortName").text(response.shortName);
            $("#groupInformationLongName").text(response.longName);
            group = response;
            appendMembersToGroup(response)
            $("#groupInformationContainer").slideDown()
            checkToSeeIfHideOrShowOptions()
            checkEditRights(response)
        },
        error: () => {
            createAlert("Couldn't retrieve users", "failure")
        }
    })
}


/**
 * Takes the details of a group and appends each user in the group to the group details user list div.
 *
 * @param group The group's details to be managed.
 */
function appendMembersToGroup(group) {
    let membersContainer = $("#groupTableBody")
    let imageSource;

    $.each(group.userList, function (member) {
        let user = group.userList[member]
        if (user.imagePath.length === 0) {
            imageSource = "defaultProfile.png"
        } else {
            imageSource = user.imagePath
        }

        membersContainer.append(`
                    <tr class="userRow ${checkPrivilege() ? "clickableRow" : ""}" userId=${sanitise(user.id)}>
                        <td class="userRowId">
                            <svg xmlns="http://www.w3.org/2000/svg" width="25" height="25" fill="currentColor" class="bi bi-grip-vertical dragGrip" style="display: none" viewBox="0 0 16 16">
                                    <path d="M7 2a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zM7 5a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zM7 8a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm-3 3a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm-3 3a1 1 0 1 1-2 0 1 1 0 0 1 2 0zm3 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                            </svg>
                            ${user.id}</td>
                        <td>
                            <img src=${imageSource} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                        </td>
                        <td>${sanitise(user.firstName)}</td>
                        <td>${sanitise(user.lastName)}</td>
                        <td>${sanitise(user.username)}</td>
                    </tr>`
        )
    })
}


/**
 * A function to get the git repo information from the repository and display it on the group page, if there is no repo
 * information then it changes the header to say there is no repository
 */
function retrieveGroupRepoInformation() {
    $.ajax({
        url: `getRepo?groupId=${selectedGroupId}`,
        type: "GET",
        success: (response) => {
            let repoInformationContainer = $("#gitRepo")
            repoInformationContainer.empty();
            if (response.length === 0) {
                populateEmptyGroupRepo(repoInformationContainer)
            } else {
                let group = response[0]
                populateGroupRepoInformation(repoInformationContainer, group)
            }
            getRepoCommits();
        }
    })
}


/**
 * Populates the group's git repo information section to display the lack of a repository.
 *
 * @param container The git repo information container.
 */
function populateEmptyGroupRepo(container) {
    container.append(`
        <div id="groupSettingsRepoInformationSection">
            <div id="groupSettingsRepoHeader">
                <h3 id="groupSettingsPageRepoName">No Repository</h3>
                <button type="button" class="editRepo noStyleButton marginSides1" data-bs-toggle="tooltip"
                        data-bs-placement="top" title="Edit Repository Settings">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                         class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                        <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                    </svg>
                </button>
            </div>
            <div id="repoSettingsContainer"></div>
        </div>`
    )
}


/**
 * Populates the given container with the given git repo information.
 *
 * @param container The git repo information container.
 * @param repo The repo information.
 */
function populateGroupRepoInformation(container, repo) {
    container.empty();
    container.append(`
        <div id="groupSettingsRepoInformationSection">
            <div id="groupSettingsRepoHeader">
                <h3 id="groupSettingsPageRepoName">${sanitise(repo.alias)}</h3>
                <button type="button" class="editRepo noStyleButton marginSides1" data-bs-toggle="tooltip"
                        data-bs-placement="top" title="Edit Repository Settings">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                         class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                        <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                    </svg>
                </button>
            </div>

            <div id="repoInfo" class="row marginSides1">
                <div class="inlineText col">
                    <p>Project Id:&nbsp;</p>
                    <p class="groupSettingsPageProjectId greyText">${sanitise(repo.projectId)}</p>
                </div>
                <div class="inlineText col">
                    <p>Access Token:&nbsp;</p>
                    <p class="groupSettingsPageAccessToken greyText">${sanitise(repo.accessToken)}</p>
                </div>
            </div>
            <div id="repoSettingsContainer"></div>
        </div>`
    )
}


/**
 * Gets the commits from the provided git repository and displays the first 3 if there are any, or changes the title to
 * state that there are no commits. If there is a problem accessing the webpage, an error is displayed under the repo
 * information.
 */
function getRepoCommits() {
    const commitContainer = $("#groupSettingsCommitSection");
    const repoID = $(".groupSettingsPageProjectId").text()
    const accessToken = $(".groupSettingsPageAccessToken").text();

    commitContainer.empty();

    if (repoID.length !== 0) {
        getCommits(repoID, accessToken, (data) => {
                if (data.length === 0) {
                    commitContainer.append(`<h5>No Recent Commits</h5>`)
                } else {
                    populateCommitContainer(commitContainer, data)
                }
            },
            () => {
                handleGitRepoError()
            }
        )
    }
}


/**
 * Populates the given commit container with the first 3 commits retrieved from the git repository. The data includes the url, short
 * Id, and commit message for each commit.
 *
 * @param commitContainer The container in which commits will be appended.
 * @param data The data retrieved from the repo, which contains the recent commits to be appended to the repo container.
 */
function populateCommitContainer(commitContainer, data) {
    commitContainer.append(`<h5>Recent Commits:</h5>`)

    const firstThree = data.slice(0, 3);

    for (let commit of firstThree) {
        let commitText =
            `<div id="groupSettingsCommitContainer" class="marginSides1">
                <div class="gitCommitInfo">
                    <div class="row">
                        <div class="inlineText">
                            <p>Commit:&nbsp;</p>
                            <a class="greyText" href="${commit.web_url}">${sanitise(commit.short_id)}</a>
                        </div>
                    </div>
                    <div class="row">
                        <p>${sanitise(commit.message)}</p>
                    </div>
                    <div class="row">
                        <div class="col">
                            <p class="greyText">${sanitise(commit.author_name)}</p>
                        </div>
                        <div class="col commitDate">
                            <p class="greyText">${sanitise(commit.committed_date).split("T")[0]}</p>
                        </div>
                    </div>
                </div>
             </div>`
        commitContainer.append(commitText)
    }
}


/**
 * Populates the group's git repo container to indicate a lack of commits in the repo.
 */
function handleGitRepoError() {
    let repoInformationContainer = $("#gitRepo");
    let repoProjectId = $(".groupSettingsPageProjectId");
    let repoAccessToken = $(".groupSettingsPageAccessToken");

    repoProjectId.removeClass('greyText')
    repoProjectId.addClass("redText")
    repoAccessToken.removeClass("greyText")
    repoAccessToken.addClass("redText")

    repoInformationContainer.append(`
        <div>
            <p style="color: red">One or more repository settings are invalid</p>
        </div>`
    )
}


/**
 * Performs all the actions required to close the group details edit form
 */
function cancelGroupEdit() {
    const parent = $("#groupEditInfo");
    parent.slideUp(() => {
        const editButton = $(".editButton");
        editButton.show();
    });
}


/**
 * When a group name is changed, this updates its new names to prevent the need to refresh the page
 */
function updateGroupName(shortname, longname) {
    const selectedGroup = $(".focusOnGroup");
    selectedGroup.find(".groupShortName").text(shortname);
    selectedGroup.find(".groupLongName").text(longname);
}


// ******************************* Click listeners *******************************


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
            },
            statusCode: {
                401: function () {
                    createAlert("You don't have permission to delete groups. This could be because " +
                        "your roles have been updated. Try refreshing the page", "failure")
                }
            }, error: function () {
                createAlert("Couldn't delete the group", "failure")
            }
        })
    }
})


/**
 * A listener for the edit group name button, opens up a form that allows teacher or admins to change the group names
 */
$(document).on("click", ".editButton", () => {
    const editButton = $(".editButton");
    editButton.hide();
    editButton.tooltip("hide");

    const shortName = $("#groupInformationShortName").text();
    const longName = $("#groupInformationLongName").text();

    $("#groupShortName").val(shortName)
    $("#groupLongName").val(longName)

    if (!checkPrivilege()) {
        $("#editShortNameInput").hide();
    }
    $("#groupEditInfo").slideDown();

    let formControl = $(".form-control");
    formControl.each(countCharacters);
    formControl.keyup(countCharacters);
})


/**
 * Event listener for the submit button of editing a group name
 */
$(document).on("submit", "#editGroupForm", function (event) {
    event.preventDefault();
    let url;
    let type;

    if (checkPrivilege()) {
        url = "groups/edit/details";
        type = "post";
    } else {
        url = "groups/edit/longName";
        type = "patch";
    }
    const groupData = {
        "groupId": selectedGroupId,
        "shortName": $("#groupShortName").val(),
        "longName": $("#groupLongName").val(),
    }

    $.ajax({
        url: url,
        type: type,
        data: groupData,
        success: function () {
            createAlert("Changes submitted");
            cancelGroupEdit();
            displayGroupUsersList();
            updateGroupName($("#groupShortName").val(), $("#groupLongName").val());
        },
        statusCode: {
            401: function () {
                createAlert("You don't have permission to edit group details. This could be because " +
                    "your roles have been updated. Try refreshing the page", "failure")
            }
        },
        error: () => {
            createAlert("Couldn't edit the group details", "failure")
        }
    })
})


/**
 * Event listener for the cancel button on the git repo edit form.
 */
$(document).on("click", ".cancelGroupEdit", cancelGroupEdit);


/**
 * When member removal is confirmed, a request is made to remove the selected users from the group.
 */
$(document).on("click", "#confirmRemoval", function () {
    let arrayOfIds = [];
    $(".ui-selected").each(function () {
        arrayOfIds.push($(this).attr("userId"))
    })
    $.ajax({
        url: `groups/removeUsers?groupId=${selectedGroupId}&userIds=${arrayOfIds}`,
        type: "DELETE",
        success: () => {
            displayGroupUsersList()
            createAlert("User removed", "success")
        },
        statusCode: {
            401: function () {
                createAlert("You don't have permission to remove users. This could be because " +
                    "your roles have been updated. Try refreshing the page", "failure")
            }
        },
        error: () => {
            createAlert("Couldn't remove users from group", "failure")
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
    retrieveGroupRepoInformation()
})


/**
 * a listener for when the move users button is pushed, calls a function to move the currently selected users to the
 * selected group
 */
$(document).on("click", "#moveUsersButton", function () {
    addUsers($("#newGroupSelector").val())
})


/**
 * selects every single user in the group when the button is clicked.
 */
$(document).on("click", "#selectAllCheckboxGroups", function () {
    let isChecked = $("#selectAllCheckboxGroups").prop("checked")
    $(".selectUserCheckboxGroups").prop("checked", isChecked)
    if (isChecked) {
        $(".userRow").addClass("selected")
        showDraggableIcons()
    } else {
        let userRow = $(".userRow")
        userRow.removeClass("selected")
        userRow.find(".dragGrip").hide()
    }
    checkToSeeIfHideOrShowOptions()

})


/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    $(".scrollableGroupOverview").css("width", "50%");
    $(".group").removeClass("focusOnGroup")
    selectedGroupId = $(this).closest(".group").find(".groupId").text();
    let groupShortname = $(this).closest(".group").find(".groupShortName").text();
    $("#selectAllCheckboxGroups").prop("checked", false);
    displayGroupUsersList();
    retrieveGroupRepoInformation()

    if (groupShortname === "Teachers") { // teacher group
        $("#groupRemoveUser").show();
        $(".controlButtons").hide();
    } else if (groupShortname === "Non-Group") { // non-group group
        $("#groupRemoveUser").hide();
        $(".controlButtons").hide();
    } else {
        $("#groupRemoveUser").show();
        $(".controlButtons").show();
    }
    $("#confirmationForm").slideUp();
    $("#groupEditInfo").slideUp();
    $(this).closest(".group").addClass("focusOnGroup");
})
