function csrfHeader() {
    const token = document.cookie
        .split('; ')
        .find((row) => row.startsWith('XSRF-TOKEN='))
        ?.split('=')[1];
    return { 'X-XSRF-TOKEN': token };
}