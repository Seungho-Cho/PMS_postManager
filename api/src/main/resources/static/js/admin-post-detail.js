const root = document.getElementById('postDetailRoot');
const postId = root.dataset.postId;
const API_URL = `/api/posts/${postId}`;

const errorBanner = document.getElementById('errorBanner');
const postPhotos = document.getElementById('postPhotos');
const titleInput = document.getElementById('titleInput');
const captionInput = document.getElementById('captionInput');
const makerNameInput = document.getElementById('makerNameInput');
const makerInstagramIdInput = document.getElementById('makerInstagramIdInput');
const makerXIdInput = document.getElementById('makerXIdInput');
const saveDraftBtn = document.getElementById('saveDraftBtn');
const scheduleBtn = document.getElementById('scheduleBtn');
const deleteBtn = document.getElementById('deleteBtn');
const suggestTagsBtn = document.getElementById('suggestTagsBtn');

const previewBtn = document.getElementById('previewBtn');
const previewModal = document.getElementById('previewModal');
const previewCloseBtn = document.getElementById('previewCloseBtn');
const previewTabs = document.querySelectorAll('.preview-tab');
const previewBody = document.getElementById('previewBody');

let currentPhotos = [];
let activePreviewPlatform = 'instagram';
let activePhotoIndex = 0;

function autoGrowCaption() {
    captionInput.style.height = 'auto';
    captionInput.style.height = `${captionInput.scrollHeight}px`;
}

captionInput.addEventListener('input', autoGrowCaption);

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text ?? '';
    return div.innerHTML;
}

function showError(message) {
    errorBanner.classList.remove('is-success');
    errorBanner.textContent = message;
    errorBanner.style.display = 'block';
}

function showSuccess(message) {
    errorBanner.classList.add('is-success');
    errorBanner.textContent = message;
    errorBanner.style.display = 'block';
}

function clearError() {
    errorBanner.classList.remove('is-success');
    errorBanner.style.display = 'none';
}

function renderPhotos(photos) {
    currentPhotos = photos;
    postPhotos.innerHTML = photos
        .map((photo) => `<img src="${escapeHtml(photo.thumbnailUrl)}" alt="">`)
        .join('');
}

async function fetchFormattedCaption(platform) {
    const res = await fetch('/api/posts/preview-caption', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...csrfHeader() },
        body: JSON.stringify({
            title: titleInput.value,
            caption: captionInput.value,
            makerName: makerNameInput.value,
            makerInstagramId: makerInstagramIdInput.value,
            makerXId: makerXIdInput.value,
            platform: platform.toUpperCase(),
        }),
    });
    if (!res.ok) {
        return captionInput.value;
    }
    const body = await res.json();
    return body.text;
}

function renderCarousel(aspectClass) {
    if (currentPhotos.length === 0) {
        return `<div class="preview-carousel ${aspectClass} preview-carousel-empty"><span class="ig-preview-image-empty">사진 없음</span></div>`;
    }

    const slides = currentPhotos
        .map((photo) => `<div class="preview-carousel-slide"><img src="${escapeHtml(photo.thumbnailUrl)}" alt=""></div>`)
        .join('');

    const arrows = currentPhotos.length > 1
        ? `<button type="button" class="preview-carousel-arrow preview-carousel-prev" data-dir="-1" aria-label="이전 사진">‹</button>
           <button type="button" class="preview-carousel-arrow preview-carousel-next" data-dir="1" aria-label="다음 사진">›</button>`
        : '';

    const dots = currentPhotos.length > 1
        ? `<div class="preview-carousel-dots">${currentPhotos
            .map((_, i) => `<button type="button" class="preview-carousel-dot${i === activePhotoIndex ? ' is-active' : ''}" data-index="${i}" aria-label="${i + 1}번째 사진"></button>`)
            .join('')}</div>`
        : '';

    return `
        <div class="preview-carousel ${aspectClass}">
            <div class="preview-carousel-track" style="transform: translateX(-${activePhotoIndex * 100}%)">
                ${slides}
            </div>
            ${arrows}
            ${dots}
        </div>
    `;
}

