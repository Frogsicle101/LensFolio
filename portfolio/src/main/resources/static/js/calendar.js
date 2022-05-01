
/**
 * $(document).ready fires off a function when the document has finished loading.
 * https://learn.jquery.com/using-jquery-core/document-ready/
 */
$(document).ready(function() {
  let projectId = $("#projectId").html();
  let calendarEl = document.getElementById('calendar');


  /**
   * Calendar functionality
   */
  let calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    themeSystem: 'bootstrap5',
    eventSources: [
      {
        url: '/getProjectDetails',
        color: "black",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function () {
          $(".errorMessage").text("Error: Project not found")
          $(".errorMessageParent").show();
        },
        success: function (eventDataFromServer) {

          let calendarEvent = calendar.getEventById(eventDataFromServer.id) //Check if event already exists in the calendar.

          if (calendarEvent === null) { //Event doesn't exist, creates event.

            calendar.addEvent({
              title: eventDataFromServer.name,
              start: eventDataFromServer.startDate,
              end: eventDataFromServer.endDate,
              id: eventDataFromServer.id,
              display: 'inverse-background',
              backgroundColor: '#858585'
            })
          } else { // Event does already exist, updates it.
            calendarEvent.title = eventDataFromServer.name;
            calendarEvent.id = eventDataFromServer.id;
            calendarEvent.start = eventDataFromServer.startDate;
            calendarEvent.end = eventDataFromServer.endDate;
          }
        }
      },
      {
        url: '/getProjectSprints',
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function () {
          $(".errorMessage").text("Error: Sprints not found")
          $(".errorMessageParent").show();
        },
        success: function (rawEvent) {
          rawEvent.forEach(function (currentSprint) {
            //Check if sprint already exists
            let sprintEvent = calendar.getEventById(currentSprint.id)
            if (sprintEvent === null) {
              //Sprint doesn't exist
              calendar.addEvent({
                title: currentSprint.name,
                start: currentSprint.startDate,
                end: currentSprint.endDate,
                id: currentSprint.id,
                color: currentSprint.colour
              })
            } else {
              sprintEvent.title = currentSprint.name;
              sprintEvent.id = currentSprint.id;
              sprintEvent.start = currentSprint.startDate;
              sprintEvent.end = currentSprint.endDate;
              sprintEvent.color = currentSprint.colour;
            }
          })
        }
      }
    ]

  });


  calendar.render();

})