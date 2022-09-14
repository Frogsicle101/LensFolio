/**
 * Performs all the actions required to close the repository details edit form
 */
function cancelRepoEdit() {
    const parent = $("#repoSettingsContainer");
    parent.slideUp(() => {
        const editButton = $(".editRepo");
        editButton.show();
    });
}


/**
 * Event listener for clicking the edit repo button. Opens a form.
 */
$(document).on("click", ".editRepo", () => {
    const editButton = $(".editRepo");
    editButton.hide();
    editButton.tooltip("hide");
    const parent = $("#repoSettingsContainer");

    const maxProjectIdNumber = 2147483647 // Max java integer
    parent.html(
        `<form id="editRepoForm" class="marginSides1">
            <div class="mb-1">
                <label class="form-label">Repository Name (cannot be empty):</label>
                <input type="text" id="repoName" class="form-control" required minlength=1 value="${sanitise($("#groupSettingsPageRepoName").text())}">
            </div>
            <div class="mb-1">
                <label class="form-label">Project ID (must be a number):</label>
                <input type="number" id="projectId" class="form-control" required max=${maxProjectIdNumber} value="${sanitise($(".groupSettingsPageProjectId").text())}">
            </div>
            <div class="mb-1">
                <label class="form-label">Access Token (minimum 20 characters):</label>
                <input type="text" id="accessToken" class="form-control" required minlength=20 value="${sanitise($("#groupSettingsPageAccessToken").text())}">
            </div>
            <div class="mb-3 mt-3">
                <button type="submit" class="btn btn-primary">Save</button>
                <button type="button" class="btn btn-secondary cancelRepoEdit" >Cancel</button>
            </div>
        </form>`
    );
    parent.slideDown();
})


/**
 * Event listener for the cancel button on the git repo edit form.
 */
$(document).on("click", ".cancelRepoEdit", cancelRepoEdit);


/**
 * Event listener for the submit button
 */
$(document).on("submit", "#editRepoForm", function (event) {
    event.preventDefault();

    const repoData = {
        "groupId": selectedGroupId,
        "projectId": $("#projectId").val(),
        "alias": $("#repoName").val(),
        "accessToken": $("#accessToken").val()
    }

    $.ajax({
        url: "editGitRepo",
        type: "post",
        data: repoData,
        success: function () {
            createAlert("Changes submitted", "success");
            sendNotification("group", selectedGroupId, "updateGroup");
            cancelRepoEdit();
            retrieveGroupRepoInformation()
        },
        error: (error) => {
            createAlert(error.responseText, "failure")
        }
    })
})