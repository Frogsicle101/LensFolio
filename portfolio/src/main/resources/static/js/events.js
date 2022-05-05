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





    var eventSource = new EventSource("http://localhost:9000/notifications");

    eventSource.addEventListener("editEvent", function (event) {
        const data = JSON.parse(event.data);
        console.log("A user is editing event: " + data.eventId);
    })

    eventSource.addEventListener("editEventFinished", function (event) {
        const data = JSON.parse(event.data);
        console.log("A user is no longer editing event: " + data.eventId);
    })

})


function checkEventChanges(projectId){
    $.ajax({
        url: '/checkEventChanges',
        type: 'get',
        data: {"projectId": projectId},
        success: function (response) {

            if (response) {  // If Response contains data of events
                $("#infoEventContainer").empty() //Make sure notice is empty

                for (let eventId in response){
                    console.log(eventId)
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
}


$(document).on("click", ".eventDeleteButton", function(){
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



/**
 * When event is submitted.
 */
$(document).on('submit', "#eventSubmit", function (event) {
    event.preventDefault();
    let eventData = {
        "projectId": projectId,
        "eventName": $("#eventName").val(),
        "eventStart": $("#eventStart").val(),
        "eventEnd": $("#eventEnd").val(),
        "typeOfEvent": $(".typeOfEvent").val()
    }
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



$(document).on("click", ".eventEditButton", function() {

    let eventId = $(this).closest(".event").find(".eventId").text();
    console.log(eventId)
    let eventName = $(this).closest(".event").find(".eventName").text();
    let eventStart = $(this).closest(".event").find(".eventStartDateNilFormat").text().slice(0,16);
    let eventEnd = $(this).closest(".event").find(".eventEndDateNilFormat").text().slice(0,16);
    let typeOfEvent = $(this).closest(".event").find(".typeOfEvent").text()


    $.ajax({
        url: "/eventEdit",
        type: "POST",
        data: {"eventId" :eventId}
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




function refreshEvents(projectId){
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
                //TODO format them so they appear on page in order of dates
            }

        },
        error: function() {
            //TODO handle error
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