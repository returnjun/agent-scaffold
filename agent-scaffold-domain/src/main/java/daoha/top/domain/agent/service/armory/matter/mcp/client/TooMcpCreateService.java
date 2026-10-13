package daoha.top.domain.agent.service.armory.matter.mcp.client;

import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import daoha.top.domain.agent.model.valobj.AiApiVO;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * @ClassName : TooMcpCreateService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/7  14:59
 */

public interface TooMcpCreateService {
    ToolCallback[] buildToolCallback(AiMcpToolVO toolMcp) throws Exception;

    /**
     * 本地工具可在装配时使用当前应用已经加载并解密的 API 配置。
     * SSE/STDIO 工具沿用单参数实现即可。
     */
    default ToolCallback[] buildToolCallback(AiMcpToolVO toolMcp,
                                             List<AiApiVO> apiConfigs) throws Exception {
        return buildToolCallback(toolMcp);
    }
}
