package daoha.top.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent 配置库读取参数。
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.agent.database")
public class AgentDatabaseProperties {

    /** 必须与初始化 SQL 使用的 @agent_config_master_key 相同。 */
    private String masterKey;
}
