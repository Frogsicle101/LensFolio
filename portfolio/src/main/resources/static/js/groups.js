
/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    let members = $(this).closest(".group").find(".members");
    console.log(($(this).closest(".group")))
    console.log(members)
    displayGroupUsersList(members);
})

function displayGroupUsersList(members) {
    let membersContainer = $(".membersContainer")
    for (let member in members){
        membersContainer.append(
            `<div class="groupMember">
                <h3 class="userName">${member.username}</h3>
            </div>`
        )}
    membersContainer.slideDown(400)
}


