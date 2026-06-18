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

function formatDateTime(isoString) {
    const [date, time] = isoString.split('T');
    return `${date} ${time.slice(0, 5)}`;
}

function autoGrow(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = `${textarea.scrollHeight}px`;
}

const expandedGroups = new Set();

function groupByPrefix(configs) {
    const groups = new Map();
    configs.forEach((config) => {
        const prefix = config.key.includes('.') ? config.key.split('.')[0] : '기타';
        if (!groups.has(prefix)) {
            groups.set(prefix, []);
        }
        groups.get(prefix).push(config);
    });
    return groups;
}

function renderConfigRow(config) {
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
    return row;
}

function renderList(configs) {
    configList.innerHTML = '';

    groupByPrefix(configs).forEach((groupConfigs, prefix) => {
        const isExpanded = expandedGroups.has(prefix);

        const group = document.createElement('div');
        group.className = 'settings-group';

        const header = document.createElement('button');
        header.type = 'button';
        header.className = 'settings-group-header';
        header.innerHTML = `
            <span class="settings-group-chevron">${isExpanded ? '▾' : '▸'}</span>
            <span class="settings-group-title">${escapeHtml(prefix)}</span>
            <span class="settings-group-count">${groupConfigs.length}개</span>
        `;
        header.addEventListener('click', () => {
            if (expandedGroups.has(prefix)) {
                expandedGroups.delete(prefix);
            } else {
                expandedGroups.add(prefix);
            }
            renderList(configs);
        });

        const body = document.createElement('div');
        body.className = 'settings-group-body';
        body.hidden = !isExpanded;

        const rows = groupConfigs.map((config) => renderConfigRow(config));
        rows.forEach((row) => body.appendChild(row));

        group.appendChild(header);
        group.appendChild(body);
        configList.appendChild(group);

        // body가 실제로 DOM에 붙은 뒤에 계산해야 scrollHeight가 0으로 잡히지 않는다.
        rows.forEach((row) => {
            const valueInput = row.querySelector('.value-input');
            autoGrow(valueInput);
            valueInput.addEventListener('input', () => autoGrow(valueInput));
        });
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

const INSTAGRAM_EXPIRY_WARNING_DAYS = 7;

const instagramStatusDot = document.getElementById('instagramStatusDot');
const instagramStatusMeta = document.getElementById('instagramStatusMeta');
const instagramRecheckBtn = document.getElementById('instagramRecheckBtn');
const instagramRefreshBtn = document.getElementById('instagramRefreshBtn');

function setInstagramStatus(color, message) {
    instagramStatusDot.className = `status-dot status-dot-${color}`;
    instagramStatusMeta.textContent = message;
}

async function loadInstagramStatus() {
    setInstagramStatus('gray', '확인 중...');
    let res;
    try {
        res = await fetch('/api/instagram/status');
    } catch {
        setInstagramStatus('red', '상태를 확인하지 못했습니다 (네트워크 오류).');
        return;
    }

    if (res.status === 404) {
        setInstagramStatus('red', 'instagram.api.access.token 설정이 없습니다.');
        return;
    }
    if (!res.ok) {
        setInstagramStatus('red', '상태를 확인하지 못했습니다.');
        return;
    }

    const { valid, message, lastUpdatedAt, estimatedExpiresAt } = await res.json();
    const updatedText = lastUpdatedAt ? `최종 수정 ${formatDateTime(lastUpdatedAt)}` : null;

    if (!valid) {
        setInstagramStatus('red', [message ?? '토큰이 유효하지 않습니다.', updatedText].filter(Boolean).join('\n'));
        return;
    }

    const daysLeft = Math.floor((new Date(estimatedExpiresAt).getTime() - Date.now()) / (1000 * 60 * 60 * 24));
    const estimatedText = `예상 잔여 D-${daysLeft}`;
    const detail = [estimatedText, updatedText].filter(Boolean).join('\n');

    setInstagramStatus(daysLeft <= INSTAGRAM_EXPIRY_WARNING_DAYS ? 'yellow' : 'green', detail);
}

async function handleInstagramRefresh() {
    clearError();
    instagramRefreshBtn.disabled = true;
    instagramRefreshBtn.textContent = '갱신 중...';
    try {
        const res = await fetch('/api/instagram/refresh', {
            method: 'POST',
            headers: csrfHeader(),
        });
        if (!res.ok) {
            showError('토큰 갱신 요청에 실패했습니다.');
            return;
        }
        const { valid, message } = await res.json();
        if (!valid) {
            showError(message ?? '토큰 갱신에 실패했습니다.');
        } else {
            showSuccess(message ?? '토큰을 갱신했습니다.');
        }
        await loadInstagramStatus();
    } finally {
        instagramRefreshBtn.disabled = false;
        instagramRefreshBtn.textContent = '수동 갱신';
    }
}

instagramRecheckBtn.addEventListener('click', loadInstagramStatus);
instagramRefreshBtn.addEventListener('click', handleInstagramRefresh);
loadInstagramStatus();

const MENU_API_BASE = '/api/custom-menu-items';

const menuItemList = document.getElementById('menuItemList');
const addMenuItemBtn = document.getElementById('addMenuItemBtn');
const saveMenuItemsBtn = document.getElementById('saveMenuItemsBtn');

let menuItems = [];
let tempKeyCounter = 0;

function keyOf(item) {
    return item.id != null ? `id-${item.id}` : item.tempKey;
}

function readFileIntoTextarea(fileInput, textarea) {
    fileInput.addEventListener('change', () => {
        const file = fileInput.files[0];
        if (!file) {
            return;
        }
        const reader = new FileReader();
        reader.onload = () => {
            textarea.value = reader.result;
            autoGrow(textarea);
        };
        reader.readAsText(file);
    });
}

function syncMenuItemsFromDom() {
    menuItemList.querySelectorAll('.menu-item-row').forEach((row) => {
        const item = menuItems.find((i) => keyOf(i) === row.dataset.key);
        if (!item) {
            return;
        }
        item.label = row.querySelector('.menu-label-input').value;
        item.type = row.querySelector('.menu-type-input').value;
        item.value = row.querySelector('.menu-value-input').value;
        item.enabled = row.querySelector('.menu-enabled-input').checked;
    });
}

function renderMenuItems() {
    menuItemList.innerHTML = '';

    menuItems.forEach((item, index) => {
        const key = keyOf(item);
        const isDivider = item.type === 'DIVIDER';

        const row = document.createElement('div');
        row.className = 'settings-row menu-item-row';
        row.dataset.key = key;
        row.dataset.disabledType = String(isDivider);
        row.innerHTML = `
            <div class="menu-move-buttons">
                <button type="button" class="menu-move-btn menu-move-up" title="위로 이동" ${index === 0 ? 'disabled' : ''}>▲</button>
                <button type="button" class="menu-move-btn menu-move-down" title="아래로 이동" ${index === menuItems.length - 1 ? 'disabled' : ''}>▼</button>
            </div>
            <input type="text" class="menu-label-input" placeholder="메뉴 노출명" value="${escapeHtml(item.label)}" ${isDivider ? 'disabled' : ''}>
            <select class="menu-type-input">
                <option value="URL" ${item.type === 'URL' ? 'selected' : ''}>URL</option>
                <option value="HTML" ${item.type === 'HTML' ? 'selected' : ''}>HTML</option>
                <option value="DIVIDER" ${item.type === 'DIVIDER' ? 'selected' : ''}>구분선</option>
            </select>
            <div class="menu-value-wrap">
                <textarea class="value-input menu-value-input" rows="1" placeholder="URL 또는 HTML" ${isDivider ? 'disabled' : ''}>${escapeHtml(item.value)}</textarea>
                <input type="file" accept=".html,.htm" class="menu-file-input" title="HTML 파일 업로드" style="${item.type === 'HTML' ? '' : 'display:none;'}">
            </div>
            <label class="menu-enabled-label" title="사용여부">
                <input type="checkbox" class="menu-enabled-input" ${item.enabled ? 'checked' : ''} ${isDivider ? 'disabled' : ''}>사용
            </label>
            <button type="button" class="logout-btn delete-btn">삭제</button>
        `;
        menuItemList.appendChild(row);

        const valueInput = row.querySelector('.menu-value-input');
        const fileInput = row.querySelector('.menu-file-input');
        const typeInput = row.querySelector('.menu-type-input');

        autoGrow(valueInput);
        valueInput.addEventListener('input', () => autoGrow(valueInput));
        readFileIntoTextarea(fileInput, valueInput);

        typeInput.addEventListener('change', () => {
            syncMenuItemsFromDom();
            item.type = typeInput.value;
            renderMenuItems();
        });

        row.querySelector('.delete-btn').addEventListener('click', async () => {
            clearError();
            syncMenuItemsFromDom();
            if (item.id != null) {
                const res = await fetch(`${MENU_API_BASE}/${item.id}`, {
                    method: 'DELETE',
                    headers: csrfHeader(),
                });
                if (!res.ok) {
                    showError('삭제에 실패했습니다.');
                    return;
                }
            }
            menuItems = menuItems.filter((i) => keyOf(i) !== key);
            renderMenuItems();
        });

        row.querySelector('.menu-move-up').addEventListener('click', () => {
            syncMenuItemsFromDom();
            if (index > 0) {
                [menuItems[index - 1], menuItems[index]] = [menuItems[index], menuItems[index - 1]];
                renderMenuItems();
            }
        });

        row.querySelector('.menu-move-down').addEventListener('click', () => {
            syncMenuItemsFromDom();
            if (index < menuItems.length - 1) {
                [menuItems[index], menuItems[index + 1]] = [menuItems[index + 1], menuItems[index]];
                renderMenuItems();
            }
        });
    });
}

async function loadMenuItems() {
    const res = await fetch(MENU_API_BASE);
    if (!res.ok) {
        showError('메뉴 목록을 불러오지 못했습니다.');
        return;
    }
    const items = await res.json();
    menuItems = items.map((item) => ({ ...item, tempKey: null }));
    renderMenuItems();
}

function handleAddMenuItem() {
    syncMenuItemsFromDom();
    menuItems.push({
        id: null,
        tempKey: `new-${tempKeyCounter++}`,
        label: '',
        type: 'URL',
        value: '',
        enabled: true,
    });
    renderMenuItems();
}

async function handleSaveMenuItems() {
    clearError();
    syncMenuItemsFromDom();
    const payload = menuItems.map((item) => ({
        id: item.id,
        label: item.label,
        type: item.type,
        value: item.value,
        enabled: item.enabled,
    }));

    const res = await fetch(MENU_API_BASE, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', ...csrfHeader() },
        body: JSON.stringify(payload),
    });
    if (!res.ok) {
        const body = await res.json().catch(() => null);
        showError(body?.message ?? '저장에 실패했습니다.');
        return;
    }
    showSuccess('바로가기 메뉴를 저장했습니다.');
    await loadMenuItems();
}

addMenuItemBtn.addEventListener('click', handleAddMenuItem);
saveMenuItemsBtn.addEventListener('click', handleSaveMenuItems);

loadMenuItems();

newValueInput.addEventListener('input', () => autoGrow(newValueInput));

addConfigBtn.addEventListener('click', openAddRow);
cancelAddBtn.addEventListener('click', closeAddRow);
confirmAddBtn.addEventListener('click', handleAdd);
configList.addEventListener('click', handleListClick);

loadConfigs();
