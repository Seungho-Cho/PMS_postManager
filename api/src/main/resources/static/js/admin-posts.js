const API_BASE = '/api/posts';

const postGrid = document.getElementById('postGrid');
const errorBanner = document.getElementById('errorBanner');

const failureLogModal = document.getElementById('failureLogModal');
const failureLogCloseBtn = document.getElementById('failureLogCloseBtn');

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text ?? '';
    return div.innerHTML;
}

function showError(message) {
    errorBanner.textContent = message;
    errorBanner.style.display = 'block';
}

function formatDateTime(isoString) {
    const [date, time] = isoString.split('T');
    return `${date} ${time.slice(0, 5)}`;
}

const STATUS_LABELS = {
    DRAFT: '임시저장',
    SCHEDULED: '예약됨',
    POSTED: '포스팅 완료',
    FAILED: '포스팅 실패',
};

function renderStatusBadge(post) {
    const label = STATUS_LABELS[post.status] ?? post.status;
    if (post.status === 'FAILED') {
        return `<button type="button" class="post-status post-status-failed post-status-btn" data-id="${post.id}">⚠ ${label}</button>`;
    }
    return `<span class="post-status post-status-${post.status.toLowerCase()}">${label}</span>`;
}

function renderGrid(posts) {
    postGrid.innerHTML = '';
    posts.forEach((post) => {
        const row = document.createElement('div');
        row.className = 'post-row';
        row.dataset.id = post.id;

        const thumbnailUrl = post.photos[0]?.thumbnailUrl;
        const thumbHtml = thumbnailUrl
            ? `<img src="${escapeHtml(thumbnailUrl)}" alt="">`
            : `<span class="post-card-thumb-empty">No Photo</span>`;

        row.innerHTML = `
            <a href="/posts/${post.id}" class="post-row-link">
                <div class="post-row-thumb">${thumbHtml}</div>
                <div class="post-row-content">
                    <div class="post-row-top">
                        <h3 class="post-row-title">${escapeHtml(post.title) || '(제목 없음)'}</h3>
                        ${renderStatusBadge(post)}
                    </div>
                    <div class="post-row-bottom">
                        <span class="post-row-author">
                            ${post.authorDiscordIcon ? `<img class="post-row-avatar" src="${escapeHtml(post.authorDiscordIcon)}" alt="">` : '<span class="post-row-avatar"></span>'}
                            ${escapeHtml(post.authorDiscordNickname)}
                        </span>
                        <span class="post-row-time">최종수정일시 ${escapeHtml(formatDateTime(post.updatedAt))}</span>
                    </div>
                </div>
            </a>
        `;
        postGrid.appendChild(row);
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

function openFailureLogModal() {
    failureLogModal.hidden = false;
}

function closeFailureLogModal() {
    failureLogModal.hidden = true;
}

function handleGridClick(event) {
    const statusBtn = event.target.closest('.post-status-btn');
    if (statusBtn) {
        event.preventDefault();
        openFailureLogModal();
    }
}

postGrid.addEventListener('click', handleGridClick);
failureLogCloseBtn.addEventListener('click', closeFailureLogModal);
failureLogModal.addEventListener('click', (event) => {
    if (event.target === failureLogModal) {
        closeFailureLogModal();
    }
});

loadPosts();
