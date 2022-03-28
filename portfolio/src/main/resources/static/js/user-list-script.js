
$(document).ready(function(){

    $(".editRoleSaveButton").click(function() {
        let roleList = $(this).siblings().children(":selected").text()
        let userId = $(this).closest(".userId").length;
        console.log(roleList)
        console.log(userId)

        $.ajax('/editUserRole', {
            type: "POST",
            data:  {"userId": userId,
                "newUserRoles": roleList},
            success: function() {
                // location.href = "/user-list"
            }
        })
    })
// $(".editRoleSaveButton").siblings(".roleWithButton").find(":selected").text();
// let userId = $(".editRoleSaveButton").closest(".userRoleRow").find("userId").text();
})

function toggleEditRoles(buttonHideId, roleSelectorId, buttonShowId) {
    document.getElementById(buttonHideId).style.display='none';
    document.getElementById(buttonShowId).style.display='';

    const roleEditor = document.getElementById(roleSelectorId);
    if (roleEditor.style.display === 'none') {
        roleEditor.style.display='';
        showRoleEditor();
    } else {
        roleEditor.style.display='none';
        hideRoleEditor(roleSelectorId);
    }
}

// hides the roles column in table
// displays role selector column in table
function showRoleEditor() {
    for (let i = 0 ; i < document.getElementById("user-list").getElementsByTagName('tr').length - 1 ; i++) {
        const colToHide = document.getElementById("user-list").getElementsByTagName('th')[3]
        colToHide.style.display = 'none'
        const colToShow = document.getElementById("user-list").getElementsByTagName('th')[4]
        colToShow.style.display=''
    }
}

// saves the state of the role selector for each user
// hides the role selector
// displays an updated roles column in the table
// function hideRoleEditor(roleSelectorId) {
//     var table = document.getElementById("user-list")
//     if (table) {
//         (Array.from(table.ariaRowCount).forEach(tr, tr.rowIndex) => {
//             (Array.from(tr.cells).forEach((cell, col_ind) => {
//                 if (col_ind === 3) {
//                     cell.display;
//                 } else if (col_ind === 4) {
//                     cell.hide();
//                 })
//             })))
//             // const colToShow = table.getElementsByTagName('th')[3]
//             // colToShow.style.display = ''
//             // const colToHide = table.getElementsByTagName('th')[4]
//             // colToHide.style.display = 'none'
//         })
//     }
//     // for (let i = 0 ; i < table.getElementsByTagName('tr').length - 1 ; i++){
//     //
//     // }
// }


    // let roles = $("#FormSelectRoles").val();
    // $.ajax({
    //     url: "setRoles",
    //     type: "PUT",
    //     data: {"roles" : roles}
    //     }).done(function (data) {
    //     $("user-role-selection").hide();
    //     $("#user-list").html(data);
    // })
