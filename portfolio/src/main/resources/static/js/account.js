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
                createAlert("Updated details successfully!", AlertTypes.Success)
                toggleEditForm()
            },
            error: function (error) {//Displays error in box on failure
                createAlert(error.responseText, AlertTypes.Failure)
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
                createAlert("Password Changed Successfully!", AlertTypes.Success)
                toggleEditForm()
                $("#OldPassword").val('')
                $("#NewPassword").val('')
                $("#ConfirmPassword").val('')
            },
            error: function (error) { // Display errors in box on failure
                createAlert(error.responseText, AlertTypes.Failure)
            }
        })
    })
})