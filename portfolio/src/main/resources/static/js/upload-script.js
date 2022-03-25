/**
 *  Waits for user to select an image and then displays it as a preview.
 */
$(function () {
    $("#profileImageInput").change(function () {
        if (this.files && this.files[0]) {
            let reader = new FileReader();
            reader.onload = imageIsLoaded;
            reader.readAsDataURL(this.files[0]);
        }
    });
});

/**
 * Updates image preview and file size when image is loaded.
 * @param event image loaded event
 */
function imageIsLoaded(event) {
    const imgElement = document.getElementById('profileImagePreview');
    $('#profileImagePreview').attr('src', event.target.result);
    const imgSrc = document.getElementById(imageId).attr('src');
    // Convert image to blob to get file size
    fetch(imgSrc)
        .then(function(response) {
            return response.blob()
        })
        .then(function(blob) {
            document.querySelector("#size").innerHTML = bytesToSize(blob.size);
        });

}


/**
 *  Resizes image to desired width and height. Adjusts image quality according to constant value.
 * @param imageId   ID of html <img> element to be resized
 * @param newWidth  Desired new width in pixels
 * @param newHeight Desired new height in pixels
 */
function resizeImage(imageId,newWidth, newHeight) {
    const imgToCompress = document.getElementById(imageId);
    const formDataURLField = document.getElementById("formDataURLField");
    const quality = 0.8; // A value from 0 - 1

    // resizing the image
    const canvas = document.createElement("canvas");
    const context = canvas.getContext("2d");

    const originalWidth = imgToCompress.width;
    const originalHeight = imgToCompress.height;

    const cropOffsetX = 0;
    const cropOffsetY = 0;

    canvas.width = newWidth;
    canvas.height = newHeight;

    context.drawImage(imgToCompress, cropOffsetX, cropOffsetY, newWidth, newHeight, 0, 0, newWidth, newHeight);

    // reducing the quality of the image
    canvas.toBlob(
        (blob) => {
            if (blob) {
                // showing the compressed image
                imgToCompress.src = URL.createObjectURL(blob);
                formDataURLField.setAttribute("imageURLFromJavascript", imgToCompress.src);
                document.querySelector("#size").innerHTML = bytesToSize(blob.size); // Sends image size to upload form
                imgFileBlob = blob;
            }
        },
        "image/jpeg",
        quality
    );
}

function sendImagePostRequest(imageId, clientToken) {
    const fileSrc = document.getElementById('profileImageInput');
    const fileExt = fileSrc.files[0].type;
    alert(fileExt);
    const imgSrc = document.getElementById(imageId).attr('src');
    fetch(imgSrc)
        .then(function(response) {
            return response.blob()
        })
        .then(function(blob) {
            const formdata = new FormData();
            formdata.append("image", blob);
            formdata.append("fileExt", fileExt);
        });
    fetch("http://localhost:9000/upload", {
        method: "POST",
        headers: {
            Accept: "application/json",
            Authorization: clientToken
        },
        body: formdata
    }).then((response) => {
        // Check response here
    });
}

// source: https://stackoverflow.com/a/18650828
function bytesToSize(bytes) {
    const sizes = ["Bytes", "KB", "MB", "GB", "TB"];

    if (bytes === 0) {
        return "0 Byte";
    }

    const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));

    return Math.round(bytes / Math.pow(1024, i), 2) + " " + sizes[i];
}