function moveCarousel(direction) {
    if (currentPhotos.length === 0) {
        return;
    }
    activePhotoIndex = (activePhotoIndex + direction + currentPhotos.length) % currentPhotos.length;
    renderPreview();
}

function attachCarouselEvents() {
    previewBody.querySelectorAll('.preview-carousel-dot').forEach((dot) => {
        dot.addEventListener('click', () => {
            activePhotoIndex = Number(dot.dataset.index);
            renderPreview();
        });
    });
    previewBody.querySelectorAll('.preview-carousel-arrow').forEach((arrow) => {
        arrow.addEventListener('click', () => moveCarousel(Number(arrow.dataset.dir)));
    });

    const track = previewBody.querySelector('.preview-carousel-track');
    if (!track) {
        return;
    }
    let touchStartX = null;
    track.addEventListener('touchstart', (event) => {
        touchStartX = event.touches[0].clientX;
    }, { passive: true });
    track.addEventListener('touchend', (event) => {
        if (touchStartX === null) {
            return;
        }
        const deltaX = event.changedTouches[0].clientX - touchStartX;
        touchStartX = null;
        if (Math.abs(deltaX) > 40) {
            moveCarousel(deltaX < 0 ? 1 : -1);
        }
    });
}

function linkifyTags(html) {
    return html
        .replace(/(^|[\s>])@([^\s<]+)/g, (_, prefix, handle) => `${prefix}<span class="preview-tag">@${handle}</span>`)
        .replace(/(^|[\s>])#([^\s<]+)/g, (_, prefix, tag) => `${prefix}<span class="preview-tag">#${tag}</span>`);
}

function renderInstagramPreview(formattedCaption) {
    const caption = linkifyTags(escapeHtml(formattedCaption).replace(/\n/g, '<br>'));
    return `
        <div class="ig-preview">
            <div class="ig-preview-header">
                <div class="ig-preview-avatar"></div>
                <span class="ig-preview-username">team_plamason</span>
            </div>
            ${renderCarousel('ig-preview-image')}
            <div class="ig-preview-actions">
                <span>♡</span><span>⤳</span><span>🔖</span>
            </div>
            <div class="ig-preview-caption">${caption || '<span class="preview-empty">캡션을 입력하면 여기에 표시됩니다.</span>'}</div>
        </div>
    `;
}

function renderXPreview(formattedCaption) {
    const caption = linkifyTags(escapeHtml(formattedCaption).replace(/\n/g, '<br>'));
    return `
        <div class="x-preview">
            <div class="x-preview-header">
                <div class="x-preview-avatar"></div>
                <div class="x-preview-meta">
                    <strong>PostManager</strong>
                    <span class="x-preview-handle">@team_plamason</span>
                </div>
            </div>
            <div class="x-preview-text">${caption || '<span class="preview-empty">캡션을 입력하면 여기에 표시됩니다.</span>'}</div>
            ${renderCarousel('x-preview-image')}
            <div class="x-preview-actions">
                <span>💬</span><span>🔁</span><span>♡</span><span>📊</span>
            </div>
        </div>
    `;
}

async function renderPreview() {
    const formattedCaption = await fetchFormattedCaption(activePreviewPlatform);
    previewBody.innerHTML = activePreviewPlatform === 'instagram'
        ? renderInstagramPreview(formattedCaption)
        : renderXPreview(formattedCaption);
    attachCarouselEvents();
}

function openPreview() {
    activePhotoIndex = 0;
    renderPreview();
    previewModal.hidden = false;
}

function closePreview() {
    previewModal.hidden = true;
}

async function loadPost() {
    const res = await fetch(API_URL);
    if (!res.ok) {
        showError('포스트를 불러오지 못했습니다.');
        return;
    }
    const post = await res.json();

    const currentDiscordId = root.dataset.currentDiscordId;
    if (currentDiscordId && post.authorDiscordId && post.authorDiscordId !== currentDiscordId) {
        const confirmed = window.confirm(`${post.authorDiscordNickname}님이 작성한 포스트입니다. 수정하시겠습니까?`);
        if (!confirmed) {
            window.location.href = '/posts';
            return;
        }
    }

    titleInput.value = post.title ?? '';
    captionInput.value = post.caption ?? '';
    makerNameInput.value = post.makerName ?? '';
    makerInstagramIdInput.value = post.makerInstagramId ?? '';
    makerXIdInput.value = post.makerXId ?? '';
    renderPhotos(post.photos);
    autoGrowCaption();
}

async function handleSave(status) {
    clearError();
    const res = await fetch(API_URL, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...csrfHeader() },
        body: JSON.stringify({
            title: titleInput.value,
            caption: captionInput.value,
            makerName: makerNameInput.value,
            makerInstagramId: makerInstagramIdInput.value,
            makerXId: makerXIdInput.value,
            status,
        }),
    });
    if (!res.ok) {
        showError('저장에 실패했습니다.');
        return;
    }
    if (status === 'DRAFT') {
        showSuccess('임시저장했습니다.');
        return;
    }
    window.location.href = '/posts';
}

