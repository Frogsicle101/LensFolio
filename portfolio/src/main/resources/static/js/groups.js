let group;

/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    $(".group").removeClass("focusOnGroup")
    let groupId = $(this).closest(".group").find(".groupId").text();
    displayGroupUsersList(groupId);

    $(this).closest(".group").addClass("focusOnGroup")
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
                    imageSource = response.userList[member].imagePath
                }
                membersContainer.append(
                    `<tr class="tableRowGroups">
                     <th scope="row"><input class="selectUserCheckboxGroups" type="checkbox"/></th>
                    <td>${response.userList[member].id}</td>
                    <td>
                        <img src=${response.userList[member].imagePath} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                    </td>
                    <td>${response.userList[member].firstName}</td>
                    <td>${response.userList[member].lastName}</td>
                    <td>${response.userList[member].username}</td>
                </tr>`
                )
            }
        },
        error: (error) => {
            console.log(error);
        }

    })
    $("#groupInformationContainer").slideDown()

}


