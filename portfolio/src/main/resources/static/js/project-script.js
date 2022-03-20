let numberOfSprints = 0;

let isTeacher = true;
let projectId = 1;

/**
 * Functions to be run when page has finished loading.
 */
$(document).ready(function() {
    let user = {
        "role" : role,
        "id": id,
        "name" : name
    }
    getProject();

    $(".user-name").html(user.name)
    $(".user-role").html(user.role)
    if (user.role === "teacher") {
        isTeacher = true;
    }

    populatePage();
    if (isTeacher) {
        $(".authenticated-user").css("display", "block")
    }


})

/**
 * Populates the page.
 * Makes a GET request to server, goes through each entry in response
 * and runs addSprint() on each.
 */
function populatePage() {
    $.get("getAllSprints",{"projectId": projectId}, function (data){
        data.forEach((entry) => addSprint(entry))
    })
}

function getProject(){

    $.ajax({
        url: "getProject",
        type: "GET",
        data: {},
        async: false
    }).done(function(results) {
        projectId = results.id;
        $("#projectName").html(results.name)
        $("#project-start-date").html("Start: " + results.startDate)
        $("#project-end-date").html("End " + results.endDate)
        $("#projectDescription").html(results.description)
        $("#edit-project-form").append('<form class="p-3">\n' +
            '                    <div class = "row mb-3">\n' +
            '                        <div class="col">\n' +
            '                            <h1>Edit Project</h1>\n' +
            '                        </div>\n' +
            '\n' +
            '                    </div>\n' +
            '                    <div class="form-floating mb-3">\n' +
            '                        <input type="text" class="form-control" id="ProjectFormProjectName" placeholder="Name" value="'+results.name+'">\n' +
            '                        <label for="ProjectFormProjectName">Project Name</label>\n' +
            '                    </div>\n' +
            '\n' +
            '                    <div class="row mb-3">\n' +
            '                        <div class="col">\n' +
            '                            <label for="ProjectFormStartDate">Project Start</label>\n' +
            '                            <input type="date" id="ProjectFormStartDate" class="form-control" value="'+results.startDate+'">\n' +
            '                        </div>\n' +
            '                        <div class="col">\n' +
            '                            <label for="ProjectFormEndDate">Project End</label>\n' +
            '                            <input type="date" id="ProjectFormEndDate" class="form-control" value="'+results.endDate+'">\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '                    <div class="row mb-3">\n' +
            '                        <div class="form-floating col">\n' +
            '                            <textarea class="form-control" placeholder="Describe your project here" id="ProjectFormDescription">'+results.description+'</textarea>\n' +
            '                            <label for="ProjectFormDescription">Describe the project here</label>\n' +
            '                        </div>\n' +
            '\n' +
            '                    </div>\n' +
            '                    <div class="row mb-3">\n' +
            '                        <div class="col">\n' +
            '                            <button type="button" class="btn btn-primary" id="ProjectFormSubmit">Submit</button>\n' +
            '                            <button type="reset" class="btn btn-danger" id="ProjectFormCancel">Cancel</button>\n' +
            '                        </div>\n' +
            '                    </div>\n' +
            '\n' +
            '                </form>\n' +
            '            </div>\n' +
            '\n' +
            '            </div>')

    }).fail(function(xhr, status, error) {
        showError(xhr, status, error, "#container")
    })
}


/**
 * Event listener for Project Form submit
 */
$(document).on("click", "#ProjectFormSubmit", function () {

    let name = $("#ProjectFormProjectName").val()
    let description = $("#ProjectFormDescription").val()
    let startDate = $("#ProjectFormStartDate").val()
    let endDate = $("#ProjectFormEndDate").val()
    $.ajax({
        url: "editProject",
        type: "PUT",
        data: {"name":name, "startDate":startDate, "endDate":endDate, "description":description, "id": projectId},
    }).done(function() {
        reDisplayProject()

    }).fail(function(xhr, status, error) {
        showError(xhr, status, error, "#edit-project-form")
    })
})



/**
 * Used to reload the container that holds all the sprints.
 * Has a couple of visual things like fadeouts and fadein.
 */
function reDisplaySprints() {
    $("#sprint-container").fadeOut("fast");
    setTimeout(function () {
        $("#sprint-container").empty();
        numberOfSprints = 0;
        populatePage()
    },300)

    setTimeout(function () {
        $("#sprint-container").fadeIn("fast");
    }, 600)
}

function reDisplayProject() {
    $("#project-details").fadeOut("fast");
    setTimeout(function () {
        $("#edit-project-form").empty()
        getProject()
    },300)

    setTimeout(function () {
        $("#project-details").fadeIn("fast");
    }, 600)
}


/**
 * Listener for event where user clicks on add sprint button.
 * Sends a POST request to the server to create a default sprint.
 * Receives response from server and runs addSprint()
 */
$(document).on("click", ".project-add-sprint-button", function () {
    let sprintName = numberOfSprints + 1
    $.post("addSprint", {"name": "Sprint "+sprintName+"", "projectId": projectId}, function(data) {
        addSprint(data)
    })
})

