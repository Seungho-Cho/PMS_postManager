const themeToggleBtn = document.getElementById('themeToggleBtn');

themeToggleBtn.addEventListener('click', () => {
    const next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
});

window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (event) => {
    if (localStorage.getItem('theme')) {
        return;
    }
    document.documentElement.setAttribute('data-theme', event.matches ? 'dark' : 'light');
});