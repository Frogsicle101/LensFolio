$(() => {

    //Jquery selectors to remove duplicity
    let editUserButton = $(".editUserButton")
    let firstname = $("#firstname")
    let middlename = $("#middlename")
    let lastname = $("#lastname")
    let nickname = $("#nickname")
    let bio = $("#bio")
    let personalPronouns = $("#personalPronouns")
    let email = $("#email")


    function populateAccountInfo() {
        $.ajax({
            url: "getUser",
            success: function (response) {
                firstname.val(response.firstName)
                middlename.val(response.middleName)
                lastname.val(response.lastName)
                nickname.val(response.nickname)
                bio.val(response.bio)
                personalPronouns.val(response.pronouns)
                email.val(response.email)
            },
            error: function (error) {
                createAlert(error.responseText, "failure")
            }
        })
    }


    function toggleEditForm() {
        let canDisable = $(".canDisable")
        canDisable.prop("disabled", !canDisable.prop("disabled"));
        let editUserSubmit = $(".editUserSubmit")
        let passwordChangeDiv = $(".passwordChangeDiv")
        editUserSubmit.slideToggle() // Show submit button
        passwordChangeDiv.slideToggle() // Show password change form
        if (editUserButton.text() === "Edit Account") { //Toggle text change
            editUserButton.text("Cancel")
        } else {
            populateAccountInfo()
            editUserButton.text("Edit Account")
        }
    }


    //On Edit Account button click
    $(editUserButton).on("click", toggleEditForm)


    //On upload photo button click
    $("#uploadPhotoButton").on("click", () => {
        location.href = "uploadImage"; // change location
    });


    // On account form submit
    $("#accountForm").on("submit", (event) => {
        event.preventDefault(); // Prevents submit
        let accountData = {
            "firstname": firstname.val(),
            "middlename": middlename.val(),
            "lastname": lastname.val(),
            "nickname": nickname.val(),
            "bio": bio.val(),
            "personalPronouns": personalPronouns.val(),
            "email": email.val()
        }

        $.ajax({
            url: "edit/details",
            type: "post",
            data: accountData,
            success:  () => {
                createAlert("Updated details successfully!", "success")
                sendNotification("userDetailsUpdate", userIdent)
                toggleEditForm()

            },
            error: function (error) {//Displays error in box on failure
                createAlert(error.responseText, "failure")
            }
        })
    })


    // On password change form submit
    $("#passwordChangeForm").on( "submit", (event) => {
        event.preventDefault()
        let data = {
            "oldPassword": $("#OldPassword").val(),
            "newPassword": $("#NewPassword").val(),
            "confirmPassword": $("#ConfirmPassword").val()
        }

        $.ajax({
            type: "post",
            data: data,
            url: "edit/password",
            success: function () {
                createAlert("Password Changed Successfully!", "success")
                toggleEditForm()
                $("#OldPassword").val('')
                $("#NewPassword").val('')
                $("#ConfirmPassword").val('')
            },
            error: function (error) { // Display errors in box on failure
                createAlert(error.responseText, "failure")
            }
        })
    })

})

function handleRoleChangeEvent(notification, action) {
    $.ajax({
        url: "getUser",
        success: function (response) {
            $("#roles").val(response.roles.join(", "))
        },
        error: function (error) {
            createAlert(error.responseText, "failure")
        }
    })
    displayRoleChangeMessage(notification, action)
}