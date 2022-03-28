$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()


    /**
     * When project edit button is clicked.
     * Redirect page.
     */
    $("#projectEditSprint").click(() => {
        location.href = "/editProject?projectId=" + projectId ;
    })
    /**
     * When project add sprint button is pressed.
     * Redirect page.
     */
    $("#projectAddSprint").click(function () {
        location.href = "/portfolio/addSprint?projectId=" + projectId;
    })


    /**
     * When edit sprint button is clicked.
     * Redirect page.
     */
    $(".editSprint").click(function () {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        location.href = "/sprintEdit?sprintId=" + $(this).closest(".sprint").find(".sprintId").text() +"&projectId=" + projectId;
    })

    /**
     * When sprint delete button is clicked.
     * Sends ajax delete request.
     * Then reloads page.
     */
    $(".deleteSprint").click(function() {
        let sprintId = $(this).closest(".sprint").find(".sprintId").text();
        $.ajax({
            url: "deleteSprint",
            type: "DELETE",
            data: {"sprintId": sprintId},
        }).done(function () {
            location.href = "/portfolio?projectId=" + projectId
        })
    })

    /**
     * Slide toggle for when add event button is clicked.
     */
    $(".addEventButton").click(function() {
        $(".addEventSvg").toggleClass('rotated');
        $(".eventForm").slideToggle();



    })


    /**
     * When event is submitted.
     */
    $("#eventSubmit").click(function(event) {
        event.preventDefault();
        let eventData = {
            "projectId": projectId,
            "eventName": $("#eventName").val(),
            "eventStart": $("#eventStart").val(),
            "eventEnd": $("#eventEnd").val()
        }
        if (eventData.eventName.toString().length === 0 || eventData.eventName.toString().trim().length === 0){
            $(this).closest(".eventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter an event name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
        } else if (eventData.eventEnd < eventData.eventStart) {
            $(this).closest(".eventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> Your event end date shouldn't be before your event start date!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
        } else {
            $.ajax({
                url: "/addEvent",
                type: "put",
                data: eventData,
                success: function(response) {
                    location.href = "/portfolio?projectId=" + projectId
                },
                error: function(response) {

                }
            })
        }

    })

    $(".form-control").each(countCharacters)
    $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.

    /**
     * Function that gets the maxlength of an input and lets the user know how many characters they have left.
     */
    function countCharacters() {
        let maxLength = $(this).attr("maxLength")
        let lengthOfCurrentInput = $(this).val().length;
        let counter = maxLength - lengthOfCurrentInput;
        let helper = $(this).next(".form-text"); //Gets the next div with a class that is form-text

        //If one character remains, changes from "characters remaining" to "character remaining"
        if (counter !== 1) {
            helper.text(counter + " characters remaining")
        } else {
            helper.text(counter + " character remaining")
        }

    }

    setAddSprintButtonPlacement()
    function setAddSprintButtonPlacement() {
        $(".addSprint").css("left", $(".eventContainer").width() + "px")
        $(".addSprint").css("bottom",0 -  $(".addSprintSvg").height()/2 + "px")
    }


    $(".eventEditButton").click(function() {
        let eventId = $(this).closest(".event").find(".eventId").text();
        let eventName = $(this).closest(".event").find(".eventName").text();
        let eventStart = $(this).closest(".event").find(".eventStartDateNilFormat").text().slice(0,16);
        let eventEnd = $(this).closest(".event").find(".eventEndDateNilFormat").text().slice(0,16);

        $(this).closest(".event").append('<form class="existingEventForm">\n' +
            '                    <div class="mb-1">\n' +
            '                        <label for="eventName" class="form-label">Event name</label>\n' +
            '                        <input type="text" class="form-control form-control-sm eventName" th:maxlength="${eventNameLengthRestriction}" value="'+ eventName +'" name="eventName" required>\n' +
            '                        <small class="form-text text-muted countChar">0 characters remaining</small>\n' +
            '                    </div>\n' +
            '                    <div class="row mb-1">\n' +
            '                        <div class="col">\n' +
            '                            <label for="eventStart" class="form-label">Start</label>\n' +
            '                            <input type="datetime-local" class="form-control form-control-sm eventStart" value="'+ eventStart +'" th:min="${project.getStartDateAsLocalDateTime()}" th:max="${project.getEndDateAsLocalDateTime()}" name="eventStart"  required>\n' +
            '                        </div>\n' +
            '                        <div class="col">\n' +
            '                            <label for="eventEnd" class="form-label">End</label>\n' +
            '                            <input type="datetime-local" class="form-control form-control-sm eventEnd" value="'+ eventEnd +'" th:min="${project.getStartDateAsLocalDateTime()}" th:max="${project.getEndDateAsLocalDateTime()}" name="eventEnd" required>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                    <div class="mb-1">\n' +
            '                        <button type="submit" class="btn btn-primary existingEventSubmit">Save</button>\n' +
            '                        <button type="button" class="btn btn-secondary existingEventCancel" >Cancel</button>\n' +
            '                    </div>\n' +
            '                </form>')
        $(".existingEventCancel").click(function() {
            $(this).closest(".event").find(".eventEditButton").show();
            $(this).closest(".event").find(".existingEventForm").remove();

        })
        $(this).closest(".event").find(".eventEditButton").hide();
        $(".existingEventSubmit").click(function() {
            this.preventDefault();
            let eventData = {
                "projectId": projectId,
                "eventName": $(this).closest(".event").find(".eventName").val(),
                "eventStart": $(this).closest(".event").find(".eventStart").val(),
                "eventEnd": $(this).closest(".event").find(".eventEnd").val()
            }
            $.ajax({
                url: "/editEvent",
                type: "post",
                data: eventData,
                success: function(response) {
                    location.href = "/portfolio?projectId=" + projectId
                },
                error: function(response) {

                }
            })

        })

        console.log(eventStart)

    })










})





