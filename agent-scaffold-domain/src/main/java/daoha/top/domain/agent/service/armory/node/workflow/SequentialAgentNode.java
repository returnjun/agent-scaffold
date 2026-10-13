package daoha.top.domain.agent.service.armory.node.workflow;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.SequentialAgent;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.AiAgentWorkflowVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import daoha.top.domain.agent.service.armory.node.AgentWorkFlowNode;
import daoha.top.domain.agent.service.armory.node.RunnerNode;
import io.reactivex.rxjava3.core.Single;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName : SequentialAgentNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  21:12
 */
@Slf4j
@Service("sequentialAgentNode")
public class SequentialAgentNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("agent装配的sequent序列的装配");

        AiAgentWorkflowVO currentAgentWorkflow = dynamicContext.getCurrentAgentWorkflow();

        List<Long> subAgentIds = currentAgentWorkflow.getSubAgentIds();
        List<BaseAgent> baseAgents = dynamicContext.queryAgentList(subAgentIds);

        SequentialAgent sequentialAgent =
                SequentialAgent.builder()
                        .name(currentAgentWorkflow.getName())
                        .description(currentAgentWorkflow.getDescription())
                        .subAgents(baseAgents)
                        .build();

        if (dynamicContext.getWorkflowMap().putIfAbsent(currentAgentWorkflow.getId(), sequentialAgent) != null) {
            throw new IllegalArgumentException("重复的 Workflow ID: " + currentAgentWorkflow.getId());
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("agentWorkFlowNode");
    }
}
