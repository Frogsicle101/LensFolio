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
            handleCreateEvent();
            break;
        case 'update' :
            handleUpdateEvent();
            break;
        case 'delete' :
            handleDeleteEvent();
            break;
        case 'notify' :
            handleNotifyEvent();
            break;
        default :
            // Do nothing, unknown message format
            break;
    }
}

/**
 * Sends a message to the server.
 * We don't need to add our ID as the server can get it from the websocket authentication
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
    const editorName = notification.editorName;
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;
    console.log("Todo: Handle create event: notification controller line 65");
    // Link this up to the events controller. Use events.js lines 120-130 for reference

}


function handleUpdateEvent( notification ) {
    const editorName = notification.editorName;
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;
    console.log("Todo: Handle update event: notification controller line 74");
    // Link this up to the events controller. Use events.js lines 61-97 for reference
    // Note: new format may require some refactoring as notify editing and not editing
    //       are no longer separate, hence an update with some special body may mean not
    //       editing. (See Sam if confused)
}


function handleDeleteEvent( notification ) {
    const editorName = notification.editorName;
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;
    console.log("Todo: Handle notify event: notification controller line 83");
    // Link this up to the events controller. Use events.js lines 103-114 for reference
}


function handleNotifyEvent( notification ) {
    const editorName = notification.editorName;
    const occasionType = notification.occasionType;
    const occasionId = notification.occasionId;
    console.log("Todo: Handle notify event: notification controller line 92");
    // Link this up to the events controller. Use events.js lines 61-97 for reference
    // Note: new format may require some refactoring as notify editing and not editing
    //       are no longer separate, hence an update with some special body may mean not
    //       editing. (See Sam if confused)}
}