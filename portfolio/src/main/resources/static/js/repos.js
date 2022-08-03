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

    parent.html(
        `<form id="editRepoForm" class="marginSides1">
            <div class="mb-1">
                <label class="form-label">Repository Name:</label>
                <input type="text" class="form-control" value="${$("#groupSettingsPageRepoName").text()}">
            </div>
            <div class="mb-1">
                <label class="form-label">Project ID:</label>
                <input type="text" class="form-control" value="${$(".groupSettingsPageProjectId").text()}">
            </div>
            <div class="mb-1">
                <label class="form-label">Access Token:</label>
                <input type="text" class="form-control" value="${$(".groupSettingsPageAccessToken").text()}">
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
 * Event listener for the cancel button on the form.
 */
$(document).on("click", ".cancelRepoEdit", cancelRepoEdit);

/**
 * Detects any click outside the group settings page and closes the form.
 */
$(document).click(function(event) {
    const distance = $(event.target).closest("#groupSettingsPage").length
    if (distance < 1) {
        cancelRepoEdit()
    }
})

/**
 * Evemt listener for the submit button
 */
$(document).on("submit", "#editRepoForm", function (event) {
    event.preventDefault();
    alert("submitted");

})