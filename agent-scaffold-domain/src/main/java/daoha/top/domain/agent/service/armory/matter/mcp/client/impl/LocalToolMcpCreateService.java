package daoha.top.domain.agent.service.armory.matter.mcp.client.impl;

import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @ClassName : LocalToolMcpCreateService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/7  15:09
 */
@Slf4j
@Service
public class LocalToolMcpCreateService implements TooMcpCreateService {

    @Resource
    protected ApplicationContext applicationContext;

    @Override
    public ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception {

        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.LocalParameters local = toolMcp.getLocal();

        String name = local.getName();

        ToolCallbackProvider localToolCallbackProvider = (ToolCallbackProvider)applicationContext.getBean(name);
        log.info("tool local mcp 初始化:{}",name);

        return localToolCallbackProvider.getToolCallbacks();
    }
}
