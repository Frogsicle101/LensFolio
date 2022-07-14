$(document).ready( function() {


})
/**
 * When group div is clicked, the members for that group are retrieved.
 */
$(document).on("click", ".group", function () {
    let groupId = $(this).closest(".group").find(".groupId").text();
    displayGroupUsersList(groupId);
})

$(document).on("click", "#selectAllCheckboxGroups", function() {
    $(".selectUserCheckboxGroups").prop("checked", $("#selectAllCheckboxGroups").prop("checked"))


})

$(document).on("change","input[type=checkbox]", function() {
    let tableRow = $(this).closest("tr")
    if (!tableRow.hasClass("tableHeader")) {
        $(this).closest("tr").toggleClass("selected")
    }
    $(".numSelected").text($(".selected").length + " Selected")

})


function displayGroupUsersList(groupId) {
    let membersContainer = $("#groupTableBody")
    $.ajax({
        url: `group?groupId=${groupId}`,
        type: "GET",
        success: (response) => {
            console.log(response)
            $("#groupTableBody").empty();
            $("#groupInformationShortName").text(response.shortName);
            $("#groupInformationLongName").text(response.longName);
            let baseUrl = window.location.origin
            console.log(window.location)
            for (let member in response.userList) {
                membersContainer.append(
                 `<tr>
                     <th scope="row"><input class="selectUserCheckboxGroups" type="checkbox"/></th>
                    <td>${response.userList[member].id}</td>
                    <td>
                        <img src=${'http://localhost:9001' + response.userList[member].imagePath} alt="Profile image" class="profilePicGroupsList" id="userImage"> 
                    </td>
                    <td>${response.userList[member].firstName}</td>
                    <td>${response.userList[member].lastName}</td>
                    <td>${response.userList[member].username}</td>
                </tr>`
                )}
        },
        error: (error) => {
            console.log(error);
        }
    })
    membersContainer.slideDown(400)

}


