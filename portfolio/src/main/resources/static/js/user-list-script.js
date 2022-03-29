
$(document).ready(function() {

    $(".roleDeleteButton").click(function () {
        let role = $(this).siblings().text();
        let userId = $(this).closest(".roleButtonsContainer").siblings(".userId").text();
        $.ajax("/editUserRole", {
            type: "DELETE",
            data: {
                "userId": userId,
                "role": role
            },
            success: function () {
                $(this).parent().parent().remove()
            }
        })

    })

    $(".addRolePopUpButton").click(function () {
        $(this).siblings(".collapse").collapse('toggle');
    })

    $(".roleToAddButton").click(function () {

        let role = $(this).text();
        let userId = $(this).closest(".roleButtonsContainer").siblings(".userId").text();
        $.ajax({
            url: "/editUserRole",
            type: "POST",
            data: {
                "userId": userId,
                "role": role
            }
        })
    });




    // // Be gone soon
    // $(".editRoleSaveButton").click(function() {
    //     let roleList = $(this).siblings().children(":selected").text()
    //     let username = $(this).parent().siblings(".username").text();
    //     console.log(roleList)
    //     console.log(username)
    //
    //     $.ajax('/editUserRole', {
    //         type: "POST",
    //         data:  {"username": username,
    //             "newUserRoles": roleList},
    //         success: function() {
    //             location.href = "/user-list"
    //         }
    //     })
    // })
// $(".editRoleSaveButton").siblings(".roleWithButton").find(":selected").text();
// let userId = $(".editRoleSaveButton").closest(".userRoleRow").find("userId").text();
})

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
