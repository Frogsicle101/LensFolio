let beginDateStr = $("#projectStartDate").html().toString();
let endDateStr = $("#projectEndDate").html().toString();

let sprintList = $("#sprints").val();
//console.log(sprintList);

beginDateStr = beginDateStr.replace("-", "");
beginDateStr = beginDateStr.replace("-", "");
endDateStr = endDateStr.replace("-", "");
endDateStr = endDateStr.replace("-", "");

//let sprintList = $("#$sprints").html();






(function () {

  /*
   * Display calendar from current system time
   */
  let dateObj = (function () {
    let _date = new Date(); // current system date
    return {
      getDate: function () {
        return _date;
      },
      setDate: function (date) {
        _date = date;
      },
    };
  })();

  // setting calendar div in html
  renderHtml();
  // display numbers in table
  showCalendarData();
  // display sprint and project
  bindEvent();

  checkIfSprint()

  function checkIfSprint(){
    let sprints = $.ajax({
      url: "getProjectSprints",
      type: "GET",
      data: { projectId: 1 },
    }).done(function (obj) {
      $("td").each(function(){
        let element = $(this)
        let date = $(this).attr("date")
        let calendarDate = new Date(date)
        let today = new Date()
        let projectStartDate = new Date(obj[0].project.startDate)
        let projectEndDate = new Date(obj[0].project.endDate)


        if (calendarDate >= projectStartDate && calendarDate <= projectEndDate){
          element.css("background-color", "white")
        } else {
          element.css("background-color", "grey")
        }
        if(calendarDate.getDate() === today.getDate() && calendarDate.getMonth() === today.getMonth() && calendarDate.getFullYear() === today.getFullYear()) {
          element.css("border", "solid 3px red")
        } else {
          element.css("border", "solid 1px black")
        }

        obj.every(function(sprint) {
          let startDate = new Date(sprint.startDate)
          let endDate = new Date(sprint.endDate)
          if (calendarDate >= startDate.setHours(0) && calendarDate <= endDate){
            element.css("background-color", sprint.colour)
            return false
          }

          return true
        })

      })

    });

  }

  /**
   * setting calendar div in html
   */
  function renderHtml() {
    let calendar = document.getElementById("calendar");
    let titleBox = document.createElement("div"); // Display the current month, and change the month
    let bodyBox = document.createElement("div"); // Displays every day of the month

    // setting titleBox
    titleBox.className = "calendar-title-box";
    titleBox.innerHTML =
      "<span class='prev-month' id='prevMonth'></span>" +
      "<span class='calendar-title' id='calendarTitle'></span>" +
      "<span id='nextMonth' class='next-month'></span>";
    calendar.appendChild(titleBox); // add to calendar div

    // setting bodyBox
    bodyBox.className = "calendar-body-box";
    let _headHtml =
      "<tr>" +
      "<th>Su</th>" +
      "<th>Mo</th>" +
      "<th>Tu</th>" +
      "<th>We</th>" +
      "<th>Th</th>" +
      "<th>Fr</th>" +
      "<th>Sa</th>" +
      "</tr>";
    let _bodyHtml = "";

    // setting 6 lines for 6 weeks
    for (let i = 0; i < 6; i++) {
      _bodyHtml +=
        "<tr>" +
        "<td></td>" +
        "<td></td>" +
        "<td></td>" +
        "<td></td>" +
        "<td></td>" +
        "<td></td>" +
        "<td></td>" +
        "</tr>";
    }
    bodyBox.innerHTML =
      "<table id='calendarTable' class='calendar-table'>" +
      _headHtml +
      _bodyHtml +
      "</table>";
    // add to calendar div
    calendar.appendChild(bodyBox);
  }

  /**
   * chech it is during project date or not
   */
  function isDuringDate(dateCheckStr) {
    if (dateCheckStr >= beginDateStr && endDateStr >= dateCheckStr) {
      return true;
    }
    return false;
  }

  /**
   * display numbers in table
   */
  function showCalendarData() {

    let _year = dateObj.getDate().getFullYear();
    let _month = dateObj.getDate().getMonth() + 1;
    let _dateStr = getDateStr(dateObj.getDate());

    // setting calendar title information
    let calendarTitle = document.getElementById("calendarTitle");
    let titleStr = _dateStr.substr(0, 4) + " - " + _dateStr.substr(4, 2);

    calendarTitle.innerText = titleStr;

    // setting numbers in body table
    let _table = document.getElementById("calendarTable");
    let _tds = _table.getElementsByTagName("td");
    let _firstDay = new Date(_year, _month - 1, 1); // first day in current month
    for (let i = 0; i < _tds.length; i++) {
      let _thisDay = new Date(_year, _month - 1, i + 1 - _firstDay.getDay());
      let _thisDayStr = getDateStr(_thisDay);

      _tds[i].innerText = _thisDay.getDate();
      //_tds[i].data = _thisDayStr;
      _tds[i].setAttribute("date", _thisDay.toString());

      if (_thisDayStr == getDateStr(new Date())) {
        // current day
        _tds[i].className = "currentDay_project";
      }
      if (isDuringDate(_thisDayStr)) {
        if (_thisDayStr == getDateStr(new Date())) {
          // current day + project date
          _tds[i].className = "currentDay_project";
        } else {
          _tds[i].className = "project-bgcolor"; // project date
        }
      } else {
        if (_thisDayStr == getDateStr(new Date())) {
          // current day + no project date
          _tds[i].className = "currentDay_no_project";
        } else {
          _tds[i].className = "no-project-bgcolor"; // not project date
        }
      }
    }
  }


  /**
   * Bind previous month to next month events
   */
  function bindEvent() {
    let prevMonth = document.getElementById("prevMonth");
    let nextMonth = document.getElementById("nextMonth");
    addEvent(prevMonth, "click", toPrevMonth);
    addEvent(nextMonth, "click", toNextMonth);
  }

  /**
   * Bind event
   */
  function addEvent(dom, eType, func) {
    if (dom.addEventListener) {
      // DOM 2.0
      dom.addEventListener(eType, function (e) {
        func(e);
      });
    } else if (dom.attachEvent) {
      // IE5+
      dom.attachEvent("on" + eType, function (e) {
        func(e);
      });
    } else {
      // DOM 0
      dom["on" + eType] = function (e) {
        func(e);
      };
    }
  }

  /**
   * Click the previous month icon
   */
   function toPrevMonth() {
    let date = dateObj.getDate();
    if(getDateStr(date) >= beginDateStr){
      dateObj.setDate(new Date(date.getFullYear(), date.getMonth() - 1, 1));
    }
    else {
      dateObj.setDate(new Date(date.getFullYear(), date.getMonth(), 1));
    }
      showCalendarData();
      checkIfSprint()
    
    

  }

  /**
   * Click the next month icon
   */
  function toNextMonth() {
    let date = dateObj.getDate();
    if(getDateStr(date) <= endDateStr){
      dateObj.setDate(new Date(date.getFullYear(), date.getMonth() + 1, 1));
    } else {
      dateObj.setDate(new Date(date.getFullYear(), date.getMonth(), 1));
    }
    showCalendarData();
    checkIfSprint()
  }

  /**
   * data format
   */
  function getDateStr(date) {
    let _year = date.getFullYear();
    let _month = date.getMonth() + 1; // month start form 0
    let _d = date.getDate();

    _month = _month > 9 ? "" + _month : "0" + _month;
    _d = _d > 9 ? "" + _d : "0" + _d;
    return _year + _month + _d;
  }
})();