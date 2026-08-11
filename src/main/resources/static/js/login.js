document.addEventListener("DOMContentLoaded", function () {

    const loginTab = document.getElementById("loginTab");
    const registerTab = document.getElementById("registerTab");

    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");

    // Tab switching: Login
    loginTab.addEventListener("click", function () {

        loginTab.classList.add("active");
        registerTab.classList.remove("active");

        loginForm.classList.remove("hidden");
        registerForm.classList.add("hidden");

    });

    // Tab switching: Register
    registerTab.addEventListener("click", function () {

        registerTab.classList.add("active");
        loginTab.classList.remove("active");

        registerForm.classList.remove("hidden");
        loginForm.classList.add("hidden");

    });

});

/* Show / hide password with Font Awesome eye icon */
function togglePassword(inputId, element) {
    const input = document.getElementById(inputId);
    const icon = element.querySelector("i");

    if (input.type === "password") {
        input.type = "text";
        icon.classList.remove("fa-eye");
        icon.classList.add("fa-eye-slash");
    } else {
        input.type = "password";
        icon.classList.remove("fa-eye-slash");
        icon.classList.add("fa-eye");
    }
}

/* Mouse light glow following cursor */
document.addEventListener("mousemove", function (e) {
    const light = document.getElementById("mouse-light");
    if (!light) return;

    light.style.left = e.clientX + "px";
    light.style.top = e.clientY + "px";
});