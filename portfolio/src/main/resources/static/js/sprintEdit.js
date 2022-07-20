$(document).ready(function () {

    let sprintId = $("#sprintId")
    let sprintName = $("#sprintName")
    let sprintStartDate = $("#sprintStartDate")
    let sprintEndDate = $("#sprintEndDate")
    let sprintDescription = $("#sprintDescription")
    let sprintColour = $("#sprintColour")
    let dateAlert = $(".dateAlert")


    // Checks when the start date changes that its not past the end date.
    $("#sprintStartDate").on("change", function () {
        let sprintStart = $(this).val()
        let sprintEnd = $("#sprintEndDate").val()
        if (sprintStart >= sprintEnd) {
            dateAlert.slideUp()
            dateAlert.slideDown()
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
            $(this).addClass("is-invalid")
        } else {
            $(".canDisable").attr("disabled", false)
            $(this).removeClass("is-invalid")
            $(".dateAlert").slideUp()

        }
    })


    // Checks when the sprint end date changes that its not before the start date.
    $("#sprintEndDate").on("change", function () {
        let sprintStart = $("#sprintStartDate").val()
        let sprintEnd = $(this).val()
        if (sprintStart >= sprintEnd) {
            dateAlert.slideUp()
            dateAlert.slideDown()
            $(".canDisable").attr("disabled", true)
            $(this).attr("disabled", false)
            $(this).addClass("is-invalid")
        } else {
            $(".canDisable").attr("disabled", false)
            $(this).removeClass("is-invalid")
            $(".dateAlert").slideUp()

        }
    })


    // When submit button is clicked on sprint edit form
    $(".sprintEditForm").submit(function (event) {
        event.preventDefault()

        let dataToSend = {
            "sprintId": sprintId.val(),
            "sprintName": sprintName.val(),
            "sprintStartDate": sprintStartDate.val(),
            "sprintEndDate": sprintEndDate.val(),
            "sprintDescription": sprintDescription.val(),
            "sprintColour": sprintColour.val()
        }

        $.ajax({
            url: "sprintSubmit",
            type: "post",
            data: dataToSend,
            success: function () {
                sendNotification("sprint", sprintId.val(), "update")
                window.history.back();
            },
            error: function (error) {
                console.log(error.responseText)
                $(".errorMessage").text(error.responseText)
                $(".errorMessageParent").slideUp()
                $(".errorMessageParent").slideDown()
            }
        })
    })

    //Websocket Connection
    //Create, configure, and then activate the websocket connection.
    stompClient = new StompJs.Client();
    stompClient.configure({
        brokerURL: `ws://${window.location.hostname}:${window.location.port}/websocket`,
        reconnectDelay: 5000,
        debug: function (str) {
            console.log(str);
        },
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onStompError: function (frame) {
            console.log('Broker reported error: ' + frame.headers['message']);
            console.log('Additional details: ' + frame.body);
        },
        connectionTimeout: 1000
    });
    //We don't need to subscribe for anything on this page.
    stompClient.activate();
})