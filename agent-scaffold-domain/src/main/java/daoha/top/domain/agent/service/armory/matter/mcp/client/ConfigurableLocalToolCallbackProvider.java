package daoha.top.domain.agent.service.armory.matter.mcp.client;

import daoha.top.domain.agent.model.valobj.AiApiVO;
import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * 需要数据库配置才能创建回调的本地工具提供器。
 *
 * 与普通 ToolCallbackProvider 不同，它在每次 Agent 装配时接收该应用自己的
 * 工具参数与 API 凭证，避免把密钥写进 Bean、提示词或 Skill 文件。
 */
public interface ConfigurableLocalToolCallbackProvider {

    ToolCallback[] getToolCallbacks(AiMcpToolVO toolConfig, List<AiApiVO> apiConfigs);
}
