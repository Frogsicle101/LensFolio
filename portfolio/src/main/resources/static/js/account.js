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
    let accountForm = $("#accountForm")
    let passwordForm = $("#passwordChangeForm")

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
    accountForm.on("submit", (event) => {
        event.preventDefault(); // Prevents submit
        if (accountForm[0].checkValidity()) {
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
                    toggleEditForm()
                },
                error: function (error) {//Displays error in box on failure
                    createAlert(error.responseText, "failure")
                }
            })
        } else {
            event.stopPropagation();
            const errorElements = accountForm.find(".form-control:invalid")
            $('html, body').animate({
                scrollTop: $(errorElements[0]).offset().top - 100
            }, 50);
        }
    })


    // On password change form submit
    passwordForm.on( "submit", (event) => {
        event.preventDefault()
        if (passwordForm[0].checkValidity()) {
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
        } else {
            event.stopPropagation();
            const errorElements = passwordForm.find(".form-control:invalid")
            $('html, body').animate({
                scrollTop: $(errorElements[0]).offset().top - 100
            }, 50);
        }
    })
})