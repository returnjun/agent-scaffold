package daoha.top.domain.agent.service.armory.node;

import com.google.adk.agents.BaseAgent;
import com.google.adk.plugins.BasePlugin;
import com.google.adk.runner.InMemoryRunner;
import com.google.common.collect.ImmutableList;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.AiAppVO;
import daoha.top.domain.agent.model.valobj.AiRunnerVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import daoha.top.types.enums.ResponseCode;
import daoha.top.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName : RunnerNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  22:40
 */
@Slf4j
@Service
public class RunnerNode extends AbstractArmorySupport {
    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("运行节点runner的装配");
        AiAppVO app = dynamicContext.getApp();
        String appName = app.getAppName();
        String agentId = app.getAgentId();
        String agentDesc = app.getAgentDesc();
        String agentName = app.getAgentName();

        InMemoryRunner runner = getRunner(dynamicContext, appName);

        AiAgentRegisterVO aiAgentRegisterVO = AiAgentRegisterVO.builder()
                .appName(appName)
                .agentId(agentId)
                .agentDesc(agentDesc)
                .agentName(agentName)
                .runner(runner)
                .build();
        //注册到spring容器
        registerBean(agentId,AiAgentRegisterVO.class,aiAgentRegisterVO);

        return aiAgentRegisterVO;
    }

    private InMemoryRunner getRunner(DefaultArmoryFactory.DynamicContext dynamicContext, String appName) {
        AiRunnerVO runnerConfig = dynamicContext.getRunnerConfig();
        if (runnerConfig == null) {
            log.error("runner 配置为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        Long agentId = runnerConfig.getAgentId();
        Long workflowId = runnerConfig.getWorkflowId();
        if (agentId == null && workflowId == null) {
            throw new IllegalArgumentException("Runner 未配置 Agent 或 Workflow 入口");
        }

        BaseAgent baseAgent = agentId != null
                ? dynamicContext.getAgentMap().get(agentId)
                : dynamicContext.getWorkflowMap().get(workflowId);
        if (baseAgent == null) {
            throw new IllegalArgumentException("Runner 引用了不存在的 Agent/Workflow ID: "
                    + (agentId != null ? agentId : workflowId));
        }

        List<BasePlugin> plugins;
        List<String> pluginNames=runnerConfig.getPluginNameList();
        if(null!=pluginNames&&!pluginNames.isEmpty()){
            plugins = new ArrayList<>();
            for(String pluginName:pluginNames){
                BasePlugin bean = getBean(pluginName);
                plugins.add(bean);
            }
        }else{
            plugins= ImmutableList.of();
        }

        return new InMemoryRunner(baseAgent, appName,plugins);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
