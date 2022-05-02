function errorMessage(parent, message, target){

    $(parent).append(`<div class="errorMessageParent alert alert-danger alert-dismissible fade show" role="alert">
                                                   <p class="errorMessage">`+message+`</p>
                                                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                               </div>`)

    if (target){
        $(target).addClass("is-invalid")
    }


}


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
            error: function(data){
                if (data.status === 403){
                    if (!$(".errorMessageParent").length) {
                        // Checks to see if an error message is already on the page, will reuse the div if it is,
                        // otherwise it creates the error message
                        $("#registerForm").append(`<div class="errorMessageParent alert alert-danger alert-dismissible fade show" role="alert" style="display: none">
                                                    <p class="errorMessage"></p>
                                                     <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                                                </div>`)
                    }
                    $(".errorMessage").text(data.responseText) // Puts the error message text into the error div.
                    $(".errorMessageParent").show();
                    $("#username").addClass("is-invalid")
                }
            },

        })

    })
})