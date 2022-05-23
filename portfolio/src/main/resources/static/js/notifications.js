let stompClient = null

function connect() {
    let socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/notifications/receiving/occasions', function (notification) {
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
 * Notifies the server that this user is editing.
 * @param name The name of whoever is making the edit
 * @param occasion The type of the object being edited (milestone, deadline, event)
 * @param subject The name of whatever item the person is editing.
 * @param subjectId The ID of our subject
 */
function requestNotify(name, occasion, subject, subjectId) {
    stompClient.send("/notifications/sending/OccasionEdit", {}, JSON.stringify({
        'name': name,
        'occasion': occasion,
        'subject': subject,
        'subjectId': subjectId,
        'type':'notify'
    }));
}

/**
 * Notifies the server that this user is editing.
 * @param type What type of message this is.
 *      Is it informing us to update something? delete something?
 *      Or just notify people about something?
 *      should be one of:
 *      'create', 'update', 'delete', 'notify'
 * @param occasion The type of the object being edited (milestone, deadline, event)
 * @param subjectId The ID of our subject
 * @param content an optional content field, if we want to transmit any extra information
 */
function requestReload(type, occasion, subjectId, content = '') {
    stompClient.send("/notifications/sending/OccasionReload", {}, JSON.stringify({
        'type': type,
        'occasion': occasion,
        'subjectId': subjectId,
        'content':content
    }));
}

export {connect, requestReload, requestNotify}