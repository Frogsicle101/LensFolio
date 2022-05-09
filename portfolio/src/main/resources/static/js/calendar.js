
/**
 * $(document).ready fires off a function when the document has finished loading.
 * https://learn.jquery.com/using-jquery-core/document-ready/
 */

/**
 * Function to check if two events are allowed to overlap each other
 * TODO Implement check
 * @returns {boolean}
 */
function overlapCheck(stillEvent, movingEvent) {
  return false;
}

function eventResize (info) {

  if (!confirm("update " + info.event.title +" start date to " + info.event.start.toISOString() + " and end date to " + info.event.end.toISOString() )) {
    info.revert();
  }

  // TODO Check that new dates are valid


  // Data to send in post request to server
  let dataToSend= {
    "sprintId" : info.event.id,
    "sprintName" : info.event.title,
    "sprintStartDate" : info.event.start.toISOString().split("T")[0],
    "sprintEndDate" : info.event.end.toISOString().split("T")[0],
    "sprintDescription" : info.event.description,
    "sprintColour" : info.event.backgroundColor
  }

  alert(JSON.stringify(dataToSend));

  // Update sprint to have new start and end dates
  $.ajax({
    url: "/sprintSubmit",
    type: "post",
    data: dataToSend,
    success: function(){
      alert("Updated sprint dates");
    },
    error: function(error){
      console.log(error.responseText)
      $(".errorMessage").text(error.responseText)
      $(".errorMessageParent").slideUp()
      $(".errorMessageParent").slideDown()
    }
  })

}

$(document).ready(function() {
  let projectId = $("#projectId").html();
  let calendarEl = document.getElementById('calendar');


  /**
   * Calendar functionality
   * https://fullcalendar.io/docs
   */
  let calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'dayGridMonth',
    eventDurationEditable: true,
    eventResizableFromStart: true,
    eventResize: function( info ) {
      eventResize( info )
    },
    themeSystem: 'bootstrap5',
    eventSources: [{ //The sources to grab the events from.
      url: '/getProjectSprintsWithDatesAsFeed', //Project sprints
      method: "get",
      extraParams: {
                projectId: projectId.toString()
              },
      failure: function(err){
        console.log(err.responseText)
      }
    },
      {
        url: '/getProjectAsFeed', // Project itself
        method: "get",
        display: "inverse-background",
        extraParams: {
          projectId: projectId.toString()
        },
        failure: function(err){
          console.log(err.responseText)
        }
      }]

  });


  calendar.render();


})