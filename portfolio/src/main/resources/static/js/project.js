$(document).ready(() => {
    //Gets the project Id
    const projectId = $("#projectId").html()
    getSprints(() => {
        refreshDeadlines(projectId);
        refreshMilestones(projectId);
        refreshEvents(projectId);
    });


    /**
     * When project edit button is clicked.
     * Redirect page.
     */
    $("#editProject").on("click", () => {
        location.href = "editProject?projectId=" + projectId;
    })

    /**
     * When project add sprint button is pressed.
     * Redirect page.
     */
    $("#projectAddSprint").click(function () {
        $.ajax({
            url: "portfolio/addSprint?projectId=" + projectId,
            success: function (response) {
                $(".sprintsContainer").slideUp(400, function () {
                    $(".sprintsContainer").empty()
                    getSprints(() => {
                        refreshDeadlines(projectId);
                        refreshMilestones(projectId);
                        refreshEvents(projectId);
                    })
                })
                createAlert("Sprint created!", "success")
                sendNotification("sprint", response.id, "create")
            },
            error: function (error) {
                createAlert(error.responseText, "failure")

            }
        })

    })

    $(".collapseAlert").click(function () {
        $(this).parent().slideUp();
    })

    let addSprint = $(".addSprint")
    addSprint.css("left", $(".eventContainer").width() + "px")
    addSprint.css("bottom", 0 - $(".addSprintSvg").height() / 2 + "px")
})


/**
 * When edit sprint button is clicked.
 * Redirect page.
 */
$(document).on("click", ".editSprint", function () {
    let sprintId = $(this).closest(".sprint").find(".sprintId").text();
    location.href = "sprintEdit?sprintId=" + sprintId + "&projectId=" + projectId;
})


/**
 * When sprint delete button is clicked.
 * Sends ajax delete request.
 * Then reloads page.
 */
$(document).on("click", ".deleteSprint", function () {
    let sprintId = $(this).closest(".sprint").find(".sprintId").text();
    $.ajax({
        url: "deleteSprint",
        type: "DELETE",
        data: {"sprintId": sprintId},
        success: function () {
            createAlert("Sprint deleted!", "success")
            sendNotification("sprint", sprintId, "delete")
        },
    }).done(function () {
        $(".sprintsContainer").slideUp(400, function () {
            $(".sprintsContainer").empty()
            getSprints(() => {
                refreshDeadlines(projectId);
                refreshMilestones(projectId);
                refreshEvents(projectId);
            })
        })
    })
})


function getSprints(callback = ()=>{}) {
    $.ajax({
        url: 'getSprintList',
        type: 'GET',
        data: {"projectId": projectId},
        success: function (response) {
            let sprintContainer = $(".sprintsContainer")
            for (let index in response) {
                sprintContainer.append(appendSprint(response[index], index));
            }
            sprintContainer.slideDown(400)
            removeElementIfNotAuthorized()
            callback();
        }
    })
}


function appendSprint(springObject, index) {
    index = parseInt(index) + 1

    return `
             <div class="sprint" id=${sanitise(springObject.id)} style="border-left: solid 0.3rem ${sanitise(springObject.colour)}; border-right: solid 0.3rem ${sanitise(springObject.colour)};">
                <p class="sprintColour" style="display: none">${sanitise(springObject.colour)}</p>
                <p class="sprintId" style="display: none">${sanitise(springObject.id)}</p>
                <p class="sprintStart" style="display: none">${sanitise(springObject.startDate)}</p>
                <p class="sprintEnd" style="display: none">${sanitise(springObject.endDate)}</p>
                <p class="sprintLabel" >Sprint ${sanitise(index)}</p>
                <div class="mb3">
                    <h2 class="name">${sanitise(springObject.name)}</h2>
                </div>
                <div class="row">
                    <div class="col">
                        <h6>Start</h6>
                        <h6>${sanitise(springObject.startDateFormatted)}</h6>
                    </div>
                    <div class="col">
                        <h6>End</h6>
                        <h6>${sanitise(springObject.endDateFormatted)}</h6>
                    </div>
                </div>
                
                <div class="mb-3">
                    <p class="description">${sanitise(springObject.description)}</p>
                </div>
                <div class="mb3 hasTeacherOrAbove">
                    <button type="button" class="deleteSprint noStyleButton sprintButton" data-bs-toggle="tooltip"
                            data-bs-placement="top" title="Delete Sprint">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                             class="bi bi-x-circle" viewBox="0 0 16 16">
                            <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                            <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708z"/>
                        </svg>
                    </button>
                    <button type="button" class="editSprint noStyleButton sprintButton" data-bs-toggle="tooltip"
                            data-bs-placement="top" title="Edit Sprint">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="currentColor"
                             class="bi bi-wrench-adjustable-circle" viewBox="0 0 16 16">
                            <path d="M12.496 8a4.491 4.491 0 0 1-1.703 3.526L9.497 8.5l2.959-1.11c.027.2.04.403.04.61Z"/>
                            <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0Zm-1 0a7 7 0 1 0-13.202 3.249l1.988-1.657a4.5 4.5 0 0 1 7.537-4.623L7.497 6.5l1 2.5 1.333 3.11c-.56.251-1.18.39-1.833.39a4.49 4.49 0 0 1-1.592-.29L4.747 14.2A7 7 0 0 0 15 8Zm-8.295.139a.25.25 0 0 0-.288-.376l-1.5.5.159.474.808-.27-.595.894a.25.25 0 0 0 .287.376l.808-.27-.595.894a.25.25 0 0 0 .287.376l1.5-.5-.159-.474-.808.27.596-.894a.25.25 0 0 0-.288-.376l-.808.27.596-.894Z"/>
                        </svg>
                    </button>
                </div>
            </div>`;
}


function removeElementIfNotAuthorized() {
    if (!checkPrivilege()) {
        $(".hasTeacherOrAbove").remove()
    }
}


