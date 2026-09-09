const config = window.AGENT_CONSOLE_CONFIG;
const auth = readAuth();

if (!auth) {
    window.location.replace('./login.html');
} else {
    initializeChat(auth);
}

function readAuth() {
    try {
        const value = JSON.parse(sessionStorage.getItem('agentConsoleUser'));
        return value && typeof value.userId === 'string' && value.userId.trim() ? value : null;
    } catch {
        return null;
    }
}

function initializeChat(currentUser) {
    const elements = {
        agentSelect: document.querySelector('#agent-select'),
        agentDescription: document.querySelector('#agent-description'),
        activeAgentName: document.querySelector('#active-agent-name'),
        activeAgentState: document.querySelector('#active-agent-state'),
        conversation: document.querySelector('#conversation'),
        welcome: document.querySelector('#welcome-state'),
        messages: document.querySelector('#messages'),
        form: document.querySelector('#chat-form'),
        input: document.querySelector('#message-input'),
        sendButton: document.querySelector('#send-button'),
        sessionId: document.querySelector('#session-id'),
        serviceStatus: document.querySelector('.service-status'),
        serviceLabel: document.querySelector('#service-label'),
        sidebar: document.querySelector('#sidebar'),
        backdrop: document.querySelector('#sidebar-backdrop'),
        menuButton: document.querySelector('#menu-button'),
        newChat: document.querySelector('#new-chat'),
        logoutButton: document.querySelector('#logout-button'),
        sidebarUser: document.querySelector('#sidebar-user'),
        sidebarAvatar: document.querySelector('#sidebar-avatar'),
        topbarUser: document.querySelector('#topbar-user'),
        toast: document.querySelector('#toast')
    };

    const state = {
        userId: currentUser.userId.trim(),
        agents: [],
        busy: false,
        sessionId: ''
    };

    elements.sidebarUser.textContent = state.userId;
    elements.topbarUser.textContent = state.userId;
    elements.sidebarAvatar.textContent = state.userId.charAt(0).toUpperCase();

    elements.form.addEventListener('submit', (event) => {
        event.preventDefault();
        void sendMessage(elements.input.value);
    });

    elements.input.addEventListener('input', () => resizeComposer(elements.input));
    elements.input.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            elements.form.requestSubmit();
        }
    });

    elements.agentSelect.addEventListener('change', updateSelectedAgent);
    elements.newChat.addEventListener('click', clearConversation);
    elements.logoutButton.addEventListener('click', () => {
        sessionStorage.removeItem('agentConsoleUser');
        window.location.replace('./login.html');
    });

    elements.menuButton.addEventListener('click', () => toggleSidebar());
    elements.backdrop.addEventListener('click', () => toggleSidebar(false));

    document.querySelectorAll('[data-prompt]').forEach((button) => {
        button.addEventListener('click', () => {
            elements.input.value = button.dataset.prompt;
            resizeComposer(elements.input);
            elements.input.focus();
        });
    });

    void loadAgents();
    registerWebMcpTool();

    async function loadAgents() {
        elements.agentSelect.disabled = true;
        setServiceState('loading', '正在连接');

        try {
            const response = await requestJson(config.endpoints.agents, { method: 'GET' }, 15000);
            const agents = Array.isArray(response.data) ? response.data : [];
            if (!agents.length) throw new Error('当前没有可用的智能体');

            state.agents = agents;
            elements.agentSelect.replaceChildren(...agents.map((agent) => {
                const option = document.createElement('option');
                option.value = agent.agentId;
                option.textContent = agent.agentName || agent.agentId;
                return option;
            }));
            elements.agentSelect.disabled = false;
            updateSelectedAgent();
            setServiceState('online', `${agents.length} 个智能体在线`);
        } catch (error) {
            elements.agentSelect.replaceChildren(new Option('智能体加载失败', ''));
            elements.agentDescription.textContent = '请确认服务已在 8091 端口启动';
            setServiceState('error', '服务连接失败');
            showToast(error.message, 'error');
        }
    }

    function updateSelectedAgent() {
        const agent = state.agents.find((item) => item.agentId === elements.agentSelect.value);
        elements.activeAgentName.textContent = agent?.agentName || '智能体对话';
        elements.agentDescription.textContent = agent?.agentDesc || '选择一个智能体开始对话';
        elements.activeAgentState.textContent = agent ? `Agent ID · ${agent.agentId}` : '等待选择';
        state.sessionId = '';
        elements.sessionId.textContent = 'NEW';
        toggleSidebar(false);
    }

    async function sendMessage(rawMessage) {
        const message = String(rawMessage || '').trim();
        const agentId = elements.agentSelect.value;
        if (state.busy) return null;
        if (!agentId) {
            showToast('请先选择一个可用的智能体', 'error');
            return null;
        }
        if (!message) {
            elements.input.focus();
            return null;
        }

        state.busy = true;
        setComposerBusy(true);
        hideWelcome();
        appendMessage('user', message, state.userId);
        elements.input.value = '';
        resizeComposer(elements.input);

        let assistantMessage;
        let receivedText = '';
        try {
            state.sessionId = await createSession(agentId);
            elements.sessionId.textContent = shortenId(state.sessionId);
            assistantMessage = appendMessage('assistant', '', selectedAgentName(), true);

            try {
                receivedText = await streamChat({ agentId, sessionId: state.sessionId, message }, (chunk) => {
                    receivedText += chunk;
                    updateAssistantMessage(assistantMessage, receivedText);
                });
            } catch (streamError) {
                if (receivedText) throw streamError;
                const content = await normalChat({ agentId, sessionId: state.sessionId, message });
                receivedText = content;
                updateAssistantMessage(assistantMessage, content);
            }

            if (!receivedText.trim()) updateAssistantMessage(assistantMessage, '智能体本次没有返回内容。');
            setServiceState('online', `${state.agents.length} 个智能体在线`);
            return { agentId, sessionId: state.sessionId, content: receivedText };
        } catch (error) {
            if (assistantMessage) {
                updateAssistantMessage(assistantMessage, receivedText || `请求失败：${error.message}`, true);
            } else {
                appendMessage('assistant', `请求失败：${error.message}`, selectedAgentName(), false, true);
            }
            setServiceState('error', '本次请求失败');
            showToast(error.message, 'error');
            throw error;
        } finally {
            state.busy = false;
            setComposerBusy(false);
            elements.input.focus();
        }
    }

    async function createSession(agentId) {
        const response = await requestJson(config.endpoints.createSession, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ agentId, userId: state.userId })
        }, 20000);
        const sessionId = response.data?.sessionId;
        if (!sessionId) throw new Error('会话创建成功，但未返回 sessionId');
        return sessionId;
    }

    async function streamChat(payload, onChunk) {
        const controller = new AbortController();
        const timer = window.setTimeout(() => controller.abort(), config.REQUEST_TIMEOUT_MS);
        try {
            const response = await fetch(apiUrl(config.endpoints.chatStream), {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Accept': 'text/plain' },
                body: JSON.stringify({ ...payload, userId: state.userId }),
                signal: controller.signal
            });
            if (!response.ok) throw new Error(`流式服务请求失败（${response.status}）`);
            if (!response.body) throw new Error('浏览器不支持读取流式响应');

            const reader = response.body.getReader();
            const decoder = new TextDecoder('utf-8');
            let content = '';
            while (true) {
                const { value, done } = await reader.read();
                if (done) break;
                const chunk = decoder.decode(value, { stream: true });
                content += chunk;
                onChunk(chunk);
            }
            const tail = decoder.decode();
            if (tail) {
                content += tail;
                onChunk(tail);
            }
            return content;
        } catch (error) {
            if (error.name === 'AbortError') throw new Error('智能体响应超时，请稍后重试');
            throw error;
        } finally {
            window.clearTimeout(timer);
        }
    }

    async function normalChat(payload) {
        const response = await requestJson(config.endpoints.chat, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...payload, userId: state.userId })
        });
        return response.data?.content || '';
    }

    async function requestJson(path, options, timeout = config.REQUEST_TIMEOUT_MS) {
        const controller = new AbortController();
        const timer = window.setTimeout(() => controller.abort(), timeout);
        try {
            const response = await fetch(apiUrl(path), { ...options, signal: controller.signal });
            if (!response.ok) throw new Error(`服务请求失败（${response.status}）`);
            const result = await response.json();
            if (result.code !== '0000') throw new Error(result.info || '服务返回异常');
            return result;
        } catch (error) {
            if (error.name === 'AbortError') throw new Error('服务响应超时，请稍后重试');
            if (error instanceof TypeError) throw new Error('无法连接智能体服务，请确认后端已启动');
            throw error;
        } finally {
            window.clearTimeout(timer);
        }
    }

    function appendMessage(role, content, author, loading = false, error = false) {
        const row = document.createElement('article');
        row.className = `message-row ${role}${error ? ' is-error' : ''}`;
        const bubble = document.createElement('div');
        bubble.className = `message-bubble${loading ? ' is-loading' : ''}`;
        const header = document.createElement('div');
        header.className = 'message-header';
        const name = document.createElement('strong');
        name.textContent = role === 'user' ? author : (author || '智能体');
        const time = document.createElement('span');
        time.textContent = nowTime();
        header.append(name, time);
        const body = document.createElement('div');
        body.className = 'message-content';
        if (loading) {
            body.innerHTML = '<span class="typing"><i></i><i></i><i></i></span>';
        } else {
            body.textContent = content;
        }
        bubble.append(header, body);
        row.append(bubble);
        elements.messages.append(row);
        scrollToLatest();
        return { row, bubble, body };
    }

    function updateAssistantMessage(messageElement, content, error = false) {
        messageElement.bubble.classList.remove('is-loading');
        messageElement.body.textContent = content;
        messageElement.row.classList.toggle('is-error', error);
        scrollToLatest();
    }

    function clearConversation() {
        if (state.busy) return;
        elements.messages.replaceChildren();
        elements.welcome.hidden = false;
        state.sessionId = '';
        elements.sessionId.textContent = 'NEW';
        elements.input.value = '';
        resizeComposer(elements.input);
        toggleSidebar(false);
        elements.input.focus();
    }

    function hideWelcome() {
        elements.welcome.hidden = true;
    }

    function selectedAgentName() {
        return state.agents.find((item) => item.agentId === elements.agentSelect.value)?.agentName || '智能体';
    }

    function setComposerBusy(busy) {
        elements.sendButton.disabled = busy;
        elements.input.disabled = busy;
        elements.agentSelect.disabled = busy;
        elements.activeAgentState.textContent = busy ? '正在思考与调用工具…' : `Agent ID · ${elements.agentSelect.value}`;
    }

    function setServiceState(status, label) {
        elements.serviceStatus.classList.toggle('is-online', status === 'online');
        elements.serviceStatus.classList.toggle('is-error', status === 'error');
        elements.serviceLabel.textContent = label;
    }

    function showToast(message, type = 'info') {
        elements.toast.textContent = message;
        elements.toast.className = `toast is-visible ${type}`;
        window.clearTimeout(showToast.timer);
        showToast.timer = window.setTimeout(() => elements.toast.classList.remove('is-visible'), 3600);
    }

    function toggleSidebar(force) {
        const open = typeof force === 'boolean' ? force : !elements.sidebar.classList.contains('is-open');
        elements.sidebar.classList.toggle('is-open', open);
        elements.backdrop.classList.toggle('is-visible', open);
        elements.menuButton.setAttribute('aria-expanded', String(open));
    }

    function resizeComposer(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = `${Math.min(textarea.scrollHeight, 170)}px`;
    }

    function scrollToLatest() {
        requestAnimationFrame(() => elements.conversation.scrollTo({ top: elements.conversation.scrollHeight, behavior: 'smooth' }));
    }

    function apiUrl(path) {
        return `${config.API_BASE_URL.replace(/\/$/, '')}${path}`;
    }

    function shortenId(value) {
        return value.length > 13 ? `${value.slice(0, 8)}…${value.slice(-4)}` : value;
    }

    function nowTime() {
        return new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false }).format(new Date());
    }

    function registerWebMcpTool() {
        const context = document.modelContext;
        if (!context?.registerTool) return;
        try {
            void Promise.resolve(context.registerTool({
                name: 'send_agent_message',
                title: '向智能体发送消息',
                description: '选择页面中的智能体、创建独立会话并发送一条消息，同时把问答展示在当前对话窗口。',
                inputSchema: {
                    type: 'object',
                    properties: {
                        message: { type: 'string', minLength: 1, maxLength: 4000 },
                        agentId: { type: 'string' }
                    },
                    required: ['message'],
                    additionalProperties: false
                },
                annotations: { readOnlyHint: false, untrustedContentHint: true },
                async execute(input) {
                    if (!input || typeof input.message !== 'string' || !input.message.trim()) throw new Error('message 不能为空');
                    if (state.busy) throw new Error('智能体正在处理上一条消息');
                    if (!state.agents.length) throw new Error('智能体列表尚未加载完成');
                    if (input.agentId) {
                        const exists = state.agents.some((agent) => agent.agentId === input.agentId);
                        if (!exists) throw new Error('指定的 agentId 不可用');
                        elements.agentSelect.value = input.agentId;
                        updateSelectedAgent();
                    }
                    const result = await sendMessage(input.message);
                    if (!result) throw new Error('消息未能发送');
                    return { status: 'completed', agentId: result.agentId, sessionId: result.sessionId };
                }
            })).catch(() => {});
        } catch {
            // WebMCP is optional; the visible interface remains fully functional.
        }
    }
}
