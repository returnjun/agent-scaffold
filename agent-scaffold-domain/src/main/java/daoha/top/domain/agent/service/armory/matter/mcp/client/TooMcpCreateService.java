package daoha.top.domain.agent.service.armory.matter.mcp.client;

import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import org.springframework.ai.tool.ToolCallback;

/**
 * @ClassName : TooMcpCreateService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/7  14:59
 */

public interface TooMcpCreateService {
    ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception;
}
