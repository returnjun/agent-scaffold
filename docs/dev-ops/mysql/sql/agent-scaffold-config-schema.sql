-- MySQL 8.0.16+; run once before agent-scaffold-config-seed.sql.
-- Creates only the eight Agent configuration tables; existing data is not dropped.
CREATE DATABASE IF NOT EXISTS agent_scaffold
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE agent_scaffold;
CREATE TABLE IF NOT EXISTS ai_app (
    id                 BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_code           VARCHAR(64)  NOT NULL COMMENT '应用配置标识',
    app_name           VARCHAR(128) NOT NULL COMMENT '运行时应用名称',
    agent_code         VARCHAR(64)  NOT NULL COMMENT '对外智能体ID',
    agent_name         VARCHAR(128) NOT NULL COMMENT '对外展示名称',
    agent_description  TEXT NULL,
    status             TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                  ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_app_code (app_code),
    UNIQUE KEY uk_ai_app_agent_code (agent_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能体应用';

CREATE TABLE IF NOT EXISTS ai_api (
    id                  BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_id              BIGINT UNSIGNED NOT NULL,
    api_code            VARCHAR(128) NOT NULL COMMENT '原api-id',
    base_url            VARCHAR(1024) NOT NULL,
    api_key_ciphertext  LONGBLOB NOT NULL COMMENT 'AES_ENCRYPT后的API Key',
    completions_path    VARCHAR(512) NOT NULL DEFAULT '/v1/chat/completions',
    embeddings_path     VARCHAR(512) NOT NULL DEFAULT '/v1/embeddings',
    status              TINYINT NOT NULL DEFAULT 1,
    sort_no             INT NOT NULL DEFAULT 0,
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                   ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_api_app_code (app_id, api_code),
    CONSTRAINT fk_ai_api_app FOREIGN KEY (app_id) REFERENCES ai_app(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型API配置';

CREATE TABLE IF NOT EXISTS ai_model (
    id             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_id         BIGINT UNSIGNED NOT NULL,
    model_code     VARCHAR(128) NOT NULL COMMENT '原chat-id',
    api_id         BIGINT UNSIGNED NOT NULL,
    model_name     VARCHAR(128) NOT NULL,
    model_options  JSON NULL COMMENT 'temperature、maxTokens等扩展配置',
    status         TINYINT NOT NULL DEFAULT 1,
    sort_no        INT NOT NULL DEFAULT 0,
    created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                              ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_model_app_code (app_id, model_code),
    KEY idx_ai_model_api (api_id),
    CONSTRAINT fk_ai_model_app FOREIGN KEY (app_id) REFERENCES ai_app(id),
    CONSTRAINT fk_ai_model_api FOREIGN KEY (api_id) REFERENCES ai_api(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天模型配置';

CREATE TABLE IF NOT EXISTS ai_mcp_tool (
    id                     BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_id                 BIGINT UNSIGNED NOT NULL,
    mcp_code               VARCHAR(128) NOT NULL COMMENT '原mcp-tool-id',
    transport_type         VARCHAR(20) NOT NULL COMMENT 'SSE/STDIO/LOCAL',
    tool_name              VARCHAR(128) NULL,
    base_uri               VARCHAR(1024) NULL,
    endpoint_path          VARCHAR(1024) NULL,
    request_timeout_ms     INT NOT NULL DEFAULT 3000,
    command_text           VARCHAR(1024) NULL,
    args_json              JSON NULL,
    env_json               JSON NULL,
    local_bean_name        VARCHAR(256) NULL,
    auth_type              VARCHAR(20) NULL COMMENT 'QUERY/HEADER/NONE',
    auth_key               VARCHAR(128) NULL,
    auth_value_ciphertext  LONGBLOB NULL COMMENT 'AES_ENCRYPT后的MCP密钥',
    status                 TINYINT NOT NULL DEFAULT 1,
    sort_no                INT NOT NULL DEFAULT 0,
    created_at             DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at             DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                      ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_mcp_app_code (app_id, mcp_code),
    CONSTRAINT fk_ai_mcp_app FOREIGN KEY (app_id) REFERENCES ai_app(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP和本地工具配置';

CREATE TABLE IF NOT EXISTS ai_skill (
    id             BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_id         BIGINT UNSIGNED NOT NULL,
    skill_code     VARCHAR(128) NOT NULL COMMENT '原skill-id',
    resource_type  VARCHAR(32) NOT NULL DEFAULT 'directory',
    resource_path  VARCHAR(1024) NOT NULL,
    status         TINYINT NOT NULL DEFAULT 1,
    sort_no        INT NOT NULL DEFAULT 0,
    created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                              ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_skill_app_code (app_id, skill_code),
    CONSTRAINT fk_ai_skill_app FOREIGN KEY (app_id) REFERENCES ai_app(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Skill配置';

CREATE TABLE IF NOT EXISTS ai_agent (
    id                BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_id            BIGINT UNSIGNED NOT NULL,
    agent_code        VARCHAR(128) NOT NULL COMMENT '原agents.name',
    model_id          BIGINT UNSIGNED NOT NULL,
    description       TEXT NULL,
    instruction_text  LONGTEXT NULL,
    output_key        VARCHAR(128) NULL,
    mcp_tool_ids      JSON NOT NULL COMMENT 'ai_mcp_tool.id数组',
    skill_ids         JSON NOT NULL COMMENT 'ai_skill.id数组',
    tool_ids          JSON NOT NULL COMMENT '其他工具ID数组',
    status            TINYINT NOT NULL DEFAULT 1,
    sort_no           INT NOT NULL DEFAULT 0,
    created_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                 ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_agent_app_code (app_id, agent_code),
    KEY idx_ai_agent_model (model_id),
    CONSTRAINT fk_ai_agent_app FOREIGN KEY (app_id) REFERENCES ai_app(id),
    CONSTRAINT fk_ai_agent_model FOREIGN KEY (model_id) REFERENCES ai_model(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLM Agent节点配置';

CREATE TABLE IF NOT EXISTS ai_agent_workflow (
    id              BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_id          BIGINT UNSIGNED NOT NULL,
    workflow_code   VARCHAR(128) NOT NULL COMMENT '原workflow.name',
    workflow_type   VARCHAR(32) NOT NULL COMMENT 'SEQUENTIAL/PARALLEL/LOOP',
    description     TEXT NULL,
    sub_agent_ids   JSON NOT NULL COMMENT '按执行顺序保存ai_agent.id数组',
    max_iterations  INT NOT NULL DEFAULT 3,
    status          TINYINT NOT NULL DEFAULT 1,
    sort_no         INT NOT NULL DEFAULT 0,
    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                               ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_workflow_app_code (app_id, workflow_code),
    CONSTRAINT fk_ai_workflow_app FOREIGN KEY (app_id) REFERENCES ai_app(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent工作流配置';

CREATE TABLE IF NOT EXISTS ai_runner (
    id            BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    app_id        BIGINT UNSIGNED NOT NULL,
    agent_id      BIGINT UNSIGNED NULL COMMENT '普通Agent入口',
    workflow_id   BIGINT UNSIGNED NULL COMMENT 'Workflow入口',
    plugin_names  JSON NOT NULL COMMENT '插件Bean名称数组',
    status        TINYINT NOT NULL DEFAULT 1,
    created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                              ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_ai_runner_app (app_id),
    KEY idx_ai_runner_agent (agent_id),
    KEY idx_ai_runner_workflow (workflow_id),
    CONSTRAINT fk_ai_runner_app FOREIGN KEY (app_id) REFERENCES ai_app(id),
    CONSTRAINT fk_ai_runner_agent FOREIGN KEY (agent_id) REFERENCES ai_agent(id),
    CONSTRAINT fk_ai_runner_workflow FOREIGN KEY (workflow_id) REFERENCES ai_agent_workflow(id),
    CONSTRAINT ck_ai_runner_single_entry CHECK (
        (agent_id IS NOT NULL AND workflow_id IS NULL)
        OR (agent_id IS NULL AND workflow_id IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent运行入口';

