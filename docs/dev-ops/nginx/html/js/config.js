window.AGENT_CONSOLE_CONFIG = Object.freeze({
    API_BASE_URL: 'http://127.0.0.1:8091',
    REQUEST_TIMEOUT_MS: 180000,
    endpoints: Object.freeze({
        agents: '/api/v1/query_ai_agent_config_list',
        createSession: '/api/v1/create_session',
        chat: '/api/v1/chat',
        chatStream: '/api/v1/chat_stream'
    })
});
