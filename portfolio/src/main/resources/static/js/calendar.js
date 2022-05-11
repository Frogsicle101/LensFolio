
/**
 * Function to check if two events are allowed to overlap each other
 * TODO Implement check
 * @returns {boolean}
 */
function overlapCheck(stillEvent, movingEvent) {
  return false;
}


function eventResize (info) {
  alert(info.event.title + " end is now " + info.event.end.toISOString());

  if (!confirm("is this okay?")) {
    info.revert();
  }
}

/**
 * $(document).ready fires off a function when the document has finished loading.
 * https://learn.jquery.com/using-jquery-core/document-ready/
 */
$(document).ready(function() {
  let projectId = $("#projectId").html();
  let calendarEl = document.getElementById('calendar');

  let dateRange;
  /**
   * Calendar functionality
   * https://fullcalendar.io/docs
   */
  let calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    eventDurationEditable: true,
    eventResizableFromStart: true,
    eventResize,
    themeSystem: 'bootstrap5',
    eventSources: [
      { //The sources to grab the events from.
        url: 'getProjectSprintsWithDatesAsFeed', //Project sprints
        method: "get",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function (err) {
          console.log(err.responseText)
        }
      },
      {
        url: 'getProjectAsFeed', // Project itself
        method: "get",
        display: "inverse-background",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function (err) {
          console.log(err.responseText)
        }
      },
      {
        url: 'getEventsAsFeed', // Get all milestones
        method: "get",
        display: "inverse-background",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function (err) {
          console.log(err.responseText)
        }
      },
      {
        url: 'getMilestonesAsFeed', // Get all milestones
        method: "get",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function (err) {
          console.log(err.responseText)
        },
      }
    ],
    eventDidMount : function(info) {
      if(info.event.classNames.toString() === "milestoneCalendar") {
        info.el.innerHTML = `<col><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" className="bi bi-trophy-fill" viewBox="0 0 16 16">
                                    <path d="M2.5.5A.5.5 0 0 1 3 0h10a.5.5 0 0 1 .5.5c0 .538-.012 1.05-.034 1.536a3 3 0 1 1-1.133 5.89c-.79 1.865-1.878 2.777-2.833 3.011v2.173l1.425.356c.194.048.377.135.537.255L13.3 15.1a.5.5 0 0 1-.3.9H3a.5.5 0 0 1-.3-.9l1.838-1.379c.16-.12.343-.207.537-.255L6.5 13.11v-2.173c-.955-.234-2.043-1.146-2.833-3.012a3 3 0 1 1-1.132-5.89A33.076 33.076 0 0 1 2.5.5zm.099 2.54a2 2 0 0 0 .72 3.935c-.333-1.05-.588-2.346-.72-3.935zm10.083 3.935a2 2 0 0 0 .72-3.935c-.133 1.59-.388 2.885-.72 3.935z"/> 
                                    </svg></col> <col> ${info.event.title} </col>`
      }
      else if(info.event.classNames.toString() === "eventCalendar") {
        info.el.innerHTML = `<col><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-calendar3-event-fill" viewBox="0 0 16 16">
                                    <path fill-rule="evenodd" d="M2 0a2 2 0 0 0-2 2h16a2 2 0 0 0-2-2H2zM0 14V3h16v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2zm12-8a1 1 0 1 0 2 0 1 1 0 0 0-2 0z"/>
                                    </svg></col> <col> ${info.event.title} </col>`
      }
     },
  });

  calendar.render();
})

