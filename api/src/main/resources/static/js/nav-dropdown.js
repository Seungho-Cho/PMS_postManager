document.querySelectorAll('.nav-dropdown-trigger').forEach((trigger) => {
    trigger.addEventListener('click', (event) => {
        event.stopPropagation();
        const dropdown = trigger.closest('.nav-dropdown');
        const wasOpen = dropdown.classList.contains('is-open');
        document.querySelectorAll('.nav-dropdown.is-open').forEach((d) => d.classList.remove('is-open'));
        if (!wasOpen) {
            dropdown.classList.add('is-open');
        }
    });
});

document.addEventListener('click', () => {
    document.querySelectorAll('.nav-dropdown.is-open').forEach((dropdown) => dropdown.classList.remove('is-open'));
});