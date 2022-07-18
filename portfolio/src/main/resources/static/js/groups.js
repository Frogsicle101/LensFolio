let controlDown = false;
let shiftDown = false;
let selectedGroupId;
let lastSelectedRow;
/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup")
    let groupId = $(this).closest(".group").find(".groupId").text();
    displayGroupUsersList(groupId);

    $(this).closest(".group").addClass("focusOnGroup")
})

function showOptions(show) {
    if (show) {
        $("#groupDisplayOptions").slideDown()
    } else {
        $("#groupDisplayOptions").slideUp()
    }
    $(".numSelected").text($(".selected").length + " Selected")
}


function checkToSeeIfHideOrShowOptions() {
    let amountSelected = $(".selected").length
    if (amountSelected > 0) {
        showOptions(true)
    } else {
        showOptions(false)
    }
}


$(document).on("click", "#moveUsersButton", function() {
    let arrayOfIds = [];

    $(".selected").each(function() {
        arrayOfIds.push($(this).attr("userId"))
    })
    $.ajax({
        url: `/groups/addUsers?groupId=${$("#newGroupSelector").val()}&userIds=${arrayOfIds}`,
        type: "post",
        success: function() {
            window.location.reload()
        },
        error: function(response) {
            console.log(response)
        }
    })
})




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

$(document).on("change","input[type=checkbox]", function() {
    let tableRow = $(this).closest("tr")
    if (!tableRow.hasClass("tableHeader")) {
        $(this).closest("tr").toggleClass("selected")
    }
    checkToSeeIfHideOrShowOptions()

})



$(document).on("click", ".userRow", function() {
    if (!controlDown && !shiftDown) {
        $(".selected").each(function() {
            $(this).removeClass("selected")
            $(this).find("input[type=checkbox]").prop("checked", false)
        })
    }

    if (shiftDown) {
        let boundaries = [];
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
            for (let member in response.userList) {
                let imageSource;
                if (response.userList[member].imagePath.length === 0) {
                    imageSource = "defaultProfile.png"
                } else {
                    imageSource = response.userList[member].imagePath
                }
                membersContainer.append(
                 `<tr class="userRow" userId=${response.userList[member].id}>
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


