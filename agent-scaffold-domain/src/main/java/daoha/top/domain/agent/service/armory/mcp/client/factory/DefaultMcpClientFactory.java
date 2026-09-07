package daoha.top.domain.agent.service.armory.mcp.client.factory;

import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.service.armory.mcp.client.TooMcpCreateService;
import daoha.top.domain.agent.service.armory.mcp.client.impl.LocalToolMcpCreateService;
import daoha.top.domain.agent.service.armory.mcp.client.impl.SSEToolMcpCreateService;
import daoha.top.domain.agent.service.armory.mcp.client.impl.StdioToolMcpCreateService;
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


    public TooMcpCreateService getLocalToolMcpCreateService(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) {
        if(null !=toolMcp.getLocal())return localToolMcpCreateService;
        if(null !=toolMcp.getSse())return sseToolMcpCreateService;
        if(null !=toolMcp.getStdio())return stdioToolMcpCreateService;
        throw new AppException(ResponseCode.NOT_FOUND_METHOD.getCode(), ResponseCode.NOT_FOUND_METHOD.getInfo());
    }
}
