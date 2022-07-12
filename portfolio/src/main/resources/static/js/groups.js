
/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    let groupId = $(this).closest(".group").find(".groupId").text();

    displayGroupUsersList(groupId);
})

function displayGroupUsersList(groupId) {
    let membersContainer = $(".membersContainer")
    $.ajax({
        url: `group?groupId=${groupId}`,
        type: "GET",
        success: (response) => {
            membersContainer.empty();
            $("#groupInformationShortName").text(response.shortName);
            $("#groupInformationLongName").text(response.longName);
            for (let member in response.userList) {
                membersContainer.append(
                    `<div class="groupMember">
                        <h3 class="userName">${response.userList[member].username}</h3>
                    </div>`
                )}
        },
        error: (error) => {
            console.log(error);
        }
    })
    membersContainer.slideDown(400)

}


