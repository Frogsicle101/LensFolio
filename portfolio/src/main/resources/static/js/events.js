let thisUserIsEditing = false;



$(document).ready(function() {

    refreshEvents(projectId)
    refreshMilestones(projectId)

    removeElementIfNotAuthorized()

    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.

    let eventSource = new EventSource("http://localhost:9000/notifications");


    /**
     * This event listener listens for a notification that an event is being edited
     * It then appends a message to the notice alert showing that the event is being edited and by who.
     * It then adds a class to the event being edited which puts a border around it
     * Then it hides the edit and delete button for that event to prevent the user from editing/deleting it too.
     */
    eventSource.addEventListener("editEvent", function (event) {
        const data = JSON.parse(event.data);
        let infoString = data.usersName+ " is editing: " + $("#" + data.eventId).find(".eventName").text() // Find the name of the event from its id
        $("#infoEventContainer").append(`<p class="infoMessage" id="eventNotice`+data.eventId+`"> ` + infoString + `</p>`)
        $("#" + data.eventId).addClass("eventBeingEdited") // Add class that shows which event is being edited
        if ($(".occasion").hasClass("eventBeingEdited")) {
            $(".eventBeingEdited").find(".eventEditButton").hide()
            $(".eventBeingEdited").find(".eventDeleteButton").hide()
        }
        $("#infoEventContainer").slideDown() // Show the notice.
    })


    eventSource.addEventListener("editMilestone", function (milestone) {
        const data = JSON.parse(milestone.data);
        let infoString = data.usersName+ " is editing: " + $("#" + data.eventId).find(".milestoneName").text() // Find the name of the event from its id

        $("#infoMilestoneContainer").append(`<p class="infoMessage" id="milestoneNotice${data.eventId}" >${infoString}</p>`)

        $("#" + data.eventId).addClass("milestoneBeingEdited") // Add class that shows which event is being edited
        if ($(".occasion").hasClass("milestoneBeingEdited")) {
            $(".milestoneBeingEdited").find(".milestoneEditButton").hide()
            $(".milestoneBeingEdited").find(".milestoneDeleteButton").hide()
        }
        $("#infoMilestoneContainer").slideDown() // Show the notice.
    })











    /**
     * This event listener listens for a notification that an event is no longer being edited
     * It removes the class that shows the border
     * Then it checks if this current user is editing another event, and avoids showing the edit buttons
     * If the user isn't currently editing an event then it redisplays the edit and delete button.
     */
    eventSource.addEventListener("userNotEditingEvent", function (event) {
        const data = JSON.parse(event.data);
        $("#eventNotice" + data.eventId).remove()
        $("#" + data.eventId).removeClass("eventBeingEdited")
        if (!thisUserIsEditing) {
            $("#" + data.eventId).find(".eventEditButton").show();
            $("#" + data.eventId).find(".eventDeleteButton").show();
        }
        if ($(".occasion").hasClass("eventBeingEdited")) {
            $(".eventBeingEdited").find(".eventEditButton").hide()
            $(".eventBeingEdited").find(".eventDeleteButton").hide()
        }
        if (isEmpty($("#infoEventContainer"))) {
            $("#infoEventContainer").slideUp() // Hide the notice.
        }
    })

    eventSource.addEventListener("userNotEditingMilestone", function (milestone) {

        const data = JSON.parse(milestone.data);
        $("#milestoneNotice" + data.eventId).remove()
        $("#" + data.eventId).removeClass("milestoneBeingEdited")
        if (!thisUserIsEditing) {
            $("#" + data.eventId).find(".milestoneEditButton").show();
            $("#" + data.eventId).find(".milestoneDeleteButton").show();
        }
        if ($(".occasion").hasClass("milestoneBeingEdited")) {
            $(".milestoneBeingEdited").find(".milestoneEditButton").hide()
            $(".milestoneBeingEdited").find(".milestoneDeleteButton").hide()
        }
        if (isEmpty($("#infoMilestoneContainer"))) {
            $("#infoMilestoneContainer").slideUp() // Hide the notice.
        }
    })





    /**
     * This event listener listens for a notification that an event should be reloaded.
     * This happens if another user has changed an event.
     * It removes the class that shows the border and then calls ReloadEvent()
     */
    eventSource.addEventListener("reloadSpecificEvent", function (event) {
        const data = JSON.parse(event.data);
        $("#eventNotice" + data.eventId).remove()
        $("#" + data.eventId).removeClass("eventBeingEdited")
        if (isEmpty($("#infoEventContainer"))) {
            $("#infoEventContainer").slideUp() // Hide the notice.
        }
        reloadEvent(data.eventId) // reloads specific event
    })
    /**
     * Listens for a notification to remove an event (happens if another client deletes an event)
     */
    eventSource.addEventListener("notifyRemoveEvent", function (event) {
        const data = JSON.parse(event.data);
        removeEvent(data.eventId) // reloads specific event
    })
    /**
     * Listens for a notification to add a new event that another client has created
     */
    eventSource.addEventListener("notifyNewEvent", function (event) {
        const data = JSON.parse(event.data);
        addEvent(data.eventId) // reloads specific event
    })






})




