const API_BASE = '/api/posts';

const postGrid = document.getElementById('postGrid');
const errorBanner = document.getElementById('errorBanner');

const addPostBtn = document.getElementById('addPostBtn');
const newPostPanel = document.getElementById('newPostPanel');
const newDiscordMessageId = document.getElementById('newDiscordMessageId');
const newAuthorDiscordId = document.getElementById('newAuthorDiscordId');
const newAuthorDiscordIcon = document.getElementById('newAuthorDiscordIcon');
const newAuthorDiscordNickname = document.getElementById('newAuthorDiscordNickname');
const newTitle = document.getElementById('newTitle');
const newPhotoUrls = document.getElementById('newPhotoUrls');
const newCaption = document.getElementById('newCaption');
const confirmAddPostBtn = document.getElementById('confirmAddPostBtn');
const cancelAddPostBtn = document.getElementById('cancelAddPostBtn');

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

function formatDateTime(isoString) {
    const [date, time] = isoString.split('T');
    return `${date} ${time.slice(0, 5)}`;
}

function renderGrid(posts) {
    postGrid.innerHTML = '';
    posts.forEach((post) => {
        const card = document.createElement('div');
        card.className = 'post-card';
        card.dataset.id = post.id;
        card.dataset.authorId = post.authorDiscordId;
        card.dataset.authorNickname = post.authorDiscordNickname;

        const thumbnailUrl = post.photos[0]?.thumbnailUrl;
        const thumbHtml = thumbnailUrl
            ? `<img src="${escapeHtml(thumbnailUrl)}" alt="">`
            : `<span class="post-card-thumb-empty">No Photo</span>`;

        card.innerHTML = `
            <div class="post-card-thumb">${thumbHtml}</div>
            <div class="post-card-body">
                <span class="post-status post-status-${post.status.toLowerCase()}">${post.status}</span>
                <h3 class="post-card-title">${escapeHtml(post.title) || '(제목 없음)'}</h3>
                <p class="settings-meta">${escapeHtml(post.authorDiscordNickname)} · ${escapeHtml(formatDateTime(post.createdAt))}</p>
            </div>
            <div class="post-card-actions">
                <a href="/admin/posts/${post.id}" class="nav-link edit-link">편집</a>
                <button type="button" class="logout-btn delete-btn">삭제</button>
            </div>
        `;
        postGrid.appendChild(card);
    });
}

async function loadPosts() {
    const res = await fetch(API_BASE);
    if (!res.ok) {
        showError('포스트 목록을 불러오지 못했습니다.');
        return;
    }
    renderGrid(await res.json());
}

function openAddPanel() {
    newPostPanel.style.display = 'flex';
    newDiscordMessageId.focus();
}

function closeAddPanel() {
    newPostPanel.style.display = 'none';
    [newDiscordMessageId, newAuthorDiscordId, newAuthorDiscordIcon, newAuthorDiscordNickname, newTitle, newPhotoUrls, newCaption]
        .forEach((input) => { input.value = ''; });
}

async function handleAdd() {
    clearError();

    const photoUrls = newPhotoUrls.value
        .split(',')
        .map((url) => url.trim())
        .filter((url) => url.length > 0);

    const res = await fetch(API_BASE, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...csrfHeader() },
        body: JSON.stringify({
            discordMessageId: newDiscordMessageId.value,
            authorDiscordId: newAuthorDiscordId.value,
            authorDiscordIcon: newAuthorDiscordIcon.value,
            authorDiscordNickname: newAuthorDiscordNickname.value,
            title: newTitle.value,
            caption: newCaption.value,
            photoUrls,
        }),
    });

    if (!res.ok) {
        const body = await res.json().catch(() => null);
        showError(body?.message ?? '생성에 실패했습니다.');
        return;
    }

    closeAddPanel();
    await loadPosts();
}

function handleEditClick(event) {
    const link = event.target.closest('.edit-link');
    if (!link) {
        return;
    }
    const card = link.closest('.post-card');
    const currentDiscordId = postGrid.dataset.currentDiscordId;

    if (currentDiscordId && card.dataset.authorId && card.dataset.authorId !== currentDiscordId) {
        const confirmed = window.confirm(`${card.dataset.authorNickname}님이 작성한 포스트입니다. 편집하시겠습니까?`);
        if (!confirmed) {
            event.preventDefault();
        }
    }
}

async function handleGridClick(event) {
    handleEditClick(event);

    if (!event.target.classList.contains('delete-btn')) {
        return;
    }
    const card = event.target.closest('.post-card');
    clearError();

    const res = await fetch(`${API_BASE}/${card.dataset.id}`, {
        method: 'DELETE',
        headers: csrfHeader(),
    });
    if (!res.ok) {
        showError('삭제에 실패했습니다.');
        return;
    }
    await loadPosts();
}

addPostBtn.addEventListener('click', openAddPanel);
cancelAddPostBtn.addEventListener('click', closeAddPanel);
confirmAddPostBtn.addEventListener('click', handleAdd);
postGrid.addEventListener('click', handleGridClick);

loadPosts();