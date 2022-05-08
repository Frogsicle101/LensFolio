$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()




    /**
     * When project edit button is clicked.
     * Redirect page.
     */
    $("#projectEditSprint").click(() => {
        location.href = "editProject?projectId=" + projectId ;
    })
    /**
     * When project add sprint button is pressed.
     * Redirect page.
     */
    $("#projectAddSprint").click(function () {
        $.ajax({
            url: "portfolio/addSprint?projectId=" + projectId,
            success: function (){
                location.reload()
            },
            error: function(error){
                console.log(error.responseText)
                $(".sprintAddErrorMessage").text(error.responseText)
                $(".sprintAddAlert").slideUp()
                $(".sprintAddAlert").slideDown()
            }
        })

    })

    $(".collapseAlert").click(function(){
        $(this).parent().slideUp();
    })

    /**
     * When edit sprint button is clicked.
     * Redirect page.
     */
    $(".editSprint").click(function () {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        location.href = "sprintEdit?sprintId=" + sprintId +"&projectId=" + projectId;
    })

    /**
     * When sprint delete button is clicked.
     * Sends ajax delete request.
     * Then reloads page.
     */
    $(".deleteSprint").click(function() {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        $.ajax({
            url: "deleteSprint",
            type: "DELETE",
            data: {"sprintId": sprintId},
        }).done(function () {
            location.href = "/portfolio?projectId=" + projectId
        })
    })



    setAddSprintButtonPlacement()
    function setAddSprintButtonPlacement() {
        $(".addSprint").css("left", $(".eventContainer").width() + "px")
        $(".addSprint").css("bottom",0 -  $(".addSprintSvg").height()/2 + "px")
    }





})





