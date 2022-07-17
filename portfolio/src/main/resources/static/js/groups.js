let group;

/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    let groupId = $(this).closest(".group").find(".groupId").text();
    displayGroupUsersList(groupId);
})

$(document).on("click", "#selectAllCheckboxGroups", function () {
    $(".selectUserCheckboxGroups").prop("checked", $("#selectAllCheckboxGroups").prop("checked"))
    updateNumberSelectedDisplay($("input[type=checkbox]").length)

})

$(document).on("change", "input[type=checkbox]", function () {
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
            console.log(response)
            group = response
            $("#groupTableBody").empty();
            $("#groupInformationShortName").text(response.shortName);
            $("#groupInformationLongName").text(response.longName);
            let baseUrl = window.location.origin
            console.log(window.location)
            for (let member in response.userList) {
                membersContainer.append(
                    `<tr class="tableRowGroups">
                     <th scope="row"><input class="selectUserCheckboxGroups" type="checkbox"/></th>
                    <td>${response.userList[member].id}</td>
                    <td>
                        <img src=${'http://localhost:9001' + response.userList[member].imagePath} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
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
    membersContainer.slideDown(400)

}


