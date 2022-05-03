


$(document).ready(() => {
    let username = $("#username")
    let password = $("#password")
    let firstname = $("#firstname")
    let middlename = $("#middlename")
    let lastname = $("#lastname")
    let nickname = $("#nickname")
    let bio = $("#bio")
    let personalPronouns = $("#personalPronouns")
    let email = $("#email")
    let errorMessageParent = $(".errorMessageParent")
    let errorMessage = $(".errorMessage")


    $("#registerForm").submit(function(event) {
        event.preventDefault();
        let registerData = {
            "username": username.val(),
            "password": password.val(),
            "firstname": firstname.val(),
            "middlename": middlename.val(),
            "lastname": lastname.val(),
            "nickname": nickname.val(),
            "bio": bio.val(),
            "personalPronouns": personalPronouns.val(),
            "email": email.val()
        }


        $.ajax({
            url: "/register",
            type: "post",
            data: registerData,
            success: function(){
                location.href = "/account"
            },
            error: function(error){
                //TODO Add in error handling here
                errorMessage.text(error.responseText)
                errorMessageParent.slideUp()
                errorMessageParent.slideDown()
            },

        })

    })
})