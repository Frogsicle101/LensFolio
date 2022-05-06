$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()


    /**
     * When project edit button is clicked.
     * Redirect page.
     */
    $("#projectEditSprint").click(() => {
        location.href = "/editProject?projectId=" + projectId;
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
        location.href = "/sprintEdit?sprintId=" + sprintId + "&projectId=" + projectId;
    })


    /**
     * When sprint delete button is clicked.
     * Sends ajax delete request.
     * Then reloads page.
     */
    $(".deleteSprint").click(function () {
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
    $(".addEventButton").click(function () {
        $(".addEventSvg").toggleClass('rotated');
        $(".eventForm").slideToggle();
    })

    /**
     * Slide toggle for when add milestone button is clicked.
     */
    $(".addMilestoneButton").click(function () {
        $(".addMilestoneSvg").toggleClass('rotated');
        $(".milestoneForm").slideToggle();
    })

    /**
     * When milestone is submitted.
     */
    $("#milestoneSubmit").click(function (event) {
        event.preventDefault();
        let milestoneData = {
            "projectId": projectId,
            "milestoneName": $("#milestoneName").val(),
            "milestoneDate": $("#milestoneEnd").val(),
            "typeOfMilestone": $(".typeOfMilestone").val()
        }

        console.log(projectId)
        console.log(milestoneData.milestoneName)
        console.log(milestoneData.milestoneDate)
        console.log(milestoneData.typeOfMilestone)

        if (milestoneData.milestoneName.toString().length === 0 || milestoneData.milestoneName.toString().trim().length === 0) {
            $(this).closest(".milestoneForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter a milestone name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
        } else {
            $.ajax({
                url: "/addMilestone",
                type: "put",
                data: milestoneData,
                success: function (response) {
                    location.href = "/portfolio?projectId=" + projectId
                }
            })
        }
    })


    /**
     * When event is submitted.
     */
    $("#eventSubmit").click(function (event) {
        event.preventDefault();
        let eventData = {
            "projectId": projectId,
            "eventName": $("#eventName").val(),
            "eventStart": $("#eventStart").val(),
            "eventEnd": $("#eventEnd").val(),
            "typeOfEvent": $(".typeOfEvent").val()
        }

        console.log(eventData.typeOfEvent)
        if (eventData.eventName.toString().length === 0 || eventData.eventName.toString().trim().length === 0) {
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
                success: function (response) {
                    location.href = "/portfolio?projectId=" + projectId
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
        $(".addSprint").css("bottom", 0 - $(".addSprintSvg").height() / 2 + "px")
    }


    $(".eventEditButton").click(function () {
        let eventId = $(this).closest(".occasion").find(".eventId").text();
        let eventName = $(this).closest(".occasion").find(".eventName").text();
        let eventStart = $(this).closest(".occasion").find(".eventStartDateNilFormat").text().slice(0, 16);
        let eventEnd = $(this).closest(".occasion").find(".eventEndDateNilFormat").text().slice(0, 16);
        let typeOfEvent = $(this).closest(".occasion").find(".typeOfEvent").text()

        $(this).closest(".occasion").append(`
                <form class="existingEventForm">
                        <div class="mb-1">
                        <label for="eventName" class="form-label">Event name</label>
                        <input type="text" class="form-control form-control-sm eventName" value="` + eventName + `" maxLength="` + occasionNameLengthRestriction + `" name="eventName" required>
                        <small class="form-text text-muted countChar">0 characters remaining</small>
                    </div>
                    <div class="mb-3">
                        <label for="exampleFormControlInput1" class="form-label">Type of event</label>
                        <select class="form-select typeOfEvent" id="exampleFormControlInput1">
                            <option value="1">Event</option>
                            <option value="2">Test</option>
                            <option value="3">Meeting</option>
                            <option value="4">Workshop</option>
                            <option value="5">Special Event</option>
                            <option value="6">Attention Required</option>
                        </select>
                    </div>
                    <div class="row mb-1">
                        <div class="col">
                            <label for="eventStart" class="form-label">Start</label>
                            <input type="datetime-local" class="form-control form-control-sm eventInputStartDate eventStart" value="` + eventStart + `" min="` + projectStart + `" max="` + projectEnd + `" name="eventStart" required>
                        </div>
                        <div class="col">
                            <label for="eventEnd" class="form-label">End</label>
                            <input type="datetime-local" class="form-control form-control-sm eventInputEndDate eventEnd" value="` + eventEnd + `" min="` + projectStart + `" max="` + projectEnd + `" name="eventEnd" required>
                        </div>
                    </div>
                    <div class="mb-1">
                        <button type="button" class="btn btn-primary existingEventSubmit">Save</button>
                        <button type="button" class="btn btn-secondary existingEventCancel" >Cancel</button>
                    </div>
                </form>`)


        $(".existingEventCancel").click(function () {
            $(this).closest(".occasion").find(".eventEditButton").show();
            $(this).closest(".occasion").find(".existingEventForm").remove();

        })
        $(".form-control").each(countCharacters)
        $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
        $(this).closest(".occasion").find(".eventEditButton").hide();
        $(this).closest(".occasion").find(".existingEventForm").find(".typeOfEvent").val(typeOfEvent)

        $(".existingEventSubmit").click(function () {
            let eventData = {
                "projectId": projectId,
                "eventId": eventId,
                "eventName": $(this).closest(".existingEventForm").find(".eventName").val(),
                "eventStart": $(this).closest(".existingEventForm").find(".eventStart").val(),
                "eventEnd": $(this).closest(".existingEventForm").find(".eventEnd").val(),
                "typeOfEvent": $(this).closest(".existingEventForm").find(".typeOfEvent").val()
            }
            if (eventData.eventName.toString().length === 0 || eventData.eventName.toString().trim().length === 0) {
                $(this).closest(".existingEventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter an event name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            } else if (eventData.eventEnd < eventData.eventStart) {
                $(this).closest(".existingEventForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> Your event end date shouldn't be before your event start date!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            } else {
                $.ajax({
                    url: "/editEvent",
                    type: "POST",
                    data: eventData,
                    success: function (response) {
                        location.reload()
                    }
                })
            }
        })
    })

    $(".eventDeleteButton").click(function () {
        let eventData = {"eventId": $(this).closest(".occasion").find(".eventId").text()}
        $.ajax({
            url: "/deleteEvent",
            type: "DELETE",
            data: eventData,
            success: function (response) {
                location.reload()
            }
        })
    })

//////////////////////////////// milestones ///////////////////////////////
    $(".milestoneEditButton").click(function () {
        let milestoneId = $(this).closest(".occasion").find(".milestoneId").text();
        let milestoneName = $(this).closest(".occasion").find(".milestoneName").text();
        let milestoneEnd = $(this).closest(".occasion").find(".milestoneEndDateNilFormat").text();
        let typeOfMilestone = $(this).closest(".occasion").find(".typeOfMilestone").text()
        console.log(milestoneEnd);


        $(this).closest(".occasion").append(`
                <form class="existingMilestoneForm">
                        <div class="mb-1">
                        <label for="milestoneName" class="form-label">Milestone name</label>
                        <input type="text" class="form-control form-control-sm milestoneName" value="` + milestoneName + `" maxlength="` + occasionNameLengthRestriction + `" name="milestoneName" required>
                        <small class="form-text text-muted countChar">0 characters remaining</small>
                    </div>
                    <div class="mb-3">
                        <label for="exampleFormControlInput2" class="form-label">Type of milestone</label>
                        <select class="form-select typeOfMilestone" id="exampleFormControlInput2">
                            <option value="1">Event</option>
                            <option value="2">Test</option>
                            <option value="3">Meeting</option>
                            <option value="4">Workshop</option>
                            <option value="5">Special Event</option>
                            <option value="6">Attention Required</option>
                        </select>
                    </div>
                    <div class="row mb-1">
                        <div class="col">
                            <label for="milestoneEnd" class="form-label">End</label>
                            <input type="date" class="form-control form-control-sm milestoneInputEndDate milestoneEnd" value="` + milestoneEnd + `" min="` + projectStart + `" max="` + projectEnd + `" name="milestoneEnd" required>
                        </div>
                    </div>
                    <div class="mb-1">
                        <button type="button" class="btn btn-primary existingMilestoneSubmit">Save</button>
                        <button type="button" class="btn btn-secondary existingMilestoneCancel" >Cancel</button>
                    </div>
                </form>`)


        $(".existingMilestoneCancel").click(function () {
            $(this).closest(".occasion").find(".milestoneEditButton").show();
            $(this).closest(".occasion").find(".existingMilestoneForm").remove();

        })
        $(".form-control").each(countCharacters)
        $(".form-control").keyup(countCharacters) //Runs when key is pressed (well released) on form-control elements.
        $(this).closest(".occasion").find(".milestoneEditButton").hide();
        $(this).closest(".occasion").find(".existingMilestoneForm").find(".typeOfMilestone").val(typeOfMilestone)

        $(".existingMilestoneSubmit").click(function () {
            let milestoneData = {
                "projectId": projectId,
                "milestoneId": milestoneId,
                "milestoneName": $(this).closest(".existingMilestoneForm").find(".milestoneName").val(),
                "milestoneDate": $(this).closest(".existingMilestoneForm").find(".milestoneEnd").val(),
                "typeOfMilestone": $(this).closest(".existingMilestoneForm").find(".typeOfMilestone").val()
            }
            console.log(typeOfMilestone)
            if (milestoneData.milestoneName.toString().length === 0 || milestoneData.milestoneName.toString().trim().length === 0) {
                $(this).closest(".existingMilestoneForm").append(`
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <strong>Oh no!</strong> You probably should enter a milestone name!
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>`)
            } else {
                $.ajax({
                    url: "/editMilestone",
                    type: "POST",
                    data: milestoneData,
                    success: function (response) {
                        location.reload()
                    }
                })
            }
        })

    })

    $(".milestoneDeleteButton").click(function () {
        let milestoneData = {"milestoneId": $(this).closest(".occasion").find(".milestoneId").text()}
        $.ajax({
            url: "/deleteMilestone",
            type: "DELETE",
            data: milestoneData,
            success: function (response) {
                location.reload()
            }
        })
    })
})





