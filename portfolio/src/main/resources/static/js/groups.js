let controlDown = false;
let shiftDown = false;
let selectedGroupId;
let lastSelectedRow;
let group;
let notCtrlClick = true;
const TEACHER_GROUP_ID = 1
const MWAG_GROUP_ID = 2


$(document).ready(function () {
    let arrayOfSelected = []

    /**
     * JQuery UI Selectable interaction
     * https://api.jqueryui.com/selectable/
     */
    $("table > tbody").selectable({ //

        // Don't allow individual table cell selection.
        filter: ":not(td)",

        /**
         * Runs a function over every selected element
         * @param e event
         * @param ui the selectable item that has been selected
         */
        selected: function (e, ui) {
            let currentlySelected = $(ui.selected)
            notCtrlClick = !e.ctrlKey
            if (shiftDown) { // Checks if the shift key is currently pressed
                notCtrlClick = false
                if (parseInt(currentlySelected.attr("userId")) > parseInt(lastSelectedRow.attr("userId"))) {
                    currentlySelected.prevUntil(lastSelectedRow).each(function () {
                        $(this).addClass("selected")
                        arrayOfSelected.push($(this))
                    })

                } else if (currentlySelected.attr("userId") < parseInt(lastSelectedRow.attr("userId"))) {
                    currentlySelected.nextUntil(lastSelectedRow).each(function () {
                        $(this).addClass("selected")
                        arrayOfSelected.push($(this))
                    })

                }
                lastSelectedRow.addClass("selected")
                currentlySelected.addClass("selected");
                arrayOfSelected.push(lastSelectedRow)
                arrayOfSelected.push(currentlySelected)
            } else {
                arrayOfSelected.push(ui)
            }
            lastSelectedRow = currentlySelected // Sets the last selected row to the currently selected one.
        },

        /**
         * Triggered at the end of the select operation.
         */
        stop: function () {
            if (arrayOfSelected.length === 1) {
                if ($(arrayOfSelected[0].selected).hasClass("selected")) {
                    $(arrayOfSelected[0].selected).removeClass("selected")
                } else {
                    $(arrayOfSelected[0].selected).addClass("selected")
                }
            }
            if (notCtrlClick) {
                $(".selected").removeClass("selected")
                lastSelectedRow.addClass("selected")
            }
            $(".userRow").each(function () {
                if (!$(this).hasClass("selected") && $(this).hasClass("ui-draggable")) {
                    try {
                        $(this).draggable("destroy")
                    } catch (err) {
                    }
                }
            })
            arrayOfSelected = []
            checkToSeeIfHideOrShowOptions()
            addDraggable()
        },

        /**
         * Triggered at the end of the select operation, on each element removed from the selection.
         * @param e event
         * @param ui The selectable item that has been unselected.
         */
        unselected: function (e, ui) {
            if (notCtrlClick) {
                $(ui.unselected).removeClass("selected");
            }
            checkToSeeIfHideOrShowOptions()
        }
    });


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
            }
        })
    }
})

// ******************************* Functions *******************************


/**
 * Makes the selected elements able to  be draggable with the mouse
 * https://api.jqueryui.com/draggable/
 */
function addDraggable() {
    $(".selected").draggable({
        helper: function () {
            let helper = $("<table class='table colourForDrag'/>")
            return helper.append($(".selected").clone())
        },
        revert: true,
    })
}

/**
 * Ajax post request to the server for moving users from one group to another
 */
function addUsers(groupId) {
    let arrayOfIds = [];
    let selected = $(".selected")
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
            createAlert("User(s) moved", false)
        },
        error: function (response) {
            // Log this
        }
    })
}


/**
 * Displays the options for what to do with selected users.
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
        error: (error) => {
            createAlert(error.responseText, true)
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
                <h3 id="groupSettingsPageRepoName">${repo.alias}</h3>
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
                    <p class="groupSettingsPageProjectId greyText">${repo.projectId}</p>
                </div>
                <div class="inlineText col">
                    <p>Access Token:&nbsp;</p>
                    <p class="groupSettingsPageAccessToken greyText">${repo.accessToken}</p>
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
                            <a class="greyText" href="${commit.web_url}">${commit.short_id}</a>
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
                            <p class="greyText">${commit.committed_date.split("T")[0]}</p>
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
            }, error: function (error) {
                createAlert(error.responseText, true)
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

    $("#groupShortName").text($("#groupInformationShortName").text())
    $("#groupLongName").text($("#groupInformationLongName").text())

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
        url = "/groups/edit/details";
        type = "post";
    } else {
        url = "/groups/edit/longName";
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
        error: (error) => {
            createAlert(error.responseText, true)
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
    $(".selected").each(function () {
        arrayOfIds.push($(this).attr("userId"))
    })
    $.ajax({
        url: `groups/removeUsers?groupId=${selectedGroupId}&userIds=${arrayOfIds}`,
        type: "DELETE",
        success: () => {
            displayGroupUsersList()
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
    } else {
        $(".userRow").removeClass("selected")
    }
    checkToSeeIfHideOrShowOptions()

})


/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
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


// ******************************* Keydown listeners *******************************


$(document).keydown(function (event) {
    if (event.key === "Control") {
        controlDown = true;
    }
})


$(document).keyup(function (event) {
    if (event.key === "Control") {
        controlDown = false;
    }
})


$(document).keydown(function (event) {
    if (event.key === "Shift") {
        shiftDown = true;
    }
})


$(document).keyup(function (event) {
    if (event.key === "Shift") {
        shiftDown = false;
    }
})


// ******************************* Change listeners *******************************


$(document).on("change", "input[type=checkbox]", function () {
    let tableRow = $(this).closest("tr")
    if (!tableRow.hasClass("tableHeader")) {
        $(this).closest("tr").toggleClass("selected")
    }
    checkToSeeIfHideOrShowOptions()
})
