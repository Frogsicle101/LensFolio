let thisUserIsEditing = false;




$(document).ready(function() {


    let infoContainer = $("#informationBar")
    let beingEdited = $(".beingEdited");
    let formControl = $(".form-control");







    refreshEvents(projectId)

    refreshMilestones(projectId)

    removeElementIfNotAuthorized()

    formControl.each(countCharacters)
    formControl.keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.


// -------------------------------------- Notification Source and listeners --------------------------------------------

    /** The source of notifications used to provide updates to the user such as events being edited */
    let eventSource = new EventSource("notifications");


    /**
     * This event listener listens for a notification that an event is being edited
     * It then appends a message to the notice alert showing that the event is being edited and by who.
     * It then adds a class to the event being edited which puts a border around it
     * Then it hides the edit and delete button for that event to prevent the user from editing/deleting it too.
     */
    eventSource.addEventListener("editEvent", function (event) {
        const data = JSON.parse(event.data);
        if (checkPrivilege()) {
            let eventDiv = $("#" + data.eventId)

            let infoString = data.usersName+ " is editing element: " + eventDiv.find(".name").text() // Find the name of the event from its id
            infoContainer.append(`<p class="infoMessage" id="notice`+data.eventId+`"> ` + infoString + `</p>`)
            eventDiv.addClass("beingEdited") // Add class that shows which event is being edited
            if (eventDiv.hasClass("beingEdited")) {
                eventDiv.find(".controlButtons").hide()

            }
            infoContainer.slideDown() // Show the notice.
        }

    })



    /**
     * This event listener listens for a notification that an element is no longer being edited
     * It removes the class that shows the border
     * Then it checks if this current user is editing another element, and avoids showing the edit buttons
     * If the user isn't currently editing an element then it redisplays the edit and delete button.
     */
    eventSource.addEventListener("notifyNotEditing", function (event) {
        const data = JSON.parse(event.data);
        let elementDiv = $("#" + data.eventId)

        if (checkPrivilege()) {
            $("#notice" + data.eventId).remove()
            elementDiv.removeClass("beingEdited")
            if (!thisUserIsEditing) {
                elementDiv.find(".controlButtons").show()
            }
            if (elementDiv.hasClass("beingEdited")) {
                elementDiv.find(".controlButtons").hide()
            }
            if (isEmpty(infoContainer)) {
                infoContainer.slideUp() // Hide the notice.
            }
        }

    })


    /**
     * This event listener listens for a notification that an element should be reloaded.
     * This happens if another user has changed an element.
     * It removes the class that shows the border and then calls ReloadEvent()
     */
    eventSource.addEventListener("reloadElement", function (event) {
        const data = JSON.parse(event.data);
        $("#notice" + data.eventId).remove()
        $("#" + data.eventId).removeClass("beingEdited")
        if (isEmpty(infoContainer)) {
            infoContainer.slideUp() // Hide the notice.
        }

        reloadElement(data.eventId) // reloads specific element

    })


    /**
     * Listens for a notification to remove an element (happens if another client deletes an element)
     */
    eventSource.addEventListener("notifyRemoveEvent", function (event) {
        const data = JSON.parse(event.data);
        removeElement(data.eventId) // removes specific event
    })


    /**
     * Listens for a notification to add a new element that another client has created
     */
    eventSource.addEventListener("notifyNewElement", function (event) {
        const data = JSON.parse(event.data);
        if (data.typeOfEvent === "event"){
            addEvent(data.eventId)
        } else if (data.typeOfEvent === "milestone") {
            addMilestone(data.eventId)
        }
        //TODO add deadlines

    })

})


function notifyEdit(id, type, typeOfEvent = null) {
    $.ajax({
        url: "notifyEdit",
        type: "POST",
        data: {id, type, typeOfEvent}
    })
}


// ---------------------------------------------------------------------------------------------------------------------


function sortElementsByDate(div, childrenElement, dateElement) {

    let result = $(div).children(childrenElement).sort(function (a, b) {

        let contentA = Date.parse( $(a).find(dateElement).text());
        let contentB = Date.parse( $(b).find(dateElement).text());
        return (contentA < contentB) ? -1 : (contentA > contentB) ? 1 : 0;
    });

    $(div).html(result);
}


// <--------------------------- Listener Functions --------------------------->


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


    if (eventData.eventEnd < eventData.eventStart) {
        $(this).closest("#addEventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> Your event end date shouldn't be before your event start date!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
    } else {
        $.ajax({
            url: "addEvent",
            type: "put",
            data: eventData,
            success: function(response) {

                $(".eventForm").slideUp();
                $(".addEventSvg").toggleClass('rotated');

                notifyEdit(response.id, "notifyNewElement", "event")
            }
        })
    }
})


/**
 * When new milestone is submitted
 */
$(document).on("submit", ".milestoneForm", function(event){
    event.preventDefault()
    let milestoneData = {
        "milestoneName" : $("#milestoneName").val(),
        "milestoneEnd" : $("#milestoneEnd").val(),
        "typeOfOccasion" : $(".typeOfMilestone").val(),
        "projectId" : projectId
    }
    $.ajax({
        url: "addMilestone",
        type: "PUT",
        data: milestoneData,
        success: function(response) {
            $(".milestoneForm").slideUp()
            $(".addEventSvg").toggleClass('rotated');
            notifyEdit(response.id, "notifyNewElement", "milestone")
        }
    })
})




//TODO submit for deadline form








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
                notifyEdit(eventId, "reloadElement") // Let the server know the event is no longer being edited
            }
        })
    }
})



/**
 * When edited milestone is submitted
 */
$(document).on("submit", "#milestoneEditForm", function(event){
    event.preventDefault();

    //TODO add in date checks
    let milestoneId = $(this).parent().find(".milestoneId").text()
    let milestoneData = {
        "projectId": projectId,
        "milestoneId": milestoneId,
        "milestoneName": $(this).find(".milestoneName").val(),
        "milestoneDate": $(this).find(".milestoneEnd").val(),
        "typeOfMilestone": $(this).find(".typeOfMilestone").val()
    }


    $.ajax({
        url: "editMilestone",
        type: "POST",
        data: milestoneData,
        success: function(response) {
            notifyEdit(milestoneId, "reloadElement")
        }
    })


})


//TODO add deadlines listener for deadline edit form submit




/**
 * Listens for when add event button is clicked.
 */
$(document).on('click', '.addEventButton', function() {

    $(".addEventSvg").toggleClass('rotated');
    $(".eventForm").slideToggle();
})


/**
 * Listens for when add milestone button is clicked.
 */
$(document).on('click', '.addMilestoneButton', function() {

    $(".addEventSvg").toggleClass('rotated');
    $(".milestoneForm").slideToggle();
})


//TODO add deadlines add button click


/**
 * Listens for a click on the delete button
 */
$(document).on("click", ".deleteButton", function(){
    let parent = $(this).closest(".occasion")
    if (parent.hasClass('event')) { // Checks if the button belongs to an event
        let eventData = {"eventId": $(this).closest(".occasion").find(".eventId").text()}
        $.ajax({
            url: "deleteEvent",
            type: "DELETE",
            data: eventData,
            success: function(response) {
                notifyEdit(eventData.eventId, "notifyRemoveEvent")
            }
        })
    } else if (parent.hasClass('milestone')) {
        let milestoneData = {"milestoneId": $(this).closest(".occasion").attr("id")}
        $.ajax({
            url: 'deleteMilestone',
            type: "DELETE",
            data: milestoneData,
            success: function(response) {
                notifyEdit(milestoneData.milestoneId, "notifyRemoveEvent")
            }
        })
    }
    //TODO add deadlines
})

/**
 * Listens for a click on the edit button
 */
$(document).on("click", ".editButton", function() {
    thisUserIsEditing = true;
    $(".addOccasionButton").hide()
    $(".editButton").hide()
    $(".deleteButton").hide()
    let parent = $(this).closest(".occasion")
    let id = parent.attr("id")
    notifyEdit(id, "editEvent")
    if (parent.hasClass("event")) {
        appendEventForm(parent)
    } else if (parent.hasClass("milestone")) {
        appendMilestoneForm(parent)
    }

    $(".addOccasionButton").show()
    //TODO add deadlines


})



/**
 * Listens for a click on the event form cancel button
 */
$(document).on("click", ".cancelEdit",function() {
    thisUserIsEditing = false;
    $(".addOccasionButton").show()
    $(".editButton").show()
    $(".deleteButton").show()

    let parent = $(this).closest(".occasion")
    let form = parent.find("form")
    let id = parent.attr("id")
    form.slideUp(400, function () {
        form.remove();
    })
    notifyEdit(id, "notifyNotEditing") // Let the server know the event is no longer being edited

})




// <--------------------------- General Functions --------------------------->

/**
 * Adds Events to the sprints
 * Displays the events in the sprints in which the dates overlap.
 */
function addEventsToSprints(){
    $.ajax({
        url: 'getEventsList',
        type: 'get',
        data: {'projectId': projectId},

        success: function(response) {

            $(".eventInSprint").remove();

            for(let index in response){
                let event = response[index]

                $(".sprint").each(function(index, element) {

                    let eventStart = Date.parse(event.startDate)
                    let eventEnd = Date.parse(event.endDate)
                    let sprintStart = Date.parse($(element).find(".sprintStart").text())
                    let sprintEnd = Date.parse($(element).find(".sprintEnd").text())
                    if(eventStart >= sprintStart && eventStart <= sprintEnd) { // Event start right between sprint dates
                        appendEventToSprint(element, event)
                    } else if (eventEnd >= sprintStart && eventEnd <= sprintEnd) { //Event end falls within the sprint dates
                        appendEventToSprint(element, event)
                    }
                })

                $(".sprint").each(function(index, element) {

                    let eventStart = Date.parse(event.startDate)
                    let eventEnd = Date.parse(event.endDate)
                    let sprintStart = Date.parse($(element).find(".sprintStart").text())
                    let sprintEnd = Date.parse($(element).find(".sprintEnd").text())

                    if(eventStart >= sprintStart && eventStart <= sprintEnd) {
                        $(".eventInSprint" + event.id).find(".sprintEventStart").css("color", $(element).find(".sprintColour").text())
                    }
                    if ( sprintStart <= eventEnd && eventEnd <= sprintEnd) {
                        $(".eventInSprint" + event.id).find(".sprintEventEnd").css("color", $(element).find(".sprintColour").text())
                    }
                })
            }
        },
        error: function(error) {
            console.log(error)
        }
    })
}

/**
 * At
 * @param elementToAppendTo
 * @param event
 */
function appendEventToSprint(elementToAppendTo, event) {
    let eventInSprint = `
                <div class="row">
                    <div class="col">
                        <div class="eventInSprint eventInSprint${event.id}" >
                            <p class="sprintEventName">${event.name} : </p>
                            <p class="sprintEventStart">${event.startDateFormatted}</p>
                            <p>-</p>
                            <p class="sprintEventEnd">${event.endDateFormatted}</p>
                        </div>
                    </div>
                </div>`
    $(elementToAppendTo).append(eventInSprint)
}


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
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 * @param element the element to append the form too.
 */
function appendEventForm(element){
    let eventName = $(element).find(".eventName").text();
    let eventStart = $(element).find(".eventStartDateNilFormat").text().slice(0,16);
    let eventEnd = $(element).find(".eventEndDateNilFormat").text().slice(0,16);

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
                        <button type="button" class="btn btn-secondary cancelEdit" >Cancel</button>
                    </div>
                </form>`)




    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
    $("#editEventForm").slideDown();

}


/**
 * Appends form to the element that is passed to it.
 * Also gets data from that element.
 * @param element the element to append the form too.
 */
function appendMilestoneForm(element){

    let milestoneName = $(element).find(".milestoneName").text();
    let milestoneEnd = $(element).find(".milestoneEndDateNilFormat").text().slice(0,16);



    $(element).append(`
                <form class="existingMilestoneForm" id="milestoneEditForm" style="display: none">
                        <div class="mb-1">
                        <label for="milestoneName" class="form-label">Milestone name</label>
                        <input type="text" class="form-control form-control-sm milestoneName" id="milestoneName" value="${milestoneName}" maxlength="${eventNameLengthRestriction}" name="milestoneName" required>
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
                        <button type="button" class="btn btn-secondary cancelEdit" >Cancel</button>
                    </div>
                </form>`)




    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
    $("#milestoneEditForm").slideDown();
}



//TODO add in deadline append form






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
            <div class="occasion event" id="${eventObject.id}">
                <p class="eventId" style="display: none">` + eventObject.id + `</p>
                <p class="eventStartDateNilFormat" style="display: none">${eventObject.start}</p>
                <p class="eventEndDateNilFormat" style="display: none">${eventObject.end}</p>
                <p class="typeOfEvent" style="display: none">${eventObject.typeOfEvent}</p>
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="eventName name" >${eventObject.name}</p>
                </div>
                <div class="controlButtons">
                    <button class="editButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Event">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                            <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                            <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                        </svg>
                    </button>
                    <button type="button" class="deleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Event">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor" class="bi bi-x-circle" viewBox="0 0 16 16">
                            <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                        </svg>
                    </button>
                </div>
                
                <div class="eventDateDiv">
                    <p class="eventStart">Start Date: ${eventObject.startFormatted}</p>
                    <p class="eventEnd">End Date: ${eventObject.endFormatted}</p>
                </div>
            </div>`;
}


/**
 * Creates the Milestone divs from the milestoneObject
 * @param milestoneObject A Json object with event details
 * @returns {string} A div
 */
function createMilestoneDiv(milestoneObject) {
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
            <div class="occasion milestone" id="${milestoneObject.id}">
                <p class="milestoneId" style="display: none">${milestoneObject.id}</p>
                <p class="milestoneEndDateNilFormat" style="display: none">${milestoneObject.endDate}</p>
                <p class="typeOfMilestone" style="display: none">${milestoneObject.type}</p>
                
                
                
                <div class="mb-2 occasionTitleDiv">
                    <div class="occasionIcon">
                        ${iconElement}
                    </div>
                    <p class="milestoneName name">${milestoneObject.name}</p>
                </div>
                <div class="controlButtons">
                    <button class="editButton noStyleButton hasTeacherOrAbove" data-bs-toggle="tooltip" data-bs-placement="top" title="Edit Milestone">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                                 class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                                <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                                <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                            </svg>
                        </button>
                        <button type="button" class="deleteButton noStyleButton hasTeacherOrAbove"  data-bs-toggle="tooltip" data-bs-placement="top" title="Delete Milestone">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                                 class="bi bi-x-circle" viewBox="0 0 16 16">
                                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                                <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                            </svg>
                        </button>
                </div>
                
                <div class="milestoneDateDiv">
                    <p class="milestoneEnd">${milestoneObject.endDateFormatted}</p>
                </div>
            </div>
`;
}


//TODO add in createDeadlineDiv




/**
 * Refreshes the event div section of the page
 */
function refreshEvents(){
    $("#eventContainer").find(".occasion").remove() // Finds all event divs are removes them
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
            addEventsToSprints()

        },
        error: function(error) {
            console.log(error)
        }
    })

}



/**
 * Refreshes the milestone div section of the page
 * @param projectId
 */
function refreshMilestones(projectId){
    let milestoneContainer = $("#milestoneContainer")
    milestoneContainer.find(".occasion").remove()
    milestoneContainer.append(`<div id="infoMilestoneContainer" class="infoMessageParent alert alert-primary alert-dismissible fade show" role="alert" style="display: none">
            </div>`) // Adds an info box to the page
    $.ajax({
        url: 'getMilestoneList',
        type: 'get',
        data: {'projectId': projectId},

        success: function(response) {
            for(let milestone in response){ // Goes through all the data from the server and creates an eventObject
                let milestoneObject = {
                    "id" : response[milestone].id,
                    "name" : response[milestone].name,
                    "endDate" : response[milestone].endDate,
                    "endDateFormatted" : response[milestone].endDateFormatted,
                    "type" : response[milestone].type,
                }

                $("#milestoneContainer").append(createMilestoneDiv(milestoneObject)) // Passes the eventObject to the createDiv function
                sortElementsByDate("#milestoneContainer", ".occasion", ".endDate")
                removeElementIfNotAuthorized()
            }
            // addMilestoneToSprints()

        },
        error: function(error) {
            console.log(error)
            // location.href = "/error" // Moves the user to the error page
        }
    })


}


//TODO refresh for deadlines


function reloadElement(id){
    let elementToReload = $("#" + id)
    elementToReload.slideUp() // Hides the element
    if (elementToReload.hasClass("event")){
        $.ajax({
            url: 'getEvent',
            type: 'get',
            data: {'eventId': id},
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

                elementToReload.replaceWith(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
                elementToReload.slideDown()
                addEventsToSprints()
                sortElementsByDate("#eventContainer", ".occasion", ".eventStartDateNilFormat")

            },
            error: function() {
                location.href = "error" // Moves the user to the error page
            }
        })
    } else if (elementToReload.hasClass("milestone")) {
        $.ajax({
            url: 'getMilestone',
            type: 'get',
            data: {'milestoneId' : id},
            success: function(response) {

                elementToReload.replaceWith(createMilestoneDiv(response))
                elementToReload.slideDown()
            },
            error: function() {
                location.href = "error" // Moves the user to the error page
            }
        })
    }

    $(".editButton").show()
    $(".deleteButton").show()
    removeElementIfNotAuthorized()

    //TODO add milestones and deadlines


}


/**
 * Removes specific event
 * @param eventId id of event to remove
 */

function removeElement(eventId) {
    let element = $("#" + eventId)

    element.slideUp(400, function() {
        element.remove()
    })
    //TODO add deadlines and milestones
    addEventsToSprints()
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
                "end" : response.dateTime,
                "startFormatted" : response.startDateFormatted,
                "endFormatted" : response.endDateFormatted,
                "typeOfEvent" : response.type,
            }

            $("#eventContainer").append(createEventDiv(eventObject)) // Passes the eventObject to the createDiv function
            sortElementsByDate("#eventContainer", ".occasion", ".eventStartDateNilFormat")
            addEventsToSprints()
            removeElementIfNotAuthorized()

        },
        error: function() {
            location.href = "error" // Moves the user to the error page
        }
    })
}


function addMilestone(milestoneId) {
    $.ajax({
        url: "getMilestone",
        type: "GET",
        data: {"milestoneId" : milestoneId},
        success: function(response) {

            let milestoneObject = {
                "id" : response.id,
                "name" : response.name,
                "endDate" : response.endDate,
                "endDateFormatted" : response.endDateFormatted,
                "type" : response.type,
            }

            $("#milestoneContainer").append(createMilestoneDiv(milestoneObject)) // Passes the eventObject to the createDiv function
            sortElementsByDate("#milestoneContainer", ".occasion", ".endDate")
            // addMilestonesToSprints()
            removeElementIfNotAuthorized()

        },
        error: function() {
            location.href = "error" // Moves the user to the error page
        }
    })
}


//TODO add deadline


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