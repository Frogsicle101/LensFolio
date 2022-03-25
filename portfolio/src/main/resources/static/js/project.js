$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()


    /**
     * When project edit button is clicked.
     * Redirect page.
     */
    $("#projectEditSprint").click(() => {
        location.href = "/editProject?projectId=" + projectId;
    })
    /**
     * When project add sprint button is pressed.
     * Redirect page.
     */
    $("#projectAddSprint").click(function () {
        location.href = "/portfolio/addSprint?projectId=" + projectId;
    })


    /**
     * When edit sprint button is clicked.
     * Redirect page.
     */
    $(".editSprint").click(function () {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        location.href = "/sprintEdit?sprintId=" + $(this).closest(".sprint").find(".sprintId").text();
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
            location.href = "/portfolio"
        })
    })

    /**
     * Slide toggle for when add event button is clicked.
     */
    $(".addEventButton").click(function() {
        $(".eventForm").slideToggle();
    })


    /**
     * When event is submitted.
     */
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

    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.

    /**
     * Function that gets the maxlength of an input and lets the user know how many characters they have left.
     */
    function countCharacters() {
        let maxLength = $(this).attr("maxLength")
        let lengthOfCurrentInput = $(this).val().length;
        let counter = maxLength - lengthOfCurrentInput;
        let helper = $(this).next(".form-text"); //Gets the next div with a class that is form-text

        //If one character remains, changes from "characters remaining" to "character remaining"
        if (counter !== 1) {
            helper.text(counter + " characters remaining")
        } else {
            helper.text(counter + " character remaining")
        }

    }



})





