

$(document).ready(function() {
  let projectId = $("#projectId").html();


  let calendarEl = document.getElementById('calendar');
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
          //TODO add a failure thing.
        },
        success: function (rawEvent) {
          //Check if event already exists
          let projectEvent = calendar.getEventById(rawEvent.id)

          if (projectEvent === null) {
            //Event doesn't exist
            calendar.addEvent({
              title: rawEvent.name,
              start: rawEvent.startDate,
              end: rawEvent.endDate,
              id: rawEvent.id,
              display: 'background',
              backgroundColor: '#dadada'
            })
          } else {
            projectEvent.title = rawEvent.name;
            projectEvent.id = rawEvent.id;
            projectEvent.start = rawEvent.startDate;
            projectEvent.end = rawEvent.endDate;
          }
        }
      },
      {
        url: '/getProjectSprints',
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function () {
          //TODO add a failure thing.
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