package daoha.top.domain.agent.service.armory;

import daoha.top.domain.agent.adapter.repository.IAgentRepository;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAppVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.IArmoryService;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName : ArmoryService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:01
 */
@Service
public class ArmoryService implements IArmoryService {
    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Resource
    private IAgentRepository agentRepository;

    @Value("${ai.agent.bootstrap.agent-ids:}")
    private String bootstrapAgentIds;

    @Override
    public void acceptArmoryAgents() throws Exception {
        List<AiAppVO> apps = agentRepository.queryEnabledApps();
        if (bootstrapAgentIds != null && !bootstrapAgentIds.isBlank()) {
            Set<String> selectedIds = Arrays.stream(bootstrapAgentIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .collect(Collectors.toSet());
            apps = apps.stream().filter(app -> selectedIds.contains(app.getAgentId())).toList();
            if (apps.size() != selectedIds.size()) {
                throw new IllegalArgumentException("ai.agent.bootstrap.agent-ids 包含未启用或不存在的 Agent ID");
            }
        }
        for (AiAppVO app : apps) {
            acceptArmoryAgent(app.getAgentId());
        }
    }

    @Override
    public void acceptArmoryAgent(String agentId) throws Exception {
        StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> handler =
                defaultArmoryFactory.armoryStrategyHandler();
        handler.apply(
                ArmoryCommandEntity.builder().agentId(agentId).build(),
                new DefaultArmoryFactory.DynamicContext()
        );
    }
}
