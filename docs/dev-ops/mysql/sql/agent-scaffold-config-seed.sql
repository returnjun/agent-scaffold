-- MySQL 8.0.16+; execute after agent-scaffold-config-schema.sql.
-- Set @agent_config_master_key, @model_api_key, @baidu_mcp_key and
-- @amap_mcp_key in this MySQL session before sourcing this file.
-- The master key must equal AGENT_CONFIG_MASTER_KEY at application startup.
USE agent_scaffold;
SET NAMES utf8mb4;

DELIMITER $$
DROP PROCEDURE IF EXISTS validate_agent_seed$$
CREATE PROCEDURE validate_agent_seed()
BEGIN
    IF @agent_config_master_key IS NULL
       OR @model_api_key IS NULL
       OR @baidu_mcp_key IS NULL
       OR @amap_mcp_key IS NULL
       OR @agent_config_master_key LIKE 'REPLACE_WITH_%'
       OR @model_api_key LIKE 'REPLACE_WITH_%'
       OR @baidu_mcp_key LIKE 'REPLACE_WITH_%'
       OR @amap_mcp_key LIKE 'REPLACE_WITH_%'
       OR @agent_config_master_key = ''
       OR @model_api_key = ''
       OR @baidu_mcp_key = ''
       OR @amap_mcp_key = '' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Set the master key and model API key before seeding';
    END IF;
END$$
CALL validate_agent_seed()$$
DROP PROCEDURE validate_agent_seed$$
DELIMITER ;

START TRANSACTION;

-- Active single-agent example; the public agentId is 100002.
INSERT INTO ai_app
    (app_code, app_name, agent_code, agent_name, agent_description, status)
VALUES
    ('testAgent02', 'testAgent02', '100002', '单一智能体', '单一智能体', 1);
SET @app_id = LAST_INSERT_ID();

INSERT INTO ai_api
    (app_id, api_code, base_url, api_key_ciphertext,
     completions_path, embeddings_path, status, sort_no)
VALUES
    (@app_id, 'default-openai', 'https://api2.aigcbest.top',
     AES_ENCRYPT(@model_api_key, UNHEX(SHA2(@agent_config_master_key, 512))),
     '/v1/chat/completions', '/v1/embeddings', 1, 1);
SET @api_id = LAST_INSERT_ID();

INSERT INTO ai_model
    (app_id, model_code, api_id, model_name, model_options, status, sort_no)
VALUES
    (@app_id, 'default-chat', @api_id, 'gpt-5.4-mini', NULL, 1, 1);
SET @model_id = LAST_INSERT_ID();

INSERT INTO ai_mcp_tool
    (app_id, mcp_code, transport_type, tool_name, base_uri, endpoint_path,
     request_timeout_ms, auth_type, auth_key, auth_value_ciphertext, status, sort_no)
VALUES
    (@app_id, 'baidu-search', 'SSE', 'baidu-search',
     'http://appbuilder.baidu.com/v2/ai_search/mcp/', 'sse', 5000,
     'QUERY', 'api_key',
     AES_ENCRYPT(@baidu_mcp_key, UNHEX(SHA2(@agent_config_master_key, 512))), 1, 1);
SET @baidu_tool_id = LAST_INSERT_ID();

INSERT INTO ai_mcp_tool
    (app_id, mcp_code, transport_type, tool_name, base_uri, endpoint_path,
     request_timeout_ms, auth_type, auth_key, auth_value_ciphertext, status, sort_no)
VALUES
    (@app_id, 'amap-search', 'SSE', 'Amap', 'https://mcp.amap.com', '/sse', 5000,
     'QUERY', 'key',
     AES_ENCRYPT(@amap_mcp_key, UNHEX(SHA2(@agent_config_master_key, 512))), 1, 2);
SET @amap_tool_id = LAST_INSERT_ID();

INSERT INTO ai_mcp_tool
    (app_id, mcp_code, transport_type, tool_name, local_bean_name,
     request_timeout_ms, status, sort_no)
VALUES
    (@app_id, 'local-test-tools', 'LOCAL', 'myToolCallbackProvider',
     'myToolCallbackProvider', 3000, 1, 3);
SET @local_tool_id = LAST_INSERT_ID();

INSERT INTO ai_skill
    (app_id, skill_code, resource_type, resource_path, status, sort_no)
VALUES
    (@app_id, 'bundled-skills', 'resource', 'agent/skills', 1, 1);
SET @skill_id = LAST_INSERT_ID();

INSERT INTO ai_agent
    (app_id, agent_code, model_id, description, instruction_text, output_key,
     mcp_tool_ids, skill_ids, tool_ids, status, sort_no)
VALUES
    (@app_id, 'onlyAgent', @model_id, '通用chat聊天agent',
     '负责和主要和用户聊天的通用agent.', NULL,
     JSON_ARRAY(@baidu_tool_id, @amap_tool_id, @local_tool_id),
     JSON_ARRAY(@skill_id), JSON_ARRAY(), 1, 1);
SET @agent_id = LAST_INSERT_ID();

INSERT INTO ai_runner (app_id, agent_id, workflow_id, plugin_names, status)
VALUES (@app_id, @agent_id, NULL, JSON_ARRAY('myTestPlugin'), 1);

COMMIT;

-- Additional Agent/workflow rows can be inserted using database row IDs in
-- ai_agent_workflow.sub_agent_ids and ai_runner.workflow_id.
