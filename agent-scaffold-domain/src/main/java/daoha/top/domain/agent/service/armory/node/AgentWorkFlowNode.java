package daoha.top.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.enums.AgentTypeEnum;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.domain.agent.service.armory.node.workflow.LoopAgentNode;
import daoha.top.domain.agent.service.armory.node.workflow.ParallelAgentNode;
import daoha.top.domain.agent.service.armory.node.workflow.SequentialAgentNode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName : AgentWorkFlowNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  21:10
 */
@Slf4j
@Service
public class AgentWorkFlowNode extends AbstractArmorySupport {

    @Resource
    private LoopAgentNode loopAgentNode;

    @Resource
    private ParallelAgentNode parallelAgentNode;

    @Resource
    private SequentialAgentNode  sequentialAgentNode;


    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai agent的workflow节点装配");

        AiAgentConfigTableVO aiAgentConfigTableVO = armoryCommandEntity.getAiAgentConfigTableVO();
        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = aiAgentConfigTableVO.getModule().getAgentWorkflows();

        if(null==agentWorkflows||agentWorkflows.size()==0){
            throw new RuntimeException("agentWorkflow is null");
        }

        dynamicContext.setAgentWorkflows(agentWorkflows);

        return router(armoryCommandEntity,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();

        AiAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.get(0);

        String type = agentWorkflow.getType();

        AgentTypeEnum agentTypeEnum = AgentTypeEnum.formType(type);

        if(null==agentTypeEnum){
            throw new RuntimeException("agentTypeEnum type is error");
        }
        String node = agentTypeEnum.getNode();

        return switch (node){
            case "loop" -> loopAgentNode;
            case "parallel" -> parallelAgentNode;
            case "sequential" -> sequentialAgentNode;
            default -> defaultStrategyHandler;
        };
    }
}
