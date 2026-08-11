/* ==========================================================
   DIGITAL AUTHENTICATION SYSTEM
   Premium Landing Page - script.js
========================================================== */

document.addEventListener("DOMContentLoaded", () => {

    /* ==========================================
            CREATE ANIMATED STARS
    ========================================== */

    const stars = document.getElementById("stars");

    for (let i = 0; i < 180; i++) {

        const star = document.createElement("div");

        star.classList.add("star");

        const size = Math.random() * 3 + 1;

        star.style.width = size + "px";
        star.style.height = size + "px";

        star.style.left = Math.random() * 100 + "%";

        star.style.top = Math.random() * 100 + "%";

        star.style.animationDuration =
            (8 + Math.random() * 10) + "s";

        star.style.animationDelay =
            Math.random() * 10 + "s";

        stars.appendChild(star);
    }

    /* ==========================================
             MOUSE FOLLOW GLOW
    ========================================== */

    const glow = document.getElementById("mouse-light");

    document.addEventListener("mousemove", (e) => {

        glow.style.left = e.clientX + "px";

        glow.style.top = e.clientY + "px";

    });

    /* ==========================================
               BUTTON RIPPLE EFFECT
    ========================================== */

    const buttons = document.querySelectorAll(".continue-btn");

    buttons.forEach(button => {

        button.addEventListener("click", function (e) {

            const ripple = document.createElement("span");

            const diameter = Math.max(
                button.clientWidth,
                button.clientHeight
            );

            ripple.style.width = diameter + "px";
            ripple.style.height = diameter + "px";

            ripple.style.position = "absolute";
            ripple.style.borderRadius = "50%";

            ripple.style.background =
                "rgba(255,255,255,.35)";

            ripple.style.transform = "scale(0)";

            ripple.style.animation =
                "ripple .7s linear";

            ripple.style.left =
                (e.offsetX - diameter / 2) + "px";

            ripple.style.top =
                (e.offsetY - diameter / 2) + "px";

            button.appendChild(ripple);

            setTimeout(() => {

                ripple.remove();

            }, 700);

        });

    });

});
