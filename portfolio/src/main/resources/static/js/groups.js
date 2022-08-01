let controlDown = false;
let shiftDown = false;
let selectedGroupId;
let lastSelectedRow;
let group;
let singleClick = true;

const TEACHER_GROUP_ID = 1

$(document).ready(function() {

    /**
     * JQuery UI Selectable interaction
     * https://api.jqueryui.com/selectable/
     */
    $( "table > tbody" ).selectable({ //

        // Don't allow individual table cell selection.
        filter: ":not(td)",

        /**
         * Runs a function over every selected element
         * @param e event
         * @param ui the selectable item that has been selected
         */
        selected: function(e, ui) {
            let currentlySelected = $(ui.selected)
            currentlySelected.addClass("selected")
            singleClick = !e.ctrlKey
            if (shiftDown) { // Checks if the shift key is currently pressed
                singleClick = false
                if (parseInt(currentlySelected.attr("id")) > parseInt(lastSelectedRow.attr("id"))) {
                    currentlySelected.prevUntil(lastSelectedRow).addClass("selected")
                } else if (currentlySelected.attr("id") < parseInt(lastSelectedRow.attr("id"))) {
                    currentlySelected.nextUntil(lastSelectedRow).addClass("selected")
                }
                lastSelectedRow.addClass("selected")
                currentlySelected.addClass( "selected" );
            }
            lastSelectedRow = currentlySelected // Sets the last selected row to the currently selected one.
            /**
             * Makes the selected elements able to  be draggable with the mouse
             * https://api.jqueryui.com/draggable/
             */
            $(".selected").draggable({
                helper: function(event){
                    let helper = $("<table class='table colourForDrag'/>")
                    return helper.append($(".selected").clone())

                },
                revert: true,
                /**
                 * Triggered when dragging starts.
                 */
                start: function(){
                },

                /**
                 * Triggered when the dragging stops
                 */
                stop: function() {
                }
            })
            checkToSeeIfHideOrShowOptions()

        },

        /**
         * Triggered at the end of the select operation.
         */
        stop: function() {
            if (singleClick) {
                $(".selected").removeClass("selected")
                lastSelectedRow.addClass("selected")
            }
        },

        /**
         * Triggered at the end of the select operation, on each element removed from the selection.
         * @param e event
         * @param ui The selectable item that has been unselected.
         */
        unselected: function(e, ui) {
            $( ui.unselected ).removeClass( "selected" );
            $( ui.unselected ).removeClass("ui-draggable")
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
             * @param event
             * @param ui
             */
            over: function(event, ui) {
                $(this).effect("shake")
                //https://api.jqueryui.com/category/effects/
            },

            /**
             * Triggered when an accepted draggable is dropped on the droppable (based on thetolerance option).
             * https://api.jqueryui.com/droppable/#event-drop
             * @param event
             */
            drop: function(event) {
                addUsers($(this).attr("id"))
                $(".group").removeClass("ui-state-highlight")
            }
        })
    }


})

// ******************************* Functions *******************************

/**
 * Ajax post request to the server for moving users from one group to another
 */
function addUsers(groupId) {
    let arrayOfIds = [];
    $(".selected").each(function() {
        arrayOfIds.push($(this).attr("userId"))
    })
    $.ajax({
        url: `/groups/addUsers?groupId=${groupId}&userIds=${arrayOfIds}`,
        type: "post",
        success: function() {
            displayGroupUsersList(selectedGroupId)
        },
        error: function(response) {
            console.log(response)
        }
    })
}


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
                if (response.userList[member].imagePath.length === 0) {
                    imageSource = "defaultProfile.png"
                } else {
                    imageSource = response.userList[member].imagePath
                }
                membersContainer.append(
                    `<tr class="userRow" userId=${response.userList[member].id} id="${member}">
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
            checkToSeeIfHideOrShowOptions()

        },
        error: (error) => {
            $("#groupInformationContainer").append(`<div class="alert alert-danger alert-dismissible fade show" role="alert">
                                                       ${error.responseText}
                                                       <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                                     </div>`)
        }
    })

}


/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup")
    let groupId = $(this).closest(".group").find(".groupId").text();
    let groupShortname = $(this).closest(".group").find(".groupShortName").text();
    $("#selectAllCheckboxGroups").prop("checked", false);
    displayGroupUsersList(groupId);

    $(this).closest(".group").addClass("focusOnGroup")

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





// ******************************* Click listeners *******************************

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
 * Ajax post request to the server for moving users from one group to another
 */
$(document).on("click", "#moveUsersButton", function() {
    addUsers($("#newGroupSelector").val())
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

// /**
//  * handles all the functionality for selecting users.
//  * Allows for using shift+click and ctrl+click functionality.
//  */
// $(document).on("click", ".userRow", function() {
//
//     if (shiftDown) {
//         let boundaries = []; // Boundaries in this case are the first user, and the last user, used to select everything between.
//         boundaries.push(lastSelectedRow)
//         boundaries.push($(this).attr("userId"));
//         boundaries[0] = parseInt(boundaries[0])
//         boundaries[1] = parseInt(boundaries[1])
//         boundaries = boundaries.sort()
//         $(".userRow").each(function() {
//             if ($(this).attr("userId") >= boundaries[0] && $(this).attr("userId") <= boundaries[1]) {
//                 $(this).addClass("selected")
//                 $(this).find("input[type=checkbox]").prop("checked", true)
//             }
//         })
//     }
//     lastSelectedRow = $(this).attr("userId")
//     checkToSeeIfHideOrShowOptions()
//
//
// })

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
