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
        url: `/groups/addUsers?groupId=${groupId}&userIds=${arrayOfIds}`,
        type: "post",
        success: function () {
            displayGroupUsersList(selectedGroupId)
            createAlert("User(s) moved", false)
        },
        error: function (response) {
            console.log(response)
        }
    })
}


/**
 * Displays the options for what to do with selected users.
 * @param show a boolean of if to show or hide
 */
function showOptions(show) {
    if ($("#groupDisplayOptions").is(':hidden')) {
        if (show && (selectedGroupId !== TEACHER_GROUP_ID || isAdmin())) {
            $("#groupDisplayOptions").slideDown()
        } else {
            $("#groupDisplayOptions").slideUp()
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
    if (groupId !== MWAG_GROUP_ID &&
        groupId !== TEACHER_GROUP_ID &&
        (checkPrivilege() || group.userList.some(member => member.id === userIdent))) {
        groupSettingsTab.show()
    } else {
        changeToUsersTab()
    }
}


/**
 * Makes an ajax get call to the server and gets all the information for a particular group.
 * Loops through the groups members and adds them to the table.
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
            $(this).closest(".group").addClass("focusOnGroup");
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


function displayGroupRepoInformation () {
    let repoInformationContainer = $("#gitRepo")
    $.ajax({
        url: `getRepo?groupId=${selectedGroupId}`,
        type: "GET",
        success: (response) => {
            if (response.length === 0){
                repoInformationContainer.empty();
                repoInformationContainer.append(`
                    <h3 id="groupSettingsPageRepoName">No Repository</h3>`
                )
            }
            else {
                let repo = response[0];
                repoInformationContainer.empty();
                repoInformationContainer.append(`
                        <h3 id="the-group-settings-page-repo-name">${repo.alias}</h3>
                             <div class="row margin-sides-1">
                                <div class="inline-text col">
                                    <p>Project Id:&nbsp;</p>
                                    <p class="groupSettingsPageProjectId greyText" >${repo.projectId}</p>
                                </div>
                                <div class="inline-text col">
                                    <p>Access Token:&nbsp;</p>
                                    <p class="groupSettingsPageAccessToken greyText" >${repo.accessToken}</p>
                                </div>
                             </div>`
                )
                getRepoCommits();
            }

        },
        error: (error) => {
            console.log(error);
        },
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


function getRepoCommits() {
    const repoID = $(".groupSettingsPageProjectId").text()
    const accessToken = $(".groupSettingsPageAccessToken").text();
    getCommits(repoID, accessToken, (data) => {

        const firstThree = data.slice(0, 3);

        const commitContainer = $("#groupSettingsCommitContainer");
        commitContainer.empty();
        for (let commit of firstThree) {
            let commitText =
                `<div class="gitCommitInfo">
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
            </div>`
            commitContainer.append(commitText)
        }
    })
}


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
    displayGroupRepoInformation()
})


// ******************************* Click listeners *******************************


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
    let groupId = $(this).closest(".group").find(".groupId").text();
    selectedGroupId = groupId;
    let groupShortname = $(this).closest(".group").find(".groupShortName").text();
    $("#selectAllCheckboxGroups").prop("checked", false);
    displayGroupUsersList(groupId);
    displayGroupRepoInformation()

    //$(this).closest(".group").addClass("focusOnGroup")

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
