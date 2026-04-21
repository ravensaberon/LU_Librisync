document.addEventListener("submit", function (event) {
    var form = event.target;
    if (!(form instanceof HTMLFormElement)) {
        return;
    }

    if (form.dataset.swalBypass === "true") {
        return;
    }

    var config = resolveConfirmationConfig(form);
    if (!config) {
        return;
    }

    event.preventDefault();

    if (window.Swal && typeof window.Swal.fire === "function") {
        window.Swal.fire({
            title: config.title,
            text: config.text,
            icon: config.icon || "warning",
            showCancelButton: true,
            confirmButtonText: config.confirmButtonText || "Continue",
            cancelButtonText: config.cancelButtonText || "Cancel",
            reverseButtons: true,
            focusCancel: true,
            confirmButtonColor: "#0f7f34",
            cancelButtonColor: "#8ca095"
        }).then(function (result) {
            if (result.isConfirmed) {
                submitConfirmedForm(form);
            }
        });
        return;
    }

    if (window.confirm(config.title + "\n\n" + config.text)) {
        submitConfirmedForm(form);
    }
}, true);

function submitConfirmedForm(form) {
    form.dataset.swalBypass = "true";
    HTMLFormElement.prototype.submit.call(form);
}

function resolveConfirmationConfig(form) {
    if (form.dataset.confirmTitle || form.dataset.confirmText) {
        return {
            title: form.dataset.confirmTitle || "Are you sure?",
            text: form.dataset.confirmText || "Please confirm this action before continuing.",
            icon: form.dataset.confirmIcon || "warning",
            confirmButtonText: form.dataset.confirmButtonText || "Yes, continue",
            cancelButtonText: form.dataset.confirmCancelText || "Cancel"
        };
    }

    var action = (form.getAttribute("action") || "").toLowerCase();

    if (action.indexOf("/logout") !== -1) {
        return {
            title: "Log out now?",
            text: "Your current session will end and you will need to sign in again to continue.",
            icon: "question",
            confirmButtonText: "Yes, log out",
            cancelButtonText: "Stay signed in"
        };
    }

    if (action.indexOf("/delete") !== -1) {
        return {
            title: "Delete this record?",
            text: "This action removes the selected data and should only be done if you are sure.",
            icon: "warning",
            confirmButtonText: "Yes, delete it",
            cancelButtonText: "Keep it"
        };
    }

    if (action.indexOf("/cancel") !== -1) {
        return {
            title: "Cancel this item?",
            text: "This will stop the current reservation or pending action.",
            icon: "warning",
            confirmButtonText: "Yes, cancel it",
            cancelButtonText: "Go back"
        };
    }

    if (action.indexOf("/return") !== -1) {
        return {
            title: "Mark this book as returned?",
            text: "The circulation record, availability, and any related queue updates will be applied.",
            icon: "question",
            confirmButtonText: "Yes, mark returned",
            cancelButtonText: "Not yet"
        };
    }

    if (action.indexOf("/password") !== -1) {
        return {
            title: "Proceed with password action?",
            text: "Please confirm before changing or resetting the password.",
            icon: "warning",
            confirmButtonText: "Yes, continue",
            cancelButtonText: "Review first"
        };
    }

    return null;
}
