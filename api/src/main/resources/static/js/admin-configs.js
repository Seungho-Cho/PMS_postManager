const API_BASE = '/api/configs';

const configList = document.getElementById('configList');
const errorBanner = document.getElementById('errorBanner');

const addConfigBtn = document.getElementById('addConfigBtn');
const newConfigRow = document.getElementById('newConfigRow');
const newKeyInput = document.getElementById('newKeyInput');
const newValueInput = document.getElementById('newValueInput');
const confirmAddBtn = document.getElementById('confirmAddBtn');
const cancelAddBtn = document.getElementById('cancelAddBtn');

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

function autoGrow(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = `${textarea.scrollHeight}px`;
}

function renderList(configs) {
    configList.innerHTML = '';
    configs.forEach((config) => {
        const row = document.createElement('div');
        row.className = 'settings-row';
        row.dataset.key = config.key;
        row.innerHTML = `
            <div class="settings-row-label">
                <span class="settings-key">${escapeHtml(config.key)}</span>
                <span class="settings-meta">수정 ${escapeHtml(formatDateTime(config.updatedAt))}</span>
            </div>
            <textarea class="value-input" rows="1">${escapeHtml(config.value)}</textarea>
            <div class="settings-row-actions">
                <button type="button" class="save-btn">저장</button>
                <button type="button" class="logout-btn delete-btn">삭제</button>
            </div>
        `;
        configList.appendChild(row);
        const valueInput = row.querySelector('.value-input');
        autoGrow(valueInput);
        valueInput.addEventListener('input', () => autoGrow(valueInput));
    });
}

async function loadConfigs() {
    const res = await fetch(API_BASE);
    if (!res.ok) {
        showError('설정 목록을 불러오지 못했습니다.');
        return;
    }
    renderList(await res.json());
}

function openAddRow() {
    newConfigRow.style.display = 'flex';
    newKeyInput.focus();
}

function closeAddRow() {
    newConfigRow.style.display = 'none';
    newKeyInput.value = '';
    newValueInput.value = '';
}

async function handleAdd() {
    clearError();
    const key = newKeyInput.value;
    const value = newValueInput.value;

    const res = await fetch(API_BASE, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...csrfHeader() },
        body: JSON.stringify({ key, value }),
    });

    if (!res.ok) {
        const body = await res.json().catch(() => null);
        showError(body?.message ?? '추가에 실패했습니다.');
        return;
    }

    closeAddRow();
    await loadConfigs();
}

async function handleListClick(event) {
    const row = event.target.closest('.settings-row');
    if (!row) {
        return;
    }
    const key = row.dataset.key;
    clearError();

    if (event.target.classList.contains('save-btn')) {
        const value = row.querySelector('.value-input').value;
        const res = await fetch(`${API_BASE}/${encodeURIComponent(key)}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', ...csrfHeader() },
            body: JSON.stringify({ value }),
        });
        if (!res.ok) {
            const body = await res.json().catch(() => null);
            showError(body?.message ?? '저장에 실패했습니다.');
            return;
        }
        await loadConfigs();
    }

    if (event.target.classList.contains('delete-btn')) {
        const res = await fetch(`${API_BASE}/${encodeURIComponent(key)}`, {
            method: 'DELETE',
            headers: csrfHeader(),
        });
        if (!res.ok) {
            showError('삭제에 실패했습니다.');
            return;
        }
        await loadConfigs();
    }
}

async function handleBotAction(action) {
    clearError();
    const res = await fetch(`/api/discord-bot/${action}`, {
        method: 'POST',
        headers: csrfHeader(),
    });
    if (!res.ok) {
        const body = await res.json().catch(() => null);
        showError(body?.message ?? 'discord 봇 제어에 실패했습니다.');
    }
}

document.getElementById('botStartBtn').addEventListener('click', () => handleBotAction('start'));
document.getElementById('botRestartBtn').addEventListener('click', () => handleBotAction('restart'));
document.getElementById('botStopBtn').addEventListener('click', () => handleBotAction('stop'));

newValueInput.addEventListener('input', () => autoGrow(newValueInput));

addConfigBtn.addEventListener('click', openAddRow);
cancelAddBtn.addEventListener('click', closeAddRow);
confirmAddBtn.addEventListener('click', handleAdd);
configList.addEventListener('click', handleListClick);

loadConfigs();
