package daoha.top.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.runner.InMemoryRunner;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

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
        AiAgentConfigTableVO aiAgentConfigTableVO = armoryCommandEntity.getAiAgentConfigTableVO();
        //获取appid
        String appName = aiAgentConfigTableVO.getAppName();
        //获取agent的信息
        AiAgentConfigTableVO.Agent agent = aiAgentConfigTableVO.getAgent();
        String agentId = agent.getAgentId();
        String agentDesc = agent.getAgentDesc();
        String agentName = agent.getAgentName();

        //获取最后的执行agent
        SequentialAgent sequentialAgent = dynamicContext.getSequentialAgent();

        InMemoryRunner runner = new InMemoryRunner(sequentialAgent, appName);

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

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