/**
 * Listener for event where user clicks cancel button on the sprint edit form
 * Removes form, removes blocking background.
 */
$(document).on("click", "#addSprintCancelButton", function () {
    $("#sprint-form-container").remove();
    $(".background-blocker").hide();
})


/**
 * Listener for event where user submits edit form.
 * Pulls out all the information from the form inputs.
 * Packages that information up and makes a PUT request to the server for "editSprint".
 * Runs reDisplaySprints() if successful.
 */
$(document).on("click", "#sprintFormSubmitButton", function () {

    let colour = $("#sprint-color-picker").val()
    let name = $("#FormControlSprintName").val()
    let description = $("#FormControlSprintDescription").val()
    let startDate = $("#FormControlStartDate").val()
    let endDate = $("#FormControlEndDate").val()
    let id = $("#sprintId").text();

    $.ajax({
        url: "editSprint",
        type: "PUT",
        data: {"name":name, "colour":colour, "startDate":startDate, "endDate":endDate, "description":description, "id":id, "projectId":projectId},
    }).done(function() {
        $(".background-blocker").hide();
        reDisplaySprints()

    }).fail(function(xhr, status, error) {
        showError(xhr, status, error, "#sprint-form-container")
        $("#sprint-form-container").css("border","solid 2px red")
    })
})


/**
 * Event handler for clicking on sprint delete buttonsFinds the closest parent with a class of 'sprint-container',
 * adds that id to variable deletedId.
 * Sends a DELETE request to server with id of Sprint.
 * Then runs reDisplaySprints()
 */
$(document).on("click", ".deleteSprint", function () {
    let element = $(this).closest(".sprint-details-container")
    let id = element.find(".sprintId").text();
    $.ajax({
        url: "deleteSprint",
        type: "DELETE",
        data: {"id": id},
    }).done(function () {
        reDisplaySprints()
    })

});


/**
 * Event handler for when user clicks on edit sprints.
 * Shows the edit sprint form.
 * Displays the background blocker element.
 */
$(document).on("click", ".edit-sprint-button", function () {
    let sprint = $(this).closest(".sprint-details-container");
    showSprintForm(sprint);
    $(".background-blocker").show();
});

/**
 * Event listener for Project Edit button
 */
$(document).on("click", "#ProjectEditButton", function () {
    $("#edit-project-form").slideToggle()
});

$(document).on("click", "#ProjectFormCancel", function () {
    $("#edit-project-form").slideUp()
});



/**
 * Function to display errors in a readable manner and present to user.
 * @param xhr
 * @param status
 * @param error
 * @param location where to append the error.
 */
function showError(xhr, status, error, location) {

    $(location).append('<div class="alert alert-danger d-flex align-items-center justify-content-center alert-dismissible fade show alert-red" role="alert">\n' +
        '        <strong>'+xhr.responseText+'</strong>\n' +
        '        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>\n' +
        '    </div>')

}


/**
 * Appends a sprint to the sprint container.
 * @param sprint all the data of the sprint to append.
 */
function showSprintForm(sprint) {
    let sprintName = sprint.find(".sprintName").text()
    let startDate = sprint.find(".start-date-data").text()
    let endDate = sprint.find(".end-date-data").text()
    let description = sprint.find(".sprint-description-data").text()
    let colour = sprint.find(".sprint-colour-data").text()
    let id = sprint.find(".sprintId").text()

    $("#sprint-container").append('<div class="container-fluid" id="sprint-form-container">\n' +
        '                <form class="p-3 needs-validation" id="sprintForm" novalidate>\n' +
        '                    <div class="row mb-3">\n' +
        '                    <p id="sprintId" style="display: none">'+id+'</p>\n' +
        '                        <div class="col">\n' +
        '                            <h1>Edit Sprint</h1>\n' +
        '                        </div>\n' +
        '                        <div class="col">\n' +
        '                            <label for="sprint-color-picker" class="form-label">Colour picker</label>\n' +
        '                            <input type="color" name="colourPicker" class="form-control form-control-color" id="sprint-color-picker" value="'+colour+'" title="Choose your sprint colour" required>\n' +
        '                        </div>\n' +
        '\n' +
        '                    </div>\n' +
        '\n' +
        '                    <div class="form-floating mb-3">\n' +
        '                        <input type="text" name="sprintName" class="form-control" id="FormControlSprintName" placeholder="Name" value="' + sprintName +'" required>\n' +
        '                        <label for="FormControlSprintName">Sprint Name</label>\n' +

        '                    </div>\n' +
        '\n' +
        '                    <div class="row mb-3">\n' +
        '                        <div class="col">\n' +
        '                            <label for="FormControlStartDate">Start Date</label>\n' +
        '                            <input type="date" name="startDate" id="FormControlStartDate" class="form-control" value="' + startDate +'" required>\n' +
        '                        </div>\n' +
        '                        <div class="col">\n' +
        '                            <label for="FormControlEndDate">End Date</label>\n' +
        '                            <input type="date" name="endDate" id="FormControlEndDate" class="form-control" value="'+endDate+'" required>\n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '                    <div class="row mb-3">\n' +
        '                        <div class="form-floating col">\n' +
        '                            <textarea class="form-control" name="description" placeholder="Describe the sprint here" id="FormControlSprintDescription" required>'+description+'</textarea>\n' +
        '                            <label for="FormControlSprintDescription">Describe the sprint here</label>\n' +
        '                        </div>\n' +
        '\n' +
        '                    </div>\n' +
        '                    <div class="row mb-3 teacher-control-buttons-form">\n' +
        '                        <div class="col">\n' +
        '                            <button type="button" class="btn btn-outline-primary" id="sprintFormSubmitButton">Submit</button>\n' +
        '                            <button type="reset" class="btn btn-outline-danger" id="addSprintCancelButton">Cancel</button>\n' +
        '                        </div>\n' +
        '                    </div>\n' +
        '\n' +
        '                </form>\n' +
        '            </div>')

}




/**
 * Function that appends a sprint to the sprint container.
 */
function addSprint(element) {
    numberOfSprints ++;
    if (isTeacher) {
        $("#sprint-container").append('<div style="border-left-color: ' + element.colour+ '" class="sprint-details-container container-fluid  p-3" id="sprint-'+ numberOfSprints +'">\n' +
            '                <div class="row justify-content-between">\n' +
            '                <p class="sprint-colour-data" style="display: none">'+element.colour+'</p>\n' +
            '                <p class="sprintId" style="display: none">'+element.id+'</p>\n' +
            '                    <div class="col-3">\n' +
            '                        <h1 class="sprint-label">Sprint '+numberOfSprints+'</h1>\n' +
            '                    </div>\n' +
            '                    <div class="col">\n' +
            '                        <h1 class="sprintName">'+ element.name + '</h1>\n' +
            '                    </div>\n' +
            '                </p>\n' +
            '               <div class="row">\n' +
            '                   <div class="col">\n' +
            '                       <h5 >Start Date:</h5>\n' +
            '                       <h4 class="start-date-data">' + element.startDate + '</h4>\n' +
            '                   </div>\n' +
            '                   <div class="col">\n' +
            '                       <h5>End Date:</h5>\n' +
            '                       <h4 class="end-date-data">'+element.endDate+'</h4>\n' +
            '                   </div>\n' +
            '               </div>\n' +
            '               <div class="row">\n' +
            '                   <div class="col">\n' +
            '                       <p class="sprint-description-data">' + element.description+'</p>\n' +
            '                   </div>\n' +
            '               </div>\n' +
            '                <div class="row">\n' +
            '                    <div class="col">\n' +
            '                        <button type="button" class="btn btn-outline-danger deleteSprint ">Delete</button>\n' +
            '                        <button type="button" class="btn btn-outline-success edit-sprint-button ">Edit</button>\n' +
            '                    </div>\n' +
            '\n' +
            '\n' +
            '\n' +
            '                </div>\n' +
            '\n' +
            '            </div>')
    } else {
        $("#sprint-container").append('<div style="border-left-color: ' + element.colour+ '" class="sprint-details-container container-fluid  p-3" id="sprint-'+ numberOfSprints +'">\n' +
            '                <div class="row justify-content-between">\n' +
            '                <p class="sprint-colour-data" style="display: none">'+element.colour+'</p>\n' +
            '                <p class="sprintId" style="display: none">'+element.id+'</p>\n' +
            '                    <div class="col-3">\n' +
            '                        <h1 class="sprint-label">Sprint '+numberOfSprints+'</h1>\n' +
            '                    </div>\n' +
            '                    <div class="col">\n' +
            '                        <h1 class="sprintName">'+ element.name + '</h1>\n' +
            '                    </div>\n' +
            '                </p>\n' +
            '               <div class="row">\n' +
            '                   <div class="col">\n' +
            '                       <h5 >Start Date:</h5>\n' +
            '                       <h4 class="start-date-data">' + element.startDate + '</h4>\n' +
            '                   </div>\n' +
            '                   <div class="col">\n' +
            '                       <h5>End Date:</h5>\n' +
            '                       <h4 class="end-date-data">'+element.endDate+'</h4>\n' +
            '                   </div>\n' +
            '               </div>\n' +
            '               <div class="row">\n' +
            '                   <div class="col">\n' +
            '                       <p class="sprint-description-data">' + element.description+'</p>\n' +
            '                   </div>\n' +
            '               </div>\n' +
            '                </div>\n' +
            '\n' +
            '            </div>')
    }


}

/**
 *  Waits for user to select an image and then displays it as a preview.
 */
$(function () {
    $("#profileImageInput").change(function () {
        if (this.files && this.files[0]) {
            var reader = new FileReader();
            reader.onload = imageIsLoaded;
            reader.readAsDataURL(this.files[0]);
        }
    });
});

/**
 * Updates image preview when image is loaded.
 * @param event image loaded event
 */
function imageIsLoaded(event) {
    $('#profileImagePreview').attr('src', event.target.result);
}
