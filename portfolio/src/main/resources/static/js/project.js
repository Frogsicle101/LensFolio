$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()



    // Project button edit
    $("#projectEditSprint").click(() => {
        location.href = "/editProject?projectId=" + projectId;
    })
    // Project button add
    $("#projectAddSprint").click(function () {
        location.href = "/portfolio/addSprint?projectId=" + projectId;
    })



    // Sprint Button Edit
    $(".editSprint").click(function () {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        location.href = "/sprintEdit?sprintId=" + $(this).closest(".sprint").find(".sprintId").text();
    })

    // Sprint Button delete
    $(".deleteSprint").click(function() {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        $.ajax({
            url: "deleteSprint",
            type: "DELETE",
            data: {"sprintId": sprintId},
        }).done(function () {
            location.href = "/portfolio"
        })
    })

    $(".addEventButton").click(function() {
        $(".eventForm").slideToggle();
    })

    $("#eventSubmit").click(function(event) {
        event.preventDefault();
        let eventData = {
            "projectId": projectId,
            "eventName": $("#eventName").val(),
            "eventStart": $("#eventStart").val(),
            "eventEnd": $("#eventEnd").val()
        }
        $.ajax({
            url: "/addEvent",
            type: "put",
            data: eventData,
            success: function(response) {
                location.href = "/portfolio"
            },
            error: function(response) {

            }
        })
    })




})





