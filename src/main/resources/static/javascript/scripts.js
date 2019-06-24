function submitEvent() {

    var loginStatus = document.getElementById("logStatus").value;

    if (loginStatus === true) {
        document.getElementById("eventForm").submit();
    } else {
        // Drop the Modal

    }
}

function modalSubmit() {
    // submit from modal


    document.getElementById("eventForm").submit;
}

function endToggle() {

    var status = document.getElementById("endDTDiv");
    if (status.style.display === "none") {
        status.style.display = "block";
        button.value = "Remove End Time";
        button.innerHTML = "Remove End Time";
    } else {
        status.style.display = "none";
        button.value = "Add End Time";
        button.innerHTML = "Add End Time";
    }
}