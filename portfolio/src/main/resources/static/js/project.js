$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()



    // Project buttons
    $("#projectEditSprint").click(() => {
        location.href = "/editProject?projectId=" + projectId;
    })

    $("#projectAddSprint").click(function () {
        $.ajax({
            url: "addSprint",
            type: "POST",
            data: {"projectId": projectId},
        }).done(function () {
            location.reload()
        }).fail(function () {

        })
    })









    // Sprint Buttons
    $(".editSprint").click(function () {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        location.href = "/sprintEdit?sprintId=" + $(this).closest(".sprint").find(".sprintId").text();
    })


    $(".deleteSprint").click(function() {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        $.ajax({
            url: "deleteSprint",
            type: "DELETE",
            data: {"sprintId": sprintId},
        }).done(function () {
             location.reload()
        })

    })



})





