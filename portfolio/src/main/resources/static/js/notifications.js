/** The STOMP client that connects to the server for sending and receiving notifications */
let stompClient = null


/**
 * Connects via websockets to the server, listening to all messages from /notifications/sending/occasions
 * and designates handleNotification to run whenever we get a message
 */
function connect() {
    let socket = new SockJS('gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('notifications/sending/occasions', handleNotification);
    });
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
    stompClient.send("notifications/receiving/message", {}, JSON.stringify({
        'occasionType': occasionType,
        'occasionId': occasionId,
        'action': action
    }));
}


/**
 * Whenever we receive a message from the /notifications/sending/occasions, this function will run.
 * This takes the notification, checks what type it is, then calls the relevant helper function
 * to handle that notification.
 *
 * @param notification The notification to handle. (modeled by OutgoingNotification)
 */
function handleNotification(notification) {
    const content = JSON.parse(notification.body);
    const action = content.action;

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
 * Processes a create notification by adding boxes for that notification to the DOM
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
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


/**
 * Helper function for handling an update event. Tells us to reload the element with the specific ID.
 * Occasion types are handled in the reloading method, so we only need to provide it the ID
 *
 * @param notification The update notification, from which we extract the ID (and also the type for logging)
 */
function handleUpdateEvent( notification ) {
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;
    console.log("Handle Update event: Reloading occasion of type: " + occasionType + " and ID: " + occasionId);

    reloadElement(occasionId)
}


/**
 * Processes a delete notification by removing the element from the DOM
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
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
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleNotifyEvent( notification ) {

    const editorId = notification.editorId;
    const editorName = notification.editorName;
    const occasionId = notification.occasionId;

    if (checkPrivilege()) {
        let infoContainer = $("#informationBar");
        let eventDiv = $("#" + occasionId)
        let noticeSelector = $("#notice" + occasionId)

        let eventName = eventDiv.find(".name").text();

        if (!noticeSelector.length) {
            let infoString = editorName + " is editing element: " + eventName
            infoContainer.append(`<p class="infoMessage text-truncate noticeEditor${editorId}" id="notice${occasionId}"> ` + infoString + `</p>`)
            eventDiv.addClass("beingEdited") // Add class that shows which event is being edited
            eventDiv.addClass("editor" + editorId)
            if (eventDiv.hasClass("beingEdited")) {
                eventDiv.find(".controlButtons").hide()
            }
            infoContainer.slideDown() // Show the notice.
        }
    }
}


/**
 * Reverts all the changes made by handleNotifyEvent
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleStopEvent( notification ) {

    const occasionId = notification.occasionId;
    const editorId = notification.editorId

    if (checkPrivilege()) {

        let elementDiv;
        let notice;

        if (occasionId === "*") { // A websocket disconnected, so we need to remove the element by the editorId
            notice = $(".noticeEditor" + editorId);
            elementDiv = $(".editor" + editorId);
        } else {
            elementDiv = $("#" + occasionId);
            notice = $("#notice" + occasionId);
        }

        notice.remove();

        elementDiv.removeClass("beingEdited")
        elementDiv.removeClass("editor" + editorId);

        if (!thisUserIsEditing) {
            elementDiv.find(".controlButtons").show()
        }

        if (elementDiv.hasClass("beingEdited")) {
            elementDiv.find(".controlButtons").hide()
        }

        let infoContainer = $("#informationBar");
        if (isEmpty(infoContainer)) {
            infoContainer.slideUp() // Hide the notice.
        }
    }
}