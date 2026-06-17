const root = document.getElementById('postDetailRoot');
const postId = root.dataset.postId;
const API_URL = `/api/posts/${postId}`;

const errorBanner = document.getElementById('errorBanner');
const postPhotos = document.getElementById('postPhotos');
const titleInput = document.getElementById('titleInput');
const captionInput = document.getElementById('captionInput');
const saveDraftBtn = document.getElementById('saveDraftBtn');
const scheduleBtn = document.getElementById('scheduleBtn');
const deleteBtn = document.getElementById('deleteBtn');

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text ?? '';
    return div.innerHTML;
}

function showError(message) {
    errorBanner.textContent = message;
    errorBanner.style.display = 'block';
}

function clearError() {
    errorBanner.style.display = 'none';
}

function renderPhotos(photos) {
    postPhotos.innerHTML = photos
        .map((photo) => `<img src="${escapeHtml(photo.thumbnailUrl)}" alt="">`)
        .join('');
}

async function loadPost() {
    const res = await fetch(API_URL);
    if (!res.ok) {
        showError('포스트를 불러오지 못했습니다.');
        return;
    }
    const post = await res.json();
    titleInput.value = post.title ?? '';
    captionInput.value = post.caption ?? '';
    renderPhotos(post.photos);
}

async function handleSave(status) {
    clearError();
    const res = await fetch(API_URL, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...csrfHeader() },
        body: JSON.stringify({ title: titleInput.value, caption: captionInput.value, status }),
    });
    if (!res.ok) {
        showError('저장에 실패했습니다.');
        return;
    }
    window.location.href = '/admin/posts';
}

async function handleDelete() {
    clearError();
    const res = await fetch(API_URL, {
        method: 'DELETE',
        headers: csrfHeader(),
    });
    if (!res.ok) {
        showError('삭제에 실패했습니다.');
        return;
    }
    window.location.href = '/admin/posts';
}

saveDraftBtn.addEventListener('click', () => handleSave('DRAFT'));
scheduleBtn.addEventListener('click', () => handleSave('SCHEDULED'));
deleteBtn.addEventListener('click', handleDelete);

loadPost();