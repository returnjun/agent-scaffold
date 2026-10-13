# Agent 配置数据库初始化

项目使用 MySQL 8.0.16+ 的八张配置表。启动时先查找已启用的应用，然后 LoadDataStrategy 使用 threadPoolExecutor 并行读取 API、模型、MCP 工具、Skill、Agent、工作流和 Runner。读取完成后按依赖顺序装配；聊天请求直接使用已注册的 Runner。

当前开发环境先连接已有的 agent_space 数据库。将 DB_URL 指向 agent_space，并使用该库对应的 DB_PASSWORD 和 AGENT_CONFIG_MASTER_KEY，即可复用现有数据；此时无需执行下面的建表和初始化脚本。下面的 SQL 用于以后创建独立的 agent_scaffold 库。

开发配置中的 ai.agent.bootstrap.agent-ids 暂设为 100001,100002。agent_space 中其他已启用应用依赖当前脚手架没有的本地工具 Bean；清空该设置后会尝试装配全部应用。配置列表接口仅返回本次成功注册的 Agent。

## 1. 执行 SQL

从项目根目录启动 MySQL 客户端，然后运行：

    SOURCE docs/dev-ops/mysql/sql/agent-scaffold-config-schema.sql;

在同一个 MySQL 客户端会话输入实际密钥（此处只展示占位说明，不要将真实值写进项目文件），然后运行：

    SET @agent_config_master_key = '<随机主密钥>';
    SET @model_api_key = '<模型 API Key>';
    SET @baidu_mcp_key = '<百度 MCP Key>';
    SET @amap_mcp_key = '<高德 MCP Key>';
    SOURCE docs/dev-ops/mysql/sql/agent-scaffold-config-seed.sql;

初始化脚本只适合空库执行一次。Schema 脚本不会删除已有表；Seed 脚本写入当前默认智能体 100002。密钥不会以明文写入数据库。

## 2. 配置应用

启动前设置 DB_URL、DB_PASSWORD 和 AGENT_CONFIG_MASTER_KEY，可选设置 DB_USERNAME（默认 root）。主密钥必须与 Seed 脚本中的 @agent_config_master_key 完全一致。例如在当前 PowerShell 窗口：

    $env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/agent_scaffold?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
    $env:DB_USERNAME = 'root'
    $env:DB_PASSWORD = '<数据库密码>'
    $env:AGENT_CONFIG_MASTER_KEY = '<与 SQL 相同的主密钥>'

也可在被 Git 忽略的 agent-scaffold-app/src/main/resources/application-dev.local.yml 中配置对应属性。修改数据库配置后需要重启应用，才能重新装配 Agent。

Seed 脚本保留了当前默认应用的模型、百度与高德 SSE 工具、本地工具、Skill 和插件。远程工具的凭据使用当前 MySQL 会话变量输入，写入数据库时由 AES_ENCRYPT 加密，真实值不会保存在仓库中。
