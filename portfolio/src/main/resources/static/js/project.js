$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()

    /**
     * When project edit button is clicked.
     * Redirect page.
     */
    $("#projectEditSprint").click(() => {
        location.href = "/editProject?projectId=" + projectId ;
    })
    /**
     * When project add sprint button is pressed.
     * Redirect page.
     */
    $("#projectAddSprint").click(function () {
        $.ajax({
            url: "/portfolio/addSprint?projectId=" + projectId,
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
        location.href = "/sprintEdit?sprintId=" + sprintId +"&projectId=" + projectId;
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

    /**
     * Slide toggle for when add event button is clicked.
     */
    $(".addEventButton").click(function() {
        $(".addEventSvg").toggleClass('rotated');
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
            "eventEnd": $("#eventEnd").val(),
            "typeOfEvent": $(".typeOfEvent").val()
        }
        console.log(eventData.typeOfEvent)
        if (eventData.eventName.toString().length === 0 || eventData.eventName.toString().trim().length === 0){
            $(this).closest(".eventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter an event name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
        } else if (eventData.eventEnd < eventData.eventStart) {
            $(this).closest(".eventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> Your event end date shouldn't be before your event start date!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
        } else {
            $.ajax({
                url: "/addEvent",
                type: "put",
                data: eventData,
                success: function(response) {
                    location.href = "/portfolio?projectId=" + projectId
                }
            })
        }

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

    setAddSprintButtonPlacement()
    function setAddSprintButtonPlacement() {
        $(".addSprint").css("left", $(".eventContainer").width() + "px")
        $(".addSprint").css("bottom",0 -  $(".addSprintSvg").height()/2 + "px")
    }


    $(".eventEditButton").click(function() {
        let eventId = $(this).closest(".event").find(".eventId").text();
        let eventName = $(this).closest(".event").find(".eventName").text();
        let eventStart = $(this).closest(".event").find(".eventStartDateNilFormat").text().slice(0,16);
        let eventEnd = $(this).closest(".event").find(".eventEndDateNilFormat").text().slice(0,16);
        let typeOfEvent = $(this).closest(".event").find(".typeOfEvent").text()



        $(this).closest(".event").append(`
                <form class="existingEventForm">
                        <div class="mb-1">
                        <label for="eventName" class="form-label">Event name</label>
                        <input type="text" class="form-control form-control-sm eventName" value="`+ eventName +`" maxlength="`+eventNameLengthRestriction+`" name="eventName" required>
                        <small class="form-text text-muted countChar">0 characters remaining</small>
                    </div>
                    <div class="mb-3">
                        <label for="exampleFormControlInput1" class="form-label">Type of event</label>
                        <select class="form-select typeOfEvent" id="exampleFormControlInput1">
                            <option value="1">Event</option>
                            <option value="2">Test</option>
                            <option value="3">Meeting</option>
                            <option value="4">Workshop</option>
                            <option value="5">Special Event</option>
                            <option value="6">Attention Required</option>
                        </select>
                    </div>
                    <div class="row mb-1">
                        <div class="col">
                            <label for="eventStart" class="form-label">Start</label>
                            <input type="datetime-local" class="form-control form-control-sm eventInputStartDate eventStart" value="`+eventStart+`" min="`+projectStart+`" max="`+projectEnd+`" name="eventStart" required>
                        </div>
                        <div class="col">
                            <label for="eventEnd" class="form-label">End</label>
                            <input type="datetime-local" class="form-control form-control-sm eventInputEndDate eventEnd" value="`+eventEnd+`" min="`+projectStart+`" max="`+projectEnd+`" name="eventEnd" required>
                        </div>
                    </div>
                    <div class="mb-1">
                        <button type="button" class="btn btn-primary existingEventSubmit">Save</button>
                        <button type="button" class="btn btn-secondary existingEventCancel" >Cancel</button>
                    </div>
                </form>`)



        $(".existingEventCancel").click(function() {
            $(this).closest(".event").find(".eventEditButton").show();
            $(this).closest(".event").find(".existingEventForm").remove();

        })
        $(".form-control").each(countCharacters)
        $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
        $(this).closest(".event").find(".eventEditButton").hide();
        $(this).closest(".event").find(".existingEventForm").find(".typeOfEvent").val(typeOfEvent)

        $(".existingEventSubmit").click(function() {
            let eventData = {
                "projectId": projectId,
                "eventId" : eventId,
                "eventName": $(this).closest(".existingEventForm").find(".eventName").val(),
                "eventStart": $(this).closest(".existingEventForm").find(".eventStart").val(),
                "eventEnd": $(this).closest(".existingEventForm").find(".eventEnd").val(),
                "typeOfEvent": $(this).closest(".existingEventForm").find(".typeOfEvent").val()
            }
            if (eventData.eventName.toString().length === 0 || eventData.eventName.toString().trim().length === 0){
                $(this).closest(".existingEventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter an event name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            } else if (eventData.eventEnd < eventData.eventStart) {
                $(this).closest(".existingEventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> Your event end date shouldn't be before your event start date!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            } else {
                $.ajax({
                    url: "/editEvent",
                    type: "POST",
                    data: eventData,
                    success: function(response) {
                        location.reload()
                    }
                })
            }
        })

    })

    $(".eventDeleteButton").click(function(){
        let eventData = {"eventId": $(this).closest(".event").find(".eventId").text()}
        $.ajax({
            url: "/deleteEvent",
            type: "DELETE",
            data: eventData,
            success: function(response) {
                location.reload()
            }
        })
    })

    var eventSource = new EventSource("http://localhost:9000/notifications");

    eventSource.addEventListener("editEvent", function (event) {
        const data = JSON.parse(event.data);
        console.log("A user is editing event: " + data.eventId);
    })

    $(".eventEditButton").click(function(){
        let eventId = {"eventId": $(this).closest(".event").find(".eventId").text()};
        $.ajax({
            url: "/eventEdit",
            type: "POST",
            data: eventId
        })
    })

})





