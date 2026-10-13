package daoha.top.domain.agent.service.armory.matter.mcp.client.impl;

import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import daoha.top.domain.agent.model.valobj.AiApiVO;
import daoha.top.domain.agent.service.armory.matter.mcp.client.ConfigurableLocalToolCallbackProvider;
import daoha.top.domain.agent.service.armory.matter.mcp.client.TooMcpCreateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public ToolCallback[] buildToolCallback(AiMcpToolVO toolMcp) throws Exception {

        return buildToolCallback(toolMcp, List.of());
    }

    @Override
    public ToolCallback[] buildToolCallback(AiMcpToolVO toolMcp,
                                            List<AiApiVO> apiConfigs) {

        AiMcpToolVO.LocalParameters local = toolMcp.getLocal();

        String name = local.getName();

        Object provider = applicationContext.getBean(name);
        log.info("tool local mcp 初始化:{}",name);

        if (provider instanceof ConfigurableLocalToolCallbackProvider configurableProvider) {
            return configurableProvider.getToolCallbacks(toolMcp, apiConfigs);
        }
        if (provider instanceof ToolCallbackProvider callbackProvider) {
            return callbackProvider.getToolCallbacks();
        }
        throw new IllegalArgumentException("本地工具 Bean 必须实现 ToolCallbackProvider 或 "
                + "ConfigurableLocalToolCallbackProvider: " + name);
    }
}
