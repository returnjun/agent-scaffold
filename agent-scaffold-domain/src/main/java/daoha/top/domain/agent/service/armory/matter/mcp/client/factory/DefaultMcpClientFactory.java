package daoha.top.domain.agent.service.armory.matter.mcp.client.factory;

import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import daoha.top.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import daoha.top.domain.agent.service.armory.matter.mcp.client.impl.LocalToolMcpCreateService;
import daoha.top.domain.agent.service.armory.matter.mcp.client.impl.SSEToolMcpCreateService;
import daoha.top.domain.agent.service.armory.matter.mcp.client.impl.StdioToolMcpCreateService;
import daoha.top.types.enums.ResponseCode;
import daoha.top.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @ClassName : DefaultMcpClientFactory
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/7  15:28
 */

@Slf4j
@Service
public class DefaultMcpClientFactory {
    @Resource
    private LocalToolMcpCreateService  localToolMcpCreateService;
    @Resource
    private SSEToolMcpCreateService  sseToolMcpCreateService;
    @Resource
    private StdioToolMcpCreateService   stdioToolMcpCreateService;


    public TooMcpCreateService getToolMcpCreateService(AiMcpToolVO toolMcp) {
        int transportCount = (toolMcp.getLocal() == null ? 0 : 1)
                + (toolMcp.getSse() == null ? 0 : 1)
                + (toolMcp.getStdio() == null ? 0 : 1);
        if (transportCount != 1) {
            throw new IllegalArgumentException("MCP 工具 " + toolMcp.getMcpToolId()
                    + " 必须且只能配置 local、sse、stdio 中的一种传输方式");
        }
        if (toolMcp.getLocal() != null) return localToolMcpCreateService;
        if (toolMcp.getSse() != null) return sseToolMcpCreateService;
        if (toolMcp.getStdio() != null) return stdioToolMcpCreateService;
        throw new AppException(ResponseCode.NOT_FOUND_METHOD.getCode(), ResponseCode.NOT_FOUND_METHOD.getInfo());
    }
}
