$(document).ready(() => {



    $("#projectEditSprint").click(() => {
        let projectId = $("#projectId").html()
        location.href = "/editProject?projectId=" + projectId
    })

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





