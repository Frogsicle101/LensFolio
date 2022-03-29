let beginDateStr = $("#projectStartDate").html().toString();
let endDateStr = $("#projectEndDate").html().toString();

let startList = $("#startList").html();
let endList = $("#endList").html();
let sprintColour = $("#sprintColour").html();

let sprintIndex = 0;

beginDateStr = beginDateStr.replace("-", "");
beginDateStr = beginDateStr.replace("-", "");
endDateStr = endDateStr.replace("-", "");
endDateStr = endDateStr.replace("-", "");

//let sprintList = $("#$sprints").html();

(function () {
  $.ajax({
    url: "getProjectSprints",
    type: "GET",
    data: { projectId: 2 },
  }).done(function (obj) {
    let sprints = obj;
  });
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
   * chech it is date type: today, projectDay, sprintDay
   */
  function dateType(dateCheckStr) {
    let result = "";
    if (isDuringDate(dateCheckStr)) {
      // is project date, and keep going to check is a sprint day or not
      if (sprintIndex == startList.length) {
        //all sprint done, it is a project day, not sprint day
        result += "project-day";
      } else if (
        dateCheckStr >= startList[sprintIndex] &&
        dateCheckStr <= endList[sprintIndex]
      ) {
        // is sprint date
        result += "sprint-day";
      } else if (dateCheckStr > endList[sprintIndex]) {
        // current sprint done, next time check next sprint, it is project day not sprint day
        sprintIndex += 1;
        result += "project-day";
      } else {
        // earlier than the sprint date to be viewed, it is a project day not a sprint day
        result += "project-day";
      }
    } else {
      // is not a project day
      result += "not-project-day";
    }
    if (dateCheckStr == getDateStr(new Date())) {
      // is today
      result += "-today";
    }
    return result;
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
      _tds[i].setAttribute("data", _thisDayStr);

      // check day type
      let dayClass = dateType(_thisDayStr);
      if (dayClass.startsWith("s")) {
        //sprint day, should think about sprint line color
        let color = sprintColour[sprintIndex];
        _tds[i].className = dayClass;
      } else {
        // not sprint day
        _tds[i].className = dayClass;
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
    dateObj.setDate(new Date(date.getFullYear(), date.getMonth() - 1, 1));
    showCalendarData();
  }

  /**
   * Click the next month icon
   */
  function toNextMonth() {
    let date = dateObj.getDate();
    dateObj.setDate(new Date(date.getFullYear(), date.getMonth() + 1, 1));
    showCalendarData();
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