// <--------------------------- Listener Functions --------------------------->

/**
 * Listens for when add event button is clicked.
 */
$(document).on('click', '.addEventButton', function() {

    $(".addEventSvg").toggleClass('rotated');
    $(".eventForm").slideToggle();
})


/**
 * Listens for a click on the event delete button
 */
$(document).on("click", ".eventDeleteButton", function(){
    let eventData = {"eventId": $(this).closest(".occasion").find(".eventId").text()}
    $.ajax({
        url: "deleteEvent",
        type: "DELETE",
        data: eventData,
        success: function(response) {
            notifyToDelete(eventData.eventId)
        }
    })
})



/**
 * When new event is submitted.
 */
$(document).on('submit', "#addEventForm", function (event) {
    event.preventDefault()
    let eventData = {
        "projectId": projectId,
        "eventName": $("#eventName").val(),
        "eventStart": $("#eventStart").val(),
        "eventEnd": $("#eventEnd").val(),
        "typeOfEvent": $(".typeOfEvent").val()
    }
    $.ajax({
        url: "addEvent",
        type: "put",
        data: eventData,
        success: function(response) {

            $(".eventForm").slideUp();
            $(".addEventSvg").toggleClass('rotated');
            notifyNewEvent(response)
        }
    })

})


/**
 * When existing event is edited and submitted
 */
$(document).on("submit", "#editEventForm", function(event){
    event.preventDefault()

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
            url: "editEvent",
            type: "POST",
            data: eventData,
            success: function(response) {
                notifyToReloadEvent(eventId) // Let the server know the event is no longer being edited
            }
        })
    }
})



/**
 * Listens for a click on the edit event button
 */
$(document).on("click", ".eventEditButton", function() {
    thisUserIsEditing = true;
    $(".addEventButton").hide()
    $(".eventEditButton").hide()
    $(".eventDeleteButton").hide()
    let element = $(this).parent()
    appendEventForm(element)

})


/**
 * Listens for a click on the event form cancel button
 */
$(document).on("click", ".existingEventCancel",function() {
    let eventId = $(this).closest(".occasion").find(".eventId").text();
    thisUserIsEditing = false;
    $(".addEventButton").show()
    $(".eventEditButton").show()
    $(".eventDeleteButton").show()
    notifyNotEditing(eventId) // Let the server know the event is no longer being edited
    $(this).closest(".occasion").find(".existingEventForm").slideUp(400, function () {
        $(this).closest(".occasion").find(".existingEventForm").remove();
    })


})




// <--------------------------- General Functions --------------------------->


function removeElementIfNotAuthorized() {
    if (!checkPrivilege()) {
        $(".hasTeacherOrAbove").remove()
    }
}
/**
 * Checks if a user has a role above student.
 * @returns {boolean} returns true if userRole is above student.
 */
function checkPrivilege(){
    return userRoles.includes('COURSE_ADMINISTRATOR') || userRoles.includes('TEACHER');
}

/**
 * Sends notification to server to notify other clients that this user is no longer editing an event.
 * @param eventId the id of the event
 */
function notifyNotEditing(eventId) {
    $.ajax({
        url: "userNotEditingEvent",
        type: "post",
        data: {"eventId": eventId},
    })
}


function notifyNotEditingMilestone(milestoneId) {
    $.ajax({
        url: "userNotEditingMilestone",
        type: "post",
        data: {"milestoneId": milestoneId},
    })
}



/**
 * Sends notification to server to notify other clients to reload a specific event.
 * @param eventId the id of the event
 */
function notifyToReloadEvent(eventId) {
    $.ajax({
        url: "reloadSpecificEvent",
        type: "post",
        data: {"eventId": eventId},
    })
}
/**
 * Sends notification to server to notify other clients to delete a specific event
 * @param eventId the id of the event
 */
function notifyToDelete(eventId) {
    $.ajax({
        url: "notifyRemoveEvent",
        type: "post",
        data: {"eventId": eventId},
    })
}

/**
 * Sends notification to server to notify other clients that this user has added an event.
 * @param eventId the id of the event
 */
function notifyNewEvent(eventId) {
    $.ajax({
        url: "notifyNewEvent",
        type: "post",
        data: {"eventId": eventId},
    })
}


function notifyEditEvent(eventId) {
    $.ajax({
        url: "eventEdit",
        type: "POST",
        data: {"eventId" :eventId}
    })
}

function notifyEditMilestone(milestoneId) {
    $.ajax({
        url: "milestoneBeingEdited",
        type: "POST",
        data: {"milestoneId" :milestoneId}
    })
}




/**
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 * @param element the element to append the form too.
 */
function appendEventForm(element){
    let eventId = $(element).find(".eventId").text();
    let eventName = $(element).find(".eventName").text();
    let eventStart = $(element).find(".eventStartDateNilFormat").text().slice(0,16);
    let eventEnd = $(element).find(".eventEndDateNilFormat").text().slice(0,16);
    let typeOfEvent = $(element).find(".typeOfEvent").text()

    notifyEditEvent(eventId)

    $(element).append(`
                <form class="existingEventForm" id="editEventForm" style="display: none">
                        <div class="mb-1">
                        <label for="eventName" class="form-label">Event name</label>
                        <input type="text" class="form-control form-control-sm eventName" pattern="${titleRegex}" value="${eventName}" maxlength="${eventNameLengthRestriction}" name="eventName" required>
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
                            <input type="datetime-local" class="form-control form-control-sm eventInputStartDate eventStart" value="${eventStart}" min="${projectStart}" max="${projectEnd}" name="eventStart" required>
                        </div>
                        <div class="col">
                            <label for="eventEnd" class="form-label">End</label>
                            <input type="datetime-local" class="form-control form-control-sm eventInputEndDate eventEnd" value="${eventEnd}" min="${projectStart}" max="${projectEnd}" name="eventEnd" required>
                        </div>
                    </div>
                    <div class="mb-1">
                        <button type="submit" class="btn btn-primary existingEventSubmit">Save</button>
                        <button type="button" class="btn btn-secondary existingEventCancel" >Cancel</button>
                    </div>
                </form>`)




    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
    $("#editEventForm").slideDown();
    $(element).closest(".occasion").find(".eventEditButton").hide();
    $(element).closest(".occasion").find(".existingEventForm").find(".typeOfEvent").val(typeOfEvent)
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
        case 1:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Event" th:case="'1'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"><path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>`
            break;
        case 2:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Test" th:case="'2'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z"/></svg>`
            break;
        case 3:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Meeting" th:case="'3'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cpu" viewBox="0 0 16 16"><path d="M5 0a.5.5 0 0 1 .5.5V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2A2.5 2.5 0 0 1 14 4.5h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14a2.5 2.5 0 0 1-2.5 2.5v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14A2.5 2.5 0 0 1 2 11.5H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2A2.5 2.5 0 0 1 4.5 2V.5A.5.5 0 0 1 5 0zm-.5 3A1.5 1.5 0 0 0 3 4.5v7A1.5 1.5 0 0 0 4.5 13h7a1.5 1.5 0 0 0 1.5-1.5v-7A1.5 1.5 0 0 0 11.5 3h-7zM5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3zM6.5 6a.5.5 0 0 0-.5.5v3a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-3a.5.5 0 0 0-.5-.5h-3z"/></svg>`
            break;
        case 4:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Workshop" th:case="'4'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bookmark" viewBox="0 0 16 16"><path d="M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5V2zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1H4z"/></svg>`
            break;
        case 5:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Special Event" th:case="'5'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell" viewBox="0 0 16 16"><path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/></svg>`
            break;
        case 6:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Important" th:case="'6'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation" viewBox="0 0 16 16"><path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.553.553 0 0 1-1.1 0L7.1 4.995z"/></svg>`
            break;

    }

    return `
            <div class="occasion" id="${eventObject.id}">
                <p class="eventId" style="display: none">` + eventObject.id + ` </p>
                <p class="eventStartDateNilFormat" style="display: none">${eventObject.start}</p>
                <p class="eventEndDateNilFormat" style="display: none">${eventObject.end}</p>
                <p class="typeOfEvent" style="display: none">${eventObject.typeOfEvent}</p>
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="eventName" >${eventObject.name}</p>
                </div>
                <button class="eventEditButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Event">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                        <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                        <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                    </svg>
                </button>
                <button type="button" class="eventDeleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Event">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-x-circle" viewBox="0 0 16 16">
                        <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                        <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                    </svg>
                </button>
                <div class="eventDateDiv">
                    <p class="eventStart">Start Date: ${eventObject.startFormatted}</p>
                    <p class="eventEnd">End Date: ${eventObject.endFormatted}</p>
                </div>
            </div>`;
}


/**
 * Refreshes the event div section of the page
 * @param projectId
 */
function refreshEvents(){
    $("#eventContainer").find(".occasion").remove() // Finds all event divs are removes them
    $("#eventContainer").append(`<div id="infoEventContainer" class="infoMessageParent alert alert-primary alert-dismissible fade show" role="alert" style="display: none">
            </div>`) // Adds an info box to the page
    $.ajax({
        url: 'getEventsList',
        type: 'get',
        data: {'projectId': projectId},

        success: function(response) {

            for(let event in response){ // Goes through all the data from the server and creates an eventObject
                let eventObject = {
                    "id" : response[event].id,
                    "name" : response[event].name,
                    "start" : response[event].startDate,
                    "end" : response[event].dateTime,
                    "startFormatted" : response[event].startDateFormatted,
                    "endFormatted" : response[event].endDateFormatted,
                    "typeOfEvent" : response[event].type,
                }

                $("#eventContainer").append(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
                removeElementIfNotAuthorized()
            }

        },
        error: function(error) {
            console.log(error)
            // location.href = "/error" // Moves the user to the error page
        }
    })


}

function reloadEvent(eventId){
    $("#eventContainer").find("#" + eventId).slideUp() // Finds the event divs and hides them


    $.ajax({
        url: 'getEvent',
        type: 'get',
        data: {'eventId': eventId},
        success: function(response) {

            let eventObject = {
                "id" : response.id,
                "name" : response.name,
                "start" : response.startDate,
                "end" : response.dateTime,
                "startFormatted" : response.startDateFormatted,
                "endFormatted" : response.endDateFormatted,
                "typeOfEvent" : response.type,
            }

            $("#" + eventId).replaceWith(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
            $("#" + eventId).slideDown()
            $(".eventEditButton").show()
            $(".eventDeleteButton").show()
            removeElementIfNotAuthorized()

        },
        error: function() {
            location.href = "/error" // Moves the user to the error page
        }
    })


}


/**
 * Removes specific event
 * @param eventId id of event to remove
 */

function removeEvent(eventId) {
    $("#eventContainer").find("#" + eventId).slideUp() // Finds all event divs are removes them
    $("#eventContainer").find("#" + eventId).remove()
}

/**
 * Gets the details of the event and adds it to the page.
 * @param eventId the event to add.
 */
function addEvent(eventId) {
    $.ajax({
        url: 'getEvent',
        type: 'get',
        data: {'eventId': eventId},
        success: function(response) {
            let eventObject = {
                "id" : response.id,
                "name" : response.name,
                "start" : response.startDate,
                "end" : response.endDate,
                "startFormatted" : response.startDateFormatted,
                "endFormatted" : response.endDateFormatted,
                "typeOfEvent" : response.typeOfEvent,
            }

            $("#eventContainer").append(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
            removeElementIfNotAuthorized()

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

/**
 * Checks if element is empty
 * @param el the element to check
 * @returns {boolean} true if empty, false if not
 */
function isEmpty( el ){
    return !$.trim(el.html())
}
























/**
 * Slide toggle for when add milestone button is clicked.
 */
$(".addMilestoneButton").click(function() {
    $(".addMilestoneSvg").toggleClass('rotated');
    $(".milestoneForm").slideToggle();
})


/**
 * When edited milestone is submitted
 */
$(document).on("submit", "#milestoneEditForm", function(event){
    event.preventDefault();

    let milestoneId = $(this).parent().find(".milestoneId").text()
    let milestoneData = {
        "projectId": projectId,
        "milestoneId": milestoneId,
        "milestoneName": $("#milestoneName").val(),
        "milestoneDate": $("#milestoneEnd").val(),
        "typeOfMilestone": $(".typeOfMilestone").val()
    }

    if (milestoneData.milestoneName.toString().length === 0 || milestoneData.milestoneName.toString().trim().length === 0) {
        $(this).closest(".milestoneForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter a milestone name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
    } else {
        $.ajax({
            url: "/editMilestone",
            type: "POST",
            data: milestoneData,
            success: function(response) {
                // TODO notify to reload milestone
            }
        })
    }

})






//////////////////////////////// milestones ///////////////////////////////


/**
 * Refreshes the milestone div section of the page
 * @param projectId
 */
function refreshMilestones(projectId){
    $("#milestoneContainer").find(".occasion").remove() // Finds all event divs are removes them
    $("#milestoneContainer").append(`<div id="infoMilestoneContainer" class="infoMessageParent alert alert-primary alert-dismissible fade show" role="alert" style="display: none">
            </div>`) // Adds an info box to the page
    $.ajax({
        url: '/getMilestoneList',
        type: 'get',
        data: {'projectId': projectId},

        success: function(response) {
            console.log(response)
            for(let milestone in response){ // Goes through all the data from the server and creates an eventObject
                let milestoneObject = {
                    "id" : response[milestone].id,
                    "name" : response[milestone].name,
                    "end" : response[milestone].dateTime,
                    "endFormatted" : response[milestone].endDateFormatted,
                    "type" : response[milestone].type,
                }

                $("#milestoneContainer").append(createMilstoneDiv(milestoneObject)) // Passes the eventObject to the createDiv function
                removeElementIfNotAuthorized()
            }

        },
        error: function(error) {
            console.log(error)
            // location.href = "/error" // Moves the user to the error page
        }
    })


}


/**
 * Creates the Milestone divs from the milestoneObject
 * @param milestoneObject A Json object with event details
 * @returns {string} A div
 */
function createMilstoneDiv(milestoneObject) {
    // TODO make it different if user can edit
    let iconElement;
    switch(milestoneObject.type) {
        case 1:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Event" th:case="'1'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"><path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>`
            break;
        case 2:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Test" th:case="'2'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z"/></svg>`
            break;
        case 3:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Meeting" th:case="'3'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cpu" viewBox="0 0 16 16"><path d="M5 0a.5.5 0 0 1 .5.5V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2A2.5 2.5 0 0 1 14 4.5h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14a2.5 2.5 0 0 1-2.5 2.5v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14A2.5 2.5 0 0 1 2 11.5H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2A2.5 2.5 0 0 1 4.5 2V.5A.5.5 0 0 1 5 0zm-.5 3A1.5 1.5 0 0 0 3 4.5v7A1.5 1.5 0 0 0 4.5 13h7a1.5 1.5 0 0 0 1.5-1.5v-7A1.5 1.5 0 0 0 11.5 3h-7zM5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3zM6.5 6a.5.5 0 0 0-.5.5v3a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-3a.5.5 0 0 0-.5-.5h-3z"/></svg>`
            break;
        case 4:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Workshop" th:case="'4'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bookmark" viewBox="0 0 16 16"><path d="M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5V2zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1H4z"/></svg>`
            break;
        case 5:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Special Event" th:case="'5'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell" viewBox="0 0 16 16"><path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/></svg>`
            break;
        case 6:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Important" th:case="'6'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation" viewBox="0 0 16 16"><path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.553.553 0 0 1-1.1 0L7.1 4.995z"/></svg>`
            break;

    }

    return `
            <div class="occasion" id="${milestoneObject.id}">
                <p class="milestoneId" style="display: none">${milestoneObject.id}</p>
                <p class="milestoneEndDateNilFormat" style="display: none">${milestoneObject.endDate}</p>
                <p class="typeOfMilestone" style="display: none">${milestoneObject.type}</p>
                
                
                
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="milestoneName">${milestoneObject.name}</p>
                </div>
                
                <button class="milestoneEditButton noStyleButton hasTeacherOrAbove" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Milestone">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                                 class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                                <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                            </svg>
                        </button>
                        <button type="button" class="milestoneDeleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Milestone">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                                 class="bi bi-x-circle" viewBox="0 0 16 16">
                                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                                <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                            </svg>
                        </button>
                        <div class="milestoneDateDiv">
                            <p class="milestoneEnd">${milestoneObject.endFormatted}</p>
                        </div>
            </div>`;
}

/**
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 * @param element the element to append the form too.
 */
function appendMilestoneForm(element){
    let milestoneId = $(element).find(".milestoneId").text();
    let milestoneName = $(element).find(".milestoneName").text();
    let milestoneEnd = $(element).find(".milestoneEnd").text().slice(0,16);
    let typeOfEvent = $(element).find(".typeOfMilestone").text()

    notifyEditMilestone(milestoneId)

    $(element).append(`
                <form class="existingMilestoneForm" id="milestoneEditForm" style="display: none">
                        <div class="mb-1">
                        <label for="milestoneName" class="form-label">Milestone name</label>
                        <input type="text" class="form-control form-control-sm milestoneName" value="${milestoneName}" maxlength="${eventNameLengthRestriction}" name="milestoneName" required>
                        <small class="form-text text-muted countChar">0 characters remaining</small>
                    </div>
                    <div class="mb-3">
                        <label for="exampleFormControlInput2" class="form-label">Type of milestone</label>
                        <select class="form-select typeOfMilestone" id="exampleFormControlInput2">
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
                            <label for="milestoneEnd" class="form-label">End</label>
                            <input type="date" class="form-control form-control-sm milestoneInputEndDate milestoneEnd" value="${milestoneEnd}" min="${projectStart}" max="${projectEnd}" name="milestoneEnd" required>
                        </div>
                    </div>
                    <div class="mb-1">
                        <button type="submit" class="btn btn-primary existingMilestoneSubmit">Save</button>
                        <button type="button" class="btn btn-secondary existingMilestoneCancel" >Cancel</button>
                    </div>
                </form>`)




    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
    $("#milestoneEditForm").slideDown();
}


$(document).on("click", '.existingMilestoneCancel', function(){
    let milestoneId = $(this).closest(".occasion").find(".milestoneId").text();
    //TODO thisUserIsEditing = false;
    // TODO $(".addEventButton").show()
    $(".milestoneEditButton").show()
    $(".milestoneDeleteButton").show()
    notifyNotEditingMilestone(milestoneId) // Let the server know the event is no longer being edited
    $(this).closest(".occasion").find(".existingMilestoneForm").slideUp(400, function () {
        $(this).closest(".occasion").find(".existingMilestoneForm").remove();
    })
})





$(document).on("click", ".milestoneEditButton", function() {
    thisUserIsEditing = true;
    // $(".addEventButton").hide()
    $(".milestoneEditButton").hide()
    $(".milestoneDeleteButton").hide()
    let element = $(this).parent()
    appendMilestoneForm(element)
})


$(document).on('click', ".milestoneDeleteButton", function() {
    $(".milestoneDeleteButton").click(function(){
        let milestoneData = {"milestoneId": $(this).closest(".occasion").find(".milestoneId").text()}
        $.ajax({
            url: "/deleteMilestone",
            type: "DELETE",
            data: milestoneData,
            success: function(response) {
                location.reload() // TODO dynamically do this
            }
        })
    })
})


//////////////////////////////// Deadlines ///////////////////////////////
/*      DEADLINE GENERAL FUNCTIONS      */

/**
 * Sends notification to server to notify other clients that this user has added an event.
 * @param deadlineId the id of the deadline
 */
function notifyNewDeadline(deadlineId) {
    $.ajax({
        url: "notifyNewDeadline",
        type: "post",
        data: {"deadlineId": deadlineId},
    })
}


/**
 * Sends notification to server to notify other clients that this user is no longer editing a deadline.
 * @param deadlineId the id of the deadline
 */
function notifyNotEditingDeadline(deadlineId) {
    $.ajax({
        url: "userNotEditingDeadline",
        type: "post",
        data: {"deadlineId": deadlineId},
    })
}

/*      DEADLINE EVENT/EVENTLISTENER FUNCTIONS        */

/**
 * When new deadline is submitted.
 */
$(document).on('submit', "#addDeadlineForm", function (deadline) {
    deadline.preventDefault()
    let deadlineData = {
        "projectId": projectId,
        "deadlineName": $("#deadlineName").val(),
        "deadlineStart": $("#deadlineStart").val(),
        "deadlineEnd": $("#deadlineEnd").val(),
        "typeOfDeadline": $(".typeOfDeadline").val()
    }
    //Ajax call to store the deadline data
    $.ajax({
        url: "addDeadline",
        type: "put",
        data: deadlineData,
        success: function(response) {

            $(".deadlineForm").slideUp();
            $(".addDeadlineSvg").toggleClass('rotated');
            notifyNewDeadline(response)
        }
    })

})

/**
 * Slide toggle for when add deadline button is clicked.
 */
$(".addDeadlineButton").click(function() {
    $(".addDeadlineSvg").toggleClass('rotated');
    $(".deadlineForm").slideToggle();
})

/*      DEADLINE REGULAR/MAIN/MAIN COURSE (SO TO SPEAK) FUNCTIONS        */

/**
 * Refreshes the deadline div section of the page
 * @param projectId
 */
function refreshDeadline(projectId){
    $("#deadlineContainer").find(".occasion").remove() // Finds all event divs are removes them
    $("#deadlineContainer").append(`<div id="infoDeadlineContainer" class="infoMessageParent alert alert-primary alert-dismissible fade show" role="alert" style="display: none">
            </div>`) // Adds an info box to the page
    $.ajax({
        url: '/getDeadlineList',
        type: 'get',
        data: {'projectId': projectId},

        success: function(response) {
            console.log(response)
            for(let deadline in response){ // Goes through all the data from the server and creates an eventObject
                let deadlineObject = {
                    "id" : response[deadline].id,
                    "name" : response[deadline].name,
                    "end" : response[deadline].dateTime,
                    "endFormatted" : response[deadline].endDateFormatted,
                    "type" : response[deadline].type,
                }

                $("#deadlineContainer").append(createDeadlineDiv(deadlineObject)) // Passes the deadlineObject to the createDiv function
                removeElementIfNotAuthorized()
            }

        },
        error: function(error) {
            console.log(error)
            // location.href = "/error" // Moves the user to the error page
        }
    })


}

/**
 * Creates the Deadline divs from the deadlineObject
 * @param deadlineObject A Json object with deadline details
 * @returns {string} A div
 */
function createDeadlineDiv(deadlineObject) {
    // TODO make it different if user can edit
    let iconElement;
    switch(deadlineObject.type) {
        case 1:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Event" th:case="'1'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar-event" viewBox="0 0 16 16"><path d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z"/><path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/></svg>`
            break;
        case 2:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Test" th:case="'2'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-pencil-square" viewBox="0 0 16 16"><path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z"/><path fill-rule="evenodd" d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5v11z"/></svg>`
            break;
        case 3:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Meeting" th:case="'3'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cpu" viewBox="0 0 16 16"><path d="M5 0a.5.5 0 0 1 .5.5V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2h1V.5a.5.5 0 0 1 1 0V2A2.5 2.5 0 0 1 14 4.5h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14v1h1.5a.5.5 0 0 1 0 1H14a2.5 2.5 0 0 1-2.5 2.5v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14h-1v1.5a.5.5 0 0 1-1 0V14A2.5 2.5 0 0 1 2 11.5H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2v-1H.5a.5.5 0 0 1 0-1H2A2.5 2.5 0 0 1 4.5 2V.5A.5.5 0 0 1 5 0zm-.5 3A1.5 1.5 0 0 0 3 4.5v7A1.5 1.5 0 0 0 4.5 13h7a1.5 1.5 0 0 0 1.5-1.5v-7A1.5 1.5 0 0 0 11.5 3h-7zM5 6.5A1.5 1.5 0 0 1 6.5 5h3A1.5 1.5 0 0 1 11 6.5v3A1.5 1.5 0 0 1 9.5 11h-3A1.5 1.5 0 0 1 5 9.5v-3zM6.5 6a.5.5 0 0 0-.5.5v3a.5.5 0 0 0 .5.5h3a.5.5 0 0 0 .5-.5v-3a.5.5 0 0 0-.5-.5h-3z"/></svg>`
            break;
        case 4:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Workshop" th:case="'4'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bookmark" viewBox="0 0 16 16"><path d="M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5V2zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1H4z"/></svg>`
            break;
        case 5:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Special Event" th:case="'5'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-bell" viewBox="0 0 16 16"><path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"/></svg>`
            break;
        case 6:
            iconElement = `<svg data-bs-toggle="tooltip" data-bs-placement="top" title="Important" th:case="'6'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-exclamation" viewBox="0 0 16 16"><path d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.553.553 0 0 1-1.1 0L7.1 4.995z"/></svg>`
            break;

    }

    return `
            <div class="occasion" id="${deadlineObject.id}">
                <p class="deadlineId" style="display: none">${deadlineObject.id}</p>
                <p class="deadlineEndDateNilFormat" style="display: none">${deadlineObject.endDate}</p>
                <p class="typeOfDeadline" style="display: none">${deadlineObject.type}</p>
                
                
                
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="deadlineName">${deadlineObject.name}</p>
                </div>
                
                <button class="deadlineEditButton noStyleButton hasTeacherOrAbove" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Deadline">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                                 class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                                <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                            </svg>
                        </button>
                        <button type="button" class="deadlineDeleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Deadline">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                                 class="bi bi-x-circle" viewBox="0 0 16 16">
                                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                                <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                            </svg>
                        </button>
                        <div class="deadlineDateDiv">
                            <p class="deadlineEnd">${milestoneObject.endFormatted}</p>
                        </div>
            </div>`;
}