const SUGGEST_TAGS_COOLDOWN_MS = 5000;

function startSuggestTagsCooldown() {
    let remainingMs = SUGGEST_TAGS_COOLDOWN_MS;
    suggestTagsBtn.disabled = true;
    suggestTagsBtn.textContent = `${Math.ceil(remainingMs / 1000)}초 후 가능`;

    const intervalId = setInterval(() => {
        remainingMs -= 1000;
        if (remainingMs <= 0) {
            clearInterval(intervalId);
            suggestTagsBtn.disabled = false;
            suggestTagsBtn.textContent = '✨ AI 태그 추천';
            return;
        }
        suggestTagsBtn.textContent = `${Math.ceil(remainingMs / 1000)}초 후 가능`;
    }, 1000);
}

async function handleSuggestTags() {
    clearError();
    suggestTagsBtn.disabled = true;
    suggestTagsBtn.textContent = '추천 중...';
    try {
        const res = await fetch('/api/posts/suggest-tags', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...csrfHeader() },
            body: JSON.stringify({
                title: titleInput.value,
                caption: captionInput.value,
            }),
        });
        if (!res.ok) {
            showError('태그 추천에 실패했습니다.');
            return;
        }
        const body = await res.json();
        const tagLine = body.tags.join(' ');
        captionInput.value = captionInput.value.trim()
            ? `${captionInput.value.trim()}\n\n${tagLine}`
            : tagLine;
        autoGrowCaption();
    } finally {
        startSuggestTagsCooldown();
    }
}

function hasHashtag(text) {
    return /#\S+/.test(text);
}

function handleSchedule() {
    if (!hasHashtag(captionInput.value)) {
        const proceedAnyway = window.confirm(
            '본문에 태그(#)가 없습니다. "✨ AI 태그 추천" 버튼으로 태그를 추가하는 것을 추천합니다.\n태그 없이 예약을 진행하시겠습니까?'
        );
        if (!proceedAnyway) {
            return;
        }
    }
    handleSave('SCHEDULED');
}

async function handleDelete() {
    if (!window.confirm('이 포스트를 삭제하시겠습니까? 삭제하면 되돌릴 수 없습니다.')) {
        return;
    }
    clearError();
    const res = await fetch(API_URL, {
        method: 'DELETE',
        headers: csrfHeader(),
    });
    if (!res.ok) {
        showError('삭제에 실패했습니다.');
        return;
    }
    window.location.href = '/posts';
}

previewBtn.addEventListener('click', openPreview);
previewCloseBtn.addEventListener('click', closePreview);
previewModal.addEventListener('click', (event) => {
    if (event.target === previewModal) {
        closePreview();
    }
});
previewTabs.forEach((tab) => {
    tab.addEventListener('click', () => {
        previewTabs.forEach((t) => t.classList.remove('is-active'));
        tab.classList.add('is-active');
        activePreviewPlatform = tab.dataset.platform;
        activePhotoIndex = 0;
        renderPreview();
    });
});

suggestTagsBtn.addEventListener('click', handleSuggestTags);
saveDraftBtn.addEventListener('click', () => handleSave('DRAFT'));
scheduleBtn.addEventListener('click', handleSchedule);
deleteBtn.addEventListener('click', handleDelete);

loadPost();