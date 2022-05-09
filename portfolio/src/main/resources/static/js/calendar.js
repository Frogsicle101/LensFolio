/**
 * Runs when a sprint is resized on the calendar
 * @param info
 */
function eventResize (info) {
  // Data to send in post request to server

  // Add a day to returned start date due to how full calendar defines start date
  let startDate = new Date (info.event.start.toISOString().split("T")[0])
  startDate.setDate(startDate.getDate() + 1);

  let dataToSend= {
    "sprintId" : info.event.id,
    "sprintName" : info.event.title,
    "sprintStartDate" : startDate.toISOString().split("T")[0],
    "sprintEndDate" : info.event.end.toISOString().split("T")[0],
    "sprintDescription" : info.event.extendedProps.description,
    "sprintColour" : info.event.backgroundColor
  }

  // Update sprint to have new start and end dates
  $.ajax({
    url: "/sprintSubmit",
    type: "post",
    data: dataToSend,
    success: function(){
      $(".errorMessageParent").slideUp()
      $(".successMessage").text("Sprint dates updated successfully")
      $(".successMessageParent").slideUp()
      $(".successMessageParent").slideDown()
    },
    error: function(error){
      console.log(error.responseText)
      $(".errorMessage").text(error.responseText)
      $(".errorMessageParent").slideUp()
      $(".errorMessageParent").slideDown()
      info.revert()
    }
  })

}

/**
 * Runs when an event is clicked
 * @param info
 */
function eventClick (info) {
  info.event.eventBackgroundColor = '#aaa' ;
  // info.el.classList.add('selected-event');
}

/**
 * $(document).ready fires off a function when the document has finished loading.
 * https://learn.jquery.com/using-jquery-core/document-ready/
 */

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
    eventClick: function( info ) {
      eventClick( info )
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