$(document).ready(() => {

    let editUserButton = $(".editUserButton")
    let username = $("#username")
    let password = $("#password")
    let firstname = $("#firstname")
    let middlename = $("#middlename")
    let lastname = $("#lastname")
    let nickname = $("#nickname")
    let bio = $("#bio")
    let personalPronouns = $("#personalPronouns")
    let email = $("#email")

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




    $(".uploadPhotoButton").click(() => {
        location.href = "/uploadImage";
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

    $(".editPasswordButton").click(() => {
        $(".canDisablePassword").prop("disabled",!$(".canDisablePassword").prop("disabled"));
    })


    // On account form submit
    $("#accountForm").submit(function(event){
        event.preventDefault();
        let accountData = {
            "password": password.val(),
            "firstname": firstname.val(),
            "middlename": middlename.val(),
            "lastname": lastname.val(),
            "nickname": nickname.val(),
            "bio": bio.val(),
            "personalPronouns": personalPronouns.val(),
            "email": email.val()
        }
        console.log(accountData)
        $.ajax({
            url: "/edit/details",
            type: "post",
            data: accountData,
            success: function () {
                location.href = "/account"
            },
            error: function(error){
                console.log(error.responseText)
                //TODO Add in error handling here
            }
        })
    })

    $("#passwordChangeForm").submit(function(event) {
        event.preventDefault()
        //Todo, not sure if this is the best way to handle passwords, they can be seen?

        let data = {
            "oldPassword": $("#OldPassword").val(),
            "newPassword" : $("#NewPassword").val(),
            "confirmPassword" : $("#ConfirmPassword").val()
        }
        console.log(data)

        $.ajax({
            type: "post",
            data: data,
            url: "/edit/password",
            success: function(){
                location.href = "/account"
            },
            error: function(error){
                console.log(error.responseText)
                //TODO error handling

            }

        })
    })




})