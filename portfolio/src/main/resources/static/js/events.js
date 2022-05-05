$(document).ready(function() {

    //Gets the project Id
    const projectId = $("#projectId").html()

    function createEventDiv(eventObject) {
        let eventDiv = `
            <div class="event" style="display: none">
                <p class="eventId" style="display: none"></p>
                <p class="eventStartDateNilFormat" style="display: none"></p>
                <p class="eventEndDateNilFormat" style="display: none"></p>
                <p class="typeOfEvent" style="display: none"></p>
                <div class="mb-2 eventTitleDiv">
                    <p class="eventName" ></p>
                </div>
                <button class="eventEditButton noStyleButton" th:if="${userCanEdit == true}" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Event">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                        <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                    </svg>
                </button>
                <button type="button" class="eventDeleteButton noStyleButton" th:if="${userCanEdit == true}" data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Event">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-x-circle" viewBox="0 0 16 16">
                        <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                        <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                    </svg>
                </button>
                <div class="eventDateDiv">
                    <p class="eventStart"></p>
                    <p class="eventEnd"></p>
                </div>
            </div>`
    }


    function refreshEvents(projectId){
        $.ajax({
            url: '/getEventsList',
            type: 'get',
            data: {'projectId': projectId},
            success: function(response) {
                console.log(response)
               // $("#eventContainer").append(eventDiv)

            },
            error: function() {
                //TODO handle error
            }
        })




    }

    refreshEvents(projectId)

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



    $(".eventEditButton").click(function() {
        let eventId = $(this).closest(".event").find(".eventId").text();
        let eventName = $(this).closest(".event").find(".eventName").text();
        let eventStart = $(this).closest(".event").find(".eventStartDateNilFormat").text().slice(0,16);
        let eventEnd = $(this).closest(".event").find(".eventEndDateNilFormat").text().slice(0,16);
        let typeOfEvent = $(this).closest(".event").find(".typeOfEvent").text()


        $.ajax({
            url: "/userEditingEvent",
            type: "post",
            data: {"eventId": eventId},
            success: function (response){
                console.log(response.statusText)
            },
            error: function (response){
                console.log(response.statusText)
            }
        })



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
            cancelEventBeingEdited(eventId) // Let the server know the event is no longer being edited
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
                        cancelEventBeingEdited(eventId) // Let the server know the event is no longer being edited
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


    function cancelEventBeingEdited(eventId) {
        $.ajax({
            url: "/userFinishedEditing",
            type: "post",
            data: {"eventId": eventId},
            success: function () {
                $("#" + eventId).removeClass("eventBeingEdited")
            }
        })
    }



// Sends request to the server every 5 seconds
    setInterval(function(){
        $.ajax({
            url: '/checkEventChanges',
            type: 'get',
            data: {"projectId": projectId},
            success: function (response) {

                if (response) {  // If Response contains data of events
                    $("#infoEventContainer").empty() //Make sure notice is empty

                    for (let eventId in response){
                        let infoString = response[eventId] + " is editing: " + $("#" + eventId).find(".eventName").text() // Find the name of the event from its id
                        $("#infoEventContainer").append(`<p class="infoMessage"> ` + infoString + `</p>`)
                        $("#" + eventId).addClass("eventBeingEdited") // Add class that shows which event is being edited
                    }

                    $("#infoEventContainer").slideDown() // Show the notice.
                } else { // Response contains no data, no events being edited
                    $("#infoEventContainer").slideUp()
                    $("#infoEventContainer").empty()
                }

            },
            error: function(response){
                console.log("error")
                console.log(response)
            }
        })
    }, 5000);




})