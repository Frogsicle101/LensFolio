/** The STOMP client that connects to the server for sending and receiving notifications */

let stompClient = null


/**
 * Called by STOMPJS whenever the websocket needs to reconnect
 * @returns A new instance of SockJS.
 */
function mySocketFactory() {
    return new SockJS(window.location.pathname + "/../websocket")
}


/**
 * Connects via websockets to the server, listening to all messages from /notifications/sending/occasions
 * and designates handleNotification to run whenever we get a message
 */
function connect() {

    stompClient = new StompJs.Client();
    stompClient.configure({
        // brokerURL: `ws://${window.location.hostname}:${window.location.port}/websocket`,
        webSocketFactory: mySocketFactory,
        reconnectDelay: 5000,
        debug: function (str) {
            console.log(str);
        },
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onStompError: function (frame) {
            console.log('Broker reported error: ' + frame.headers['message']);
            console.log('Additional details: ' + frame.body);
        },
        connectionTimeout: 20000
    });

    stompClient.onConnect = (frame) => {
        console.log('Connected: ' + frame);
        stompClient.subscribe('notifications/sending/occasions', handleNotification);
    }

    stompClient.activate();
}


/**
 * Sends a message to the server.
 * We don't need to add our ID as the server can get it from the websocket authentication
 *
 * @param occasionType The type of the object being edited (milestone, deadline, event)
 * @param occasionId The ID of the object being edited
 * @param action What action the user has performed to create this message
 */
function sendNotification(occasionType, occasionId, action) {
    stompClient.publish({
        destination: "notifications/message",
        body: JSON.stringify({
            'occasionType': occasionType,
            'occasionId': occasionId,
            'action': action
        })
    });
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
    console.log(content)
    for (let message of content) {
        const action = message.action;

        switch (action) {
            case 'create' :
                handleCreateEvent(message);
                break;
            case 'update' :
                handleUpdateEvent(message);
                break;
            case 'delete' :
                handleDeleteEvent(message);
                break;
            case 'edit' :
                handleNotifyEvent(message);
                break;
            case 'stop' :
                handleStopEvent(message);
                break;
            default :
                // Do nothing, unknown message format
                break;
        }
    }
}


/**
 * Processes a create notification by adding boxes for that notification to the DOM
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleCreateEvent(notification) {
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
        case "sprint" :
            $(".sprintsContainer").empty()
            getSprints()
            break
        default :
            console.log("WARNING: un-supported occasion type received. Ignoring message")
            break
    }
}


/**
 * Helper function for handling an update event. Tells us to reload the element with the specific ID.
 * Occasion types are handled in the reloading method, so we only need to provide it the ID
 *
 * @param notification The update notification, from which we extract the ID (and also the type for logging)
 */
function handleUpdateEvent(notification) {
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;
    console.log("Handle Update event: Reloading occasion of type: " + occasionType + " and ID: " + occasionId);
    switch (occasionType) {
        case 'event' :
            reloadElement(occasionId)
            break
        case 'milestone' :
            reloadElement(occasionId)
            break
        case 'deadline' :
            reloadElement(occasionId)
            break
        case "sprint" :
            $(".sprintsContainer").empty()
            getSprints()
            break
        default :
            console.log("WARNING: un-supported occasion type received. Ignoring message")
            break
    }
}


/**
 * Processes a delete notification by removing the element from the DOM
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleDeleteEvent(notification) {
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
        case "sprint" :
            $(".sprintsContainer").empty()
            getSprints()
            break
    }
}


/**
 * Opens a dialog box at the top of the screen, and disables the edit buttons for the
 * occasion that is being edited.
 *
 * @param notification The JSON object we receive (modeled by OutgoingNotification).
 */
function handleNotifyEvent(notification) {

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
function handleStopEvent(notification) {

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