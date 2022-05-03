$(document).ready(() => {


    //Jquery selectors to remove duplicity
    let editUserButton = $(".editUserButton")
    let firstname = $("#firstname")
    let middlename = $("#middlename")
    let lastname = $("#lastname")
    let nickname = $("#nickname")
    let bio = $("#bio")
    let personalPronouns = $("#personalPronouns")
    let email = $("#email")
    let errorMessageParent = $(".errorMessageParent")
    let errorMessage = $(".errorMessage")
    let errorMessageParentPassword  = $(".errorMessageParentPassword")
    let errorMessagePassword = $(".errorMessagePassword")






    //On Edit Account button click
    editUserButton.click(function() {
        $(".canDisable").prop("disabled",!$(".canDisable").prop("disabled"));
        $(".editUserSubmit").slideToggle() // Show submit button
        $(".passwordChangeDiv").slideToggle() // Show password change form
        if(editUserButton.text() === "Edit Account") { //Toggle text change
            editUserButton.text("Cancel")
        } else {
            editUserButton.text("Edit Account")
        }

    })


    //On upload photo button click
    $(".uploadPhotoButton").click(() => {
        location.href = "/uploadImage"; // change location
    });

    $(".deleteProfilePhotoButton").click(() => {
        $.ajax({
            url: "/deleteProfileImg",
            type: "DELETE",
            success: function () {
                location.reload()
            }
        })
    });



    // On account form submit
    $("#accountForm").submit(function(event){
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
            url: "/edit/details",
            type: "post",
            data: accountData,
            success: function () {
                location.href = "/account" // On success reloads page
            },
            error: function(error){//Displays error in box on failure
                errorMessage.text(error.responseText)
                errorMessageParent.slideUp()
                errorMessageParent.slideDown()


            }
        })
    })



    // On password change form submit
    $("#passwordChangeForm").submit(function(event) {
        event.preventDefault()

        let data = {
            "oldPassword": $("#OldPassword").val(),
            "newPassword" : $("#NewPassword").val(),
            "confirmPassword" : $("#ConfirmPassword").val()
        }

        $.ajax({
            type: "post",
            data: data,
            url: "/edit/password",
            success: function(){
                location.href = "/account" // Reload page on success
            },
            error: function(error){ // Display errors in box on failure
                errorMessagePassword.text(error.responseText)
                errorMessageParentPassword.slideUp()
                errorMessageParentPassword.slideDown()



            }

        })
    })




})