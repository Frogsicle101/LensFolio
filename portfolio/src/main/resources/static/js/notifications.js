let stompClient = null

function connect() {
    let socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/notifications/sending/occasions', handleNotification);
    });
}

function handleNotification(notification) {
    const content = JSON.parse(notification.body);
    const action = content.action;

    //Whenever we receive a message from the url, this function will run.
    /*TODO: use the ID and occasion to call the relevant functions to lock and notify
    Take a look at the STOMPOccasionMessage class to see what you have to work with
    You'll need to determine the occasion and then call those functions for the rest of it
     */
    /*
    The type of JSON object we're receiving is modeled by STOMPOccasionMessage.
    Please refer to the class' documentation for details.
     */

    switch (action) {
        case 'create' :
            handleCreateEvent(content);
            break;
        case 'update' :
            handleUpdateEvent(content);
            break;
        case 'delete' :
            handleDeleteEvent(content);
            break;
        case 'edit' :
            handleNotifyEvent(content);
            break;
        case 'stop' :
            handleStopEvent(content);
            break;
        default :
            // Do nothing, unknown message format
            break;
    }
}

/**
 * Sends a message to the server.
 * We don't need to add our ID as the server can get it from the websocket authentication
 *
 * @param occasionType The type of the object being edited (milestone, deadline, event)
 * @param occasionId The ID of our the object being edited
 * @param action What action the user has performed to create this message
 */
function sendNotification(occasionType, occasionId, action) {
    stompClient.send("/notifications/receiving/message", {}, JSON.stringify({
        'occasionType': occasionType,
        'occasionId': occasionId,
        'action': action
    }));
}




// --------------------------------------------- Notification handlers -------------------------------------------------

function handleCreateEvent( notification ) {
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;
    console.log("Handle create event: Adding occasion of type: " + occasionType + " and ID: " + occasionId);
    switch (occasionType) {
        case 'event' :
            addEvent(occasionId)
            break
        case 'milestone' :
            addMilestone(occasionId)
            break
        case 'deadline' :
            addDeadline(occasionId)
            break
        default :
            console.log("WARNING: un-supported occasion type receieved. Ignoring message")
            break
    }
}


function handleUpdateEvent( notification ) {
    const occasionId = notification.occasionId;

    reloadElement(occasionId)
}

/**
 * Processes a delete notification by removing the element from the DOM
 * @param notification
 */
function handleDeleteEvent( notification ) {
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;

    removeElement(occasionId) // removes specific event

    removeClass()
    //Now reload the elements, depending on what type of element was removed
    switch (occasionType) {
        case "event":
            removeClass(`eventInSprint${occasionId}`);
            break;
        case "milestone":
            removeClass(`milestoneInSprint${occasionId}`);
            break;
        case "deadline":
            removeClass(`deadlineInSprint${occasionId}`);
            break;
    }
}

/**
 * Opens a dialog box at the top of the screen, and disables the edit buttons for the
 * occasion that is being edited.
 * @param notification
 */
function handleNotifyEvent( notification ) {
    const editorName = notification.editorName;
    const occasionId = notification.occasionId;
    if (checkPrivilege()) {
        let infoContainer = $("#informationBar");
        let eventDiv = $("#" + occasionId)
        let noticeSelector = $("#notice" + occasionId)

        let eventName = eventDiv.find(".name").text();

        if (!noticeSelector.length) {
            let infoString = editorName + " is editing element: " + eventName
            infoContainer.append(`<p class="infoMessage text-truncate" id="notice${occasionId}"> ` + infoString + `</p>`)
            eventDiv.addClass("beingEdited") // Add class that shows which event is being edited
            if (eventDiv.hasClass("beingEdited")) {
                eventDiv.find(".controlButtons").hide()
            }
            infoContainer.slideDown() // Show the notice.
        }
    }
}

/**
 * Reverts all the changes made by handleNotifyEvent
 * @param notification
 */
function handleStopEvent( notification ) {

    const occasionId = notification.occasionId;

    if (checkPrivilege()) {
        let infoContainer = $("#informationBar");
        let elementDiv = $("#" + occasionId);

        $("#notice" + occasionId).remove()
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
}