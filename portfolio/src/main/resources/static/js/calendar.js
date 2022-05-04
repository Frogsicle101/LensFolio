
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