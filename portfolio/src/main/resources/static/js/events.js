$(document).ready(function() {
    //Gets the project Id
    const projectId = $("#projectId").html()

    refreshEvents(projectId)

    /**
     * Slide toggle for when add event button is clicked.
     */
    $(".addEventButton").on('click').click(function() {
        $(".addEventSvg").toggleClass('rotated');
        $(".eventForm").slideToggle();



    })


    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.

    let eventSource = new EventSource("http://localhost:9000/notifications");


    eventSource.addEventListener("editEvent", function (event) {
        const data = JSON.parse(event.data);
        let infoString = data.usersName+ " is editing: " + $("#" + data.eventId).find(".eventName").text() // Find the name of the event from its id
        $("#infoEventContainer").append(`<p class="infoMessage" id="eventNotice`+data.eventId+`"> ` + infoString + `</p>`)
        $("#" + data.eventId).addClass("eventBeingEdited") // Add class that shows which event is being edited
        $("#infoEventContainer").slideDown() // Show the notice.

    })

    eventSource.addEventListener("editEventFinished", function (event) {
        const data = JSON.parse(event.data);
        $("#eventNotice" + data.eventId).remove()
        $("#" + data.eventId).removeClass("eventBeingEdited")

        if (isEmpty($("#infoEventContainer"))) {
            $("#infoEventContainer").slideUp() // Hide the notice.
        }

        refreshEvents(projectId) // Refreshes all the events

    })

})




// <--------------------------- Listener Functions --------------------------->


/**
 * Listens for a click on the event delete button
 */
$(document).on("click", ".eventDeleteButton", function(){
    let eventData = {"eventId": $(this).closest(".event").find(".eventId").text()}
    $.ajax({
        url: "/deleteEvent",
        type: "DELETE",
        data: eventData,
        success: function(response) {
            notifyOfCompletetion(eventData.eventId)
        }
    })
})



/**
 * When new event is submitted.
 */
$(document).on('submit', "#addEventForm", function (event) {
    event.preventDefault();
    const projectId = $("#projectId").html()
    let eventData = {
        "projectId": projectId,
        "eventName": $("#eventName").val(),
        "eventStart": $("#eventStart").val(),
        "eventEnd": $("#eventEnd").val(),
        "typeOfEvent": $(".typeOfEvent").val()
    }
    $.ajax({
        url: "/addEvent",
        type: "put",
        data: eventData,
        success: function(response) {
            const projectId = $("#projectId").html()
            $(".eventForm").slideUp();
            $(".addEventSvg").toggleClass('rotated');
            notifyOfCompletetion(response)
        }
    })

})


/**
 * When existing event is edited and submitted
 */
$(document).on("submit", "#editEventForm", function(event){
    event.preventDefault()
    const projectId = $("#projectId").html()
    let eventId = $(this).parent().find(".eventId").text()
    let eventData = {
        "projectId": projectId,
        "eventId" : eventId,
        "eventName": $(this).find(".eventName").val(),
        "eventStart": $(this).find(".eventStart").val(),
        "eventEnd": $(this).find(".eventEnd").val(),
        "typeOfEvent": $(this).find(".typeOfEvent").val()
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
                notifyOfCompletetion(eventId) // Let the server know the event is no longer being edited
            }
        })
    }
})



/**
 * Listens for a click on the edit event button
 */
$(document).on("click", ".eventEditButton", function() {
    let element = $(this)

    if (!$("#editEventForm").length == 0) { // Checks if an edit event form is already open

        $("#editEventForm").slideUp('400', function(){ // if it is, slide it up
            notifyOfCompletetion($(this).parent().attr('id')) // make a call back to the server letting it know that its no longer being edited (the event)
            $("#editEventForm").remove() // then remove it
            $(".eventEditButton").show()
            appendForm(element); // Then append the form to the new event that we want to edit

        })
    } else {
        appendForm(element) // Else there is no form open so just remove it.
    }





})


/**
 * Listens for a click on the event form cancel button
 */
$(document).on("click", ".existingEventCancel",function() {
    let eventId = $(this).closest(".event").find(".eventId").text();
    notifyOfCompletetion(eventId) // Let the server know the event is no longer being edited
    $(this).closest(".event").find(".eventEditButton").show();
    $(this).closest(".event").find(".existingEventForm").slideUp(400, function () {
        $(this).closest(".event").find(".existingEventForm").remove();
    })


})




// <--------------------------- General Functions --------------------------->


function notifyOfCompletetion(eventId) {
    $.ajax({
        url: "/userFinishedEditing",
        type: "post",
        data: {"eventId": eventId},
    })
}



/**
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 * @param element the element to append the form too.
 */
function appendForm(element){
    let eventId = $(element).closest(".event").find(".eventId").text();
    let eventName = $(element).closest(".event").find(".eventName").text();
    let eventStart = $(element).closest(".event").find(".eventStartDateNilFormat").text().slice(0,16);
    let eventEnd = $(element).closest(".event").find(".eventEndDateNilFormat").text().slice(0,16);
    let typeOfEvent = $(element).closest(".event").find(".typeOfEvent").text()

    $.ajax({
        url: "/eventEdit",
        type: "POST",
        data: {"eventId" :eventId}
    })

    $(element).closest(".event").append(`
                <form class="existingEventForm" id="editEventForm" style="display: none">
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
                        <button type="submit" class="btn btn-primary existingEventSubmit">Save</button>
                        <button type="button" class="btn btn-secondary existingEventCancel" >Cancel</button>
                    </div>
                </form>`)




    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
    $(element).closest(".event").find("#editEventForm").slideDown();
    $(element).closest(".event").find(".eventEditButton").hide();
    $(element).closest(".event").find(".existingEventForm").find(".typeOfEvent").val(typeOfEvent)
}


/**
 * Creates the event divs from the eventObject
 * @param eventObject A Json object with event details
 * @returns {string} A div
 */
function createEventDiv(eventObject) {
    // TODO make it different if user can edit
    let iconElement;
    switch(eventObject.typeOfEvent) {
        case '1':
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Event" th:case="'1'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"><path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>`
            break;
        case '2':
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Test" th:case="'2'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z"/></svg>`
            break;
        case '3':
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Meeting" th:case="'3'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cpu" viewBox="0 0 16 16"><path d="M5 0a.5.5 0 0 1 .5.5V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2A2.5 2.5 0 0 1 14 4.5h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14a2.5 2.5 0 0 1-2.5 2.5v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14A2.5 2.5 0 0 1 2 11.5H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2A2.5 2.5 0 0 1 4.5 2V.5A.5.5 0 0 1 5 0zm-.5 3A1.5 1.5 0 0 0 3 4.5v7A1.5 1.5 0 0 0 4.5 13h7a1.5 1.5 0 0 0 1.5-1.5v-7A1.5 1.5 0 0 0 11.5 3h-7zM5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3zM6.5 6a.5.5 0 0 0-.5.5v3a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-3a.5.5 0 0 0-.5-.5h-3z"/></svg>`
            break;
        case '4':
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Workshop" th:case="'4'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bookmark" viewBox="0 0 16 16"><path d="M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5V2zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1H4z"/></svg>`
            break;
        case '5':
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Special Event" th:case="'5'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell" viewBox="0 0 16 16"><path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/></svg>`
            break;
        case '6':
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Important" th:case="'6'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation" viewBox="0 0 16 16"><path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.553.553 0 0 1-1.1 0L7.1 4.995z"/></svg>`
            break;

    }

    return `
            <div class="event" id="` + eventObject.id + `">
                <p class="eventId" style="display: none">` + eventObject.id + ` </p>
                <p class="eventStartDateNilFormat" style="display: none">` + eventObject.start + ` </p>
                <p class="eventEndDateNilFormat" style="display: none">` + eventObject.end + `</p>
                <p class="typeOfEvent" style="display: none">` + eventObject.typeOfEvent + `</p>
                <div class="mb-2 eventTitleDiv">
                    <div class="eventIcon">
                        ` + iconElement + `
                    </div>
                    <p class="eventName" >` + eventObject.name + `</p>
                </div>
                <button class="eventEditButton noStyleButton"  data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Event">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                        <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                    </svg>
                </button>
                <button type="button" class="eventDeleteButton noStyleButton"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Event">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-x-circle" viewBox="0 0 16 16">
                        <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                        <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                    </svg>
                </button>
                <div class="eventDateDiv">
                    <p class="eventStart">Start Date: ` + eventObject.startFormatted + `</p>
                    <p class="eventEnd">End Date: ` + eventObject.endFormatted + `</p>
                </div>
            </div>`;
}


/**
 * Refreshes the event div section of the page
 * @param projectId
 */
function refreshEvents(projectId){
    $("#eventContainer").find(".event").remove() // Finds all event divs are removes them
    $("#eventContainer").append(`<div id="infoEventContainer" class="infoMessageParent alert alert-primary alert-dismissible fade show" role="alert" style="display: none">
            </div>`) // Adds an info box to the page
    $.ajax({
        url: '/getEventsList',
        type: 'get',
        data: {'projectId': projectId},
        success: function(response) {

            for(let event in response){ // Goes through all the data from the server and creates an eventObject
                let eventObject = {
                    "id" : response[event].id,
                    "name" : response[event].name,
                    "start" : response[event].start,
                    "end" : response[event].end,
                    "startFormatted" : response[event].startFormatted,
                    "endFormatted" : response[event].endFormatted,
                    "typeOfEvent" : response[event].typeOfEvent,
                }

                $("#eventContainer").append(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
            }

        },
        error: function() {
            location.href = "/error" // Moves the user to the error page
        }
    })
}

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


function isEmpty( el ){
    return !$.trim(el.html())
}