

// function getUserList() {
//     let page = find.
//     $.ajax({
//         url: "user-list",
//         type: "GET",
//         data: {"page" : page},
//         async: false
//     }).done(function(results) {
//     }
// }

function toggleEditRoles(buttonHideId, roleSelectorId, buttonShowId) {
    document.getElementById(buttonHideId).style.display='none';
    document.getElementById(buttonShowId).style.display='';

    const roleEditor = document.getElementById(roleSelectorId);
    if (roleEditor.style.display === 'none') {
        roleEditor.style.display='';
        showRoleEditor();
    } else {
        roleEditor.style.display='none';
        // hideRoleEditor(roleSelectionId);
    }
}

// hides the roles column in table
// displays role selector column in table
function showRoleEditor() {
    // const colToHide = document.getElementById("user-list").getElementsByTagName('th')[3]
    // colToHide.style.display='none'
    $('td:nth-child(3)').hide();
        // colToHide.style.visibility="collapse";
}

// saves the state of the role selector for each user
// hides the role selector
// displays an updated roles column in the table
function hideRoleSelector(roleSelectorId) {}


    // let roles = $("#FormSelectRoles").val();
    // $.ajax({
    //     url: "setRoles",
    //     type: "PUT",
    //     data: {"roles" : roles}
    //     }).done(function (data) {
    //     $("user-role-selection").hide();
    //     $("#user-list").html(data);
    // })
