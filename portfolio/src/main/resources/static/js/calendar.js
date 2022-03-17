(function () {
  /*
   * Display calendar from current system time
   */
  var dateObj = (function () {
    var _date = new Date(); // current system date
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
    var calendar = document.getElementById("calendar");
    var titleBox = document.createElement("div"); // Display the current month, and change the month
    var bodyBox = document.createElement("div"); // Displays every day of the month

    // setting titleBox
    titleBox.className = "calendar-title-box";
    titleBox.innerHTML =
      "<span class='prev-month' id='prevMonth'></span>" +
      "<span class='calendar-title' id='calendarTitle'></span>" +
      "<span id='nextMonth' class='next-month'></span>";
    calendar.appendChild(titleBox); // add to calendar div

    // setting bodyBox
    bodyBox.className = "calendar-body-box";
    var _headHtml =
      "<tr>" +
      "<th>Su</th>" +
      "<th>Mo</th>" +
      "<th>Tu</th>" +
      "<th>We</th>" +
      "<th>Th</th>" +
      "<th>Fr</th>" +
      "<th>Sa</th>" +
      "</tr>";
    var _bodyHtml = "";

    // setting 6 lines for 6 weeks
    for (var i = 0; i < 6; i++) {
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
   * display numbers in table
   */
  function showCalendarData() {
    var _year = dateObj.getDate().getFullYear();
    var _month = dateObj.getDate().getMonth() + 1;
    var _dateStr = getDateStr(dateObj.getDate());

    // setting calendar title information
    var calendarTitle = document.getElementById("calendarTitle");
    var titleStr = _dateStr.substr(0, 4) + " - " + _dateStr.substr(4, 2);
    calendarTitle.innerText = titleStr;

    // setting numbers in body table
    var _table = document.getElementById("calendarTable");
    var _tds = _table.getElementsByTagName("td");
    var _firstDay = new Date(_year, _month - 1, 1); // first day in current month
    for (var i = 0; i < _tds.length; i++) {
      var _thisDay = new Date(_year, _month - 1, i + 1 - _firstDay.getDay());
      var _thisDayStr = getDateStr(_thisDay);
      _tds[i].innerText = _thisDay.getDate();
      //_tds[i].data = _thisDayStr;
      _tds[i].setAttribute("data", _thisDayStr);
      if (_thisDayStr == getDateStr(new Date())) {
        // current day
        _tds[i].className = "currentDay";
      } else if (
        _thisDayStr.substr(0, 6) == getDateStr(_firstDay).substr(0, 6)
      ) {
        _tds[i].className = "currentMonth"; // current month
      } else {
        // other month
        _tds[i].className = "otherMonth";
      }
    }
  }

  /**
   * Bind previous month to next month events
   */
  function bindEvent() {
    var prevMonth = document.getElementById("prevMonth");
    var nextMonth = document.getElementById("nextMonth");
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
    var date = dateObj.getDate();
    dateObj.setDate(new Date(date.getFullYear(), date.getMonth() - 1, 1));
    showCalendarData();
  }

  /**
   * Click the next month icon
   */
  function toNextMonth() {
    var date = dateObj.getDate();
    dateObj.setDate(new Date(date.getFullYear(), date.getMonth() + 1, 1));
    showCalendarData();
  }

  /**
   * data format
   */
  function getDateStr(date) {
    var _year = date.getFullYear();
    var _month = date.getMonth() + 1; // month start form 0
    var _d = date.getDate();

    _month = _month > 9 ? "" + _month : "0" + _month;
    _d = _d > 9 ? "" + _d : "0" + _d;
    return _year + _month + _d;
  }
})();
