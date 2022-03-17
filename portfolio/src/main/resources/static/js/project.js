$(document).ready(() => {
    let projectId = $("#projectId").html()
    console.log(projectId)
    $("#projectEditSprint").click(() => {
        console.log("clicked")
        let projectId = $("#projectId").html()
        console.log(projectId)
        location.href = "/editProject?projectId=" + projectId
    })


    $(".sprint").each((element) => {
        let sprintColour = $(this).find(".sprintColour");
        console.log(sprintColour)
        // $(element).css("border","solid 1rem red" )
    })
})



