(() => {
  const DEFAULT_CONFIG = {
    apiBase: '/ai',
    chatPath: '/chat/',
    clearPath: '/chat/memory',
    memoryId: 'ai-helper-default',
    medicationTimes: ''
  };

  const STORAGE_KEYS = {
    apiBase: 'aiChat.apiBase',
    chatPath: 'aiChat.chatPath',
    clearPath: 'aiChat.clearPath',
    memoryId: 'aiChat.memoryId',
    medicationTimes: 'aiChat.medicationTimes'
  };

  const state = {
    messages: [],
    config: { ...DEFAULT_CONFIG },
    loading: false
  };

  const $ = (id) => document.getElementById(id);

  const elements = {
    connectionStatus: $('connectionStatus'),
    currentMemoryLabel: $('currentMemoryLabel'),
    apiBaseInput: $('apiBaseInput'),
    chatPathInput: $('chatPathInput'),
    clearPathInput: $('clearPathInput'),
    memoryIdInput: $('memoryIdInput'),
    medicationTimesInput: $('medicationTimesInput'),
    saveConfigBtn: $('saveConfigBtn'),
    saveMemoryBtn: $('saveMemoryBtn'),
    clearMemoryBtn: $('clearMemoryBtn'),
    resetChatBtn: $('resetChatBtn'),
    messageList: $('messageList'),
    messageCount: $('messageCount'),
    messageInput: $('messageInput'),
    sendBtn: $('sendBtn'),
    sampleBtn: $('sampleBtn'),
    requestState: $('requestState'),
    lastResponseState: $('lastResponseState'),
    requestHint: $('requestHint')
  };

  function readStorage(key, fallback) {
    const value = localStorage.getItem(key);
    return value === null || value === '' ? fallback : value;
  }

  function saveStorage(key, value) {
    localStorage.setItem(key, value);
  }

  function escapeHtml(text) {
    return String(text)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function formatTime(value = new Date()) {
    const date = value instanceof Date ? value : new Date(value);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  function setStatus(text, tone = 'idle') {
    elements.connectionStatus.textContent = text;
    elements.connectionStatus.className = `status-pill status-pill--${tone}`;
  }

  function setRequestState(text) {
    elements.requestState.textContent = text;
  }

  function setLastResponseState(text) {
    elements.lastResponseState.textContent = text;
  }

  function updateUiConfigSummary() {
    elements.currentMemoryLabel.textContent = `memoryId: ${state.config.memoryId}`;
    const chatUrl = buildUrl(state.config.chatPath, {
      message: '...',
      medicationTimes: state.config.medicationTimes
    }, true);
    elements.requestHint.textContent = `GET ${chatUrl.pathname}${chatUrl.search}`;
  }

  function syncConfigFromInputs() {
    state.config.apiBase = normalizePath(elements.apiBaseInput.value || DEFAULT_CONFIG.apiBase);
    state.config.chatPath = normalizePath(elements.chatPathInput.value || DEFAULT_CONFIG.chatPath);
    state.config.clearPath = normalizePath(elements.clearPathInput.value || DEFAULT_CONFIG.clearPath);
    state.config.memoryId = (elements.memoryIdInput.value || DEFAULT_CONFIG.memoryId).trim() || DEFAULT_CONFIG.memoryId;
    state.config.medicationTimes = (elements.medicationTimesInput.value || '').trim();
    updateUiConfigSummary();
  }

  function normalizePath(path, keepTrailingSlash = false) {
    let result = String(path || '').trim();
    if (!result) return '';
    if (!result.startsWith('/')) result = `/${result}`;
    result = result.replace(/\/+/g, '/');
    if (keepTrailingSlash) {
      return result.endsWith('/') ? result : `${result}/`;
    }
    return result.endsWith('/') && result.length > 1 ? result.slice(0, -1) : result;
  }

  function buildUrl(path, query = {}, keepTrailingSlash = false) {
    const base = normalizePath(state.config.apiBase) || '';
    const normalizedPath = normalizePath(path, keepTrailingSlash);
    const url = new URL(`${base}${normalizedPath}`, window.location.origin);
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && `${value}`.trim() !== '') {
        url.searchParams.set(key, value);
      }
    });
    return url;
  }

  function setLoading(loading) {
    state.loading = loading;
    elements.sendBtn.disabled = loading;
    elements.messageInput.disabled = loading;
    elements.messageList.classList.toggle('loading', loading);
    setRequestState(loading ? '请求中...' : '待命');
  }

  function renderMessageList() {
    elements.messageList.innerHTML = '';
    if (state.messages.length === 0) {
      elements.messageList.innerHTML = '<div class="empty-state">还没有对话，先发送一条消息试试。</div>';
      elements.messageCount.textContent = '0 条消息';
      return;
    }

    const template = $('messageTemplate');
    state.messages.forEach((message) => {
      const node = template.content.firstElementChild.cloneNode(true);
      node.classList.add(`message--${message.role}`);
      node.querySelector('.message__role').textContent = message.roleLabel;
      node.querySelector('.message__time').textContent = message.time;
      node.querySelector('.message__bubble').innerHTML = escapeHtml(message.content);
      elements.messageList.appendChild(node);
    });

    elements.messageCount.textContent = `${state.messages.length} 条消息`;
    elements.messageList.scrollTop = elements.messageList.scrollHeight;
  }

  function addMessage(role, content, time = formatTime()) {
    const roleMap = {
      user: '你',
      ai: 'AI',
      system: '系统',
      error: '错误'
    };

    state.messages.push({
      role,
      roleLabel: roleMap[role] || role,
      content,
      time
    });
    renderMessageList();
  }

  function persistConfig() {
    saveStorage(STORAGE_KEYS.apiBase, state.config.apiBase);
    saveStorage(STORAGE_KEYS.chatPath, state.config.chatPath);
    saveStorage(STORAGE_KEYS.clearPath, state.config.clearPath);
    saveStorage(STORAGE_KEYS.memoryId, state.config.memoryId);
    saveStorage(STORAGE_KEYS.medicationTimes, state.config.medicationTimes);
  }

  function loadConfig() {
    state.config.apiBase = normalizePath(readStorage(STORAGE_KEYS.apiBase, DEFAULT_CONFIG.apiBase));
    state.config.chatPath = normalizePath(readStorage(STORAGE_KEYS.chatPath, DEFAULT_CONFIG.chatPath));
    state.config.clearPath = normalizePath(readStorage(STORAGE_KEYS.clearPath, DEFAULT_CONFIG.clearPath));
    state.config.memoryId = readStorage(STORAGE_KEYS.memoryId, DEFAULT_CONFIG.memoryId).trim() || DEFAULT_CONFIG.memoryId;
    state.config.medicationTimes = readStorage(STORAGE_KEYS.medicationTimes, DEFAULT_CONFIG.medicationTimes).trim();

    elements.apiBaseInput.value = state.config.apiBase;
    elements.chatPathInput.value = state.config.chatPath;
    elements.clearPathInput.value = state.config.clearPath;
    elements.memoryIdInput.value = state.config.memoryId;
    elements.medicationTimesInput.value = state.config.medicationTimes;
    updateUiConfigSummary();
  }

  async function requestText(url, options = {}) {
    const response = await fetch(url, {
      headers: {
        Accept: 'text/plain, application/json;q=0.9, */*;q=0.8',
        ...(options.headers || {})
      },
      ...options
    });

    const text = await response.text();
    if (!response.ok) {
      throw new Error(text || `请求失败：${response.status}`);
    }
    return text;
  }

  async function requestJson(url, options = {}) {
    const response = await fetch(url, {
      headers: {
        Accept: 'application/json, text/plain;q=0.9, */*;q=0.8',
        ...(options.headers || {})
      },
      ...options
    });

    const text = await response.text();
    if (!response.ok) {
      throw new Error(text || `请求失败：${response.status}`);
    }

    try {
      return JSON.parse(text);
    } catch {
      return text;
    }
  }

  async function sendMessage() {
    const message = elements.messageInput.value.trim();
    if (!message || state.loading) return;

    syncConfigFromInputs();
    persistConfig();

    addMessage('user', message);
    elements.messageInput.value = '';
    setLoading(true);
    setStatus('发送中', 'idle');
    setLastResponseState('等待响应');

    try {
      const url = buildUrl(state.config.chatPath, {
        message,
        medicationTimes: state.config.medicationTimes
      }, true);
      const reply = await requestText(url, { method: 'GET' });
      addMessage('ai', reply || '（空响应）');
      setLastResponseState(reply ? reply.slice(0, 64) : '空响应');
      setStatus('连接正常', 'ok');
    } catch (error) {
      const errorText = error instanceof Error ? error.message : String(error);
      addMessage('error', `发送失败：${errorText}`);
      setLastResponseState('请求失败');
      setStatus('请求失败', 'warn');
    } finally {
      setLoading(false);
    }
  }

  async function clearMemory() {
    syncConfigFromInputs();
    persistConfig();

    const confirmed = window.confirm(`确定清除 memoryId = ${state.config.memoryId} 的记忆吗？`);
    if (!confirmed) return;

    setLoading(true);
    setStatus('清除记忆中', 'idle');
    try {
      const url = buildUrl(state.config.clearPath);
      const result = await requestText(url, { method: 'DELETE' });
      addMessage('system', result || '记忆已清除');
      setLastResponseState('记忆清除成功');
      setStatus('连接正常', 'ok');
    } catch (error) {
      const errorText = error instanceof Error ? error.message : String(error);
      addMessage('error', `清除失败：${errorText}`);
      setLastResponseState('清除失败');
      setStatus('清除失败', 'warn');
    } finally {
      setLoading(false);
    }
  }

  async function saveMemory() {
    syncConfigFromInputs();
    persistConfig();

    setLoading(true);
    setStatus('保存记忆中', 'idle');
    try {
      const url = buildUrl('/memory/records', { memoryId: state.config.memoryId });
      const result = await requestJson(url, { method: 'GET' });
      const count = Array.isArray(result) ? result.length : 0;
      addMessage('system', `记忆保存完成，接口返回 ${count} 条记录。`);
      setLastResponseState(`保存成功（${count} 条）`);
      setStatus('连接正常', 'ok');
    } catch (error) {
      const errorText = error instanceof Error ? error.message : String(error);
      addMessage('error', `保存记忆失败：${errorText}`);
      setLastResponseState('保存失败');
      setStatus('请求失败', 'warn');
    } finally {
      setLoading(false);
    }
  }

  function resetChat() {
    state.messages = [];
    renderMessageList();
    setLastResponseState('无');
    setStatus('已清空界面', 'idle');
  }

  function fillSample() {
    elements.messageInput.value = '你好，请帮我介绍一下这个项目当前支持哪些 AI 能力？';
    elements.messageInput.focus();
  }

  function bindEvents() {
    elements.saveConfigBtn.addEventListener('click', () => {
      syncConfigFromInputs();
      persistConfig();
      setStatus('配置已保存', 'ok');
    });

    elements.saveMemoryBtn.addEventListener('click', saveMemory);
    elements.clearMemoryBtn.addEventListener('click', clearMemory);
    elements.resetChatBtn.addEventListener('click', resetChat);
    elements.sendBtn.addEventListener('click', sendMessage);
    elements.sampleBtn.addEventListener('click', fillSample);

    elements.memoryIdInput.addEventListener('change', () => {
      syncConfigFromInputs();
      persistConfig();
    });

    elements.messageInput.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
      }
    });

    [elements.apiBaseInput, elements.chatPathInput, elements.clearPathInput, elements.medicationTimesInput].forEach((el) => {
      el.addEventListener('change', () => {
        syncConfigFromInputs();
        persistConfig();
      });
    });
  }

  async function bootstrap() {
    loadConfig();
    bindEvents();
    renderMessageList();
    setStatus('准备就绪', 'ok');
    setRequestState('待命');
    setLastResponseState('无');
    addMessage('system', '页面已加载，可直接开始对话。');
  }

  document.addEventListener('DOMContentLoaded', bootstrap);
})();
