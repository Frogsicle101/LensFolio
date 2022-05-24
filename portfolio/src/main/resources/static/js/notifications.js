let stompClient = null

function connect() {
    let socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/notifications/sending/occasions', function (notification) {
            //Whenever we receive a message from the url, this function will run.
            /*TODO: use the ID and occasion to call the relevant functions to lock and notify
            Take a look at the STOMPOccasionMessage class to see what you have to work with
            You'll need to determine the occasion and then call those functions for the rest of it
             */
            /*
            The type of JSON object we're receiving is modeled by STOMPOccasionMessage.
            Please refer to the class' documentation for details.
             */
            console.log('Receieved message:' + JSON.parse(notification.body).content);
        });
    });
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
