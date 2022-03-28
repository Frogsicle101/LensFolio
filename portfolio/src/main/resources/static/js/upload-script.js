/**
 * Creates a new Image object and waits for it to load before continuing
 * @param imageUrl src url of new image object ot be created
 * @returns {Promise<*>}
 */
async function loadImage(imageUrl) {
    let img;
    const imageLoadPromise = new Promise(resolve => {
        img = new Image();
        img.onload = resolve;
        img.src = imageUrl;
    });

    await imageLoadPromise;
    return img

}

/**
 *  Resizes image to desired width and height. Adjusts image quality according to constant value.
 */
async function processImage() {

    // Get elements from HTML page
    const previewImage = document.getElementById('profileImagePreview');
    const fileUploadInput = document.getElementById('profileImageInput');

    // Create canvas and context objects
    const canvas = document.createElement("canvas");
    const context = canvas.getContext("2d");

    // Set constants
    const uploadImageDataURL = URL.createObjectURL(fileUploadInput.files[0]);
    const uploadImageObject = await loadImage(uploadImageDataURL);
    const originalWidth = uploadImageObject.width;
    const originalHeight = uploadImageObject.height;
    const maxFileSize = 5000000; // 5MB

    // Initialize Variables
    let newHeight;
    let newWidth;
    let imageSize = fileUploadInput.files[0].size;
    let quality;

    // Calculate quality value needed
    if (imageSize <= maxFileSize) {
        quality = 0.9;
    } else {
        quality = maxFileSize/imageSize;
    }

    // Find smaller dimension of image for making square
    if (originalWidth <= originalHeight) {
        newHeight = originalWidth;
        newWidth = originalWidth;
    } else {
        newHeight = originalHeight;
        newWidth = originalHeight;
    }

    // Resizing the image
    const cropOffsetX = 0;
    const cropOffsetY = 0;

    canvas.width = newWidth;
    canvas.height = newHeight;

    // Image with new parameters is drawn using canvas object
    context.drawImage(uploadImageObject, cropOffsetX, cropOffsetY, newWidth, newHeight, 0, 0, newWidth, newHeight);

    // Compressing the image and converting to jpeg using a blob object
    canvas.toBlob(
        (blob) => {
            if (blob) {
                // showing the compressed image
                previewImage.src = URL.createObjectURL(blob);
                document.querySelector("#size").innerHTML = bytesToSize(blob.size); // Sends image size to upload form
            }
        },
        "image/jpeg",
        quality
    );
}

async function sendImagePostRequest() {
    const url = document.getElementById('profileImagePreview').getAttribute('src');
    const formData = new FormData();
    formData.append("image", await fetch(url).then(r => r.blob()));

    await fetch("http://localhost:9000/upload", {
        method: "POST",
        body: formData
    });
    location.reload();



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
