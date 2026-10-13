package daoha.top.domain.agent.service.armory.data;

import daoha.top.domain.agent.adapter.repository.IAgentRepository;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentVO;
import daoha.top.domain.agent.model.valobj.AiAgentWorkflowVO;
import daoha.top.domain.agent.model.valobj.AiApiVO;
import daoha.top.domain.agent.model.valobj.AiAppVO;
import daoha.top.domain.agent.model.valobj.AiChatModelVO;
import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import daoha.top.domain.agent.model.valobj.AiRunnerVO;
import daoha.top.domain.agent.model.valobj.AiSkillVO;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * @ClassName : LoadDataStrategy
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/14  11:31
 */

@Slf4j
@Service
public class LoadDataStrategy implements ILoadDataStrategy {

    @Resource
    private IAgentRepository agentRepository;

    @Resource(name = "threadPoolExecutor")
    private Executor executor;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) {
        String agentId = armoryCommandEntity.getAgentId();
        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("agentId 不能为空");
        }

        AiAppVO app = agentRepository.queryEnabledAppByAgentId(agentId);
        if (app == null) {
            throw new IllegalArgumentException("未找到已启用的 Agent 应用: " + agentId);
        }

        Long appId = app.getId();
        CompletableFuture<List<AiApiVO>> apiFuture = supplyAsync(() -> agentRepository.queryAiApisByAppId(appId));
        CompletableFuture<List<AiChatModelVO>> modelFuture = supplyAsync(() -> agentRepository.queryChatModelsByAppId(appId));
        CompletableFuture<List<AiMcpToolVO>> mcpFuture = supplyAsync(() -> agentRepository.queryMcpToolsByAppId(appId));
        CompletableFuture<List<AiSkillVO>> skillFuture = supplyAsync(() -> agentRepository.querySkillsByAppId(appId));
        CompletableFuture<List<AiAgentVO>> agentFuture = supplyAsync(() -> agentRepository.queryAgentsByAppId(appId));
        CompletableFuture<List<AiAgentWorkflowVO>> workflowFuture = supplyAsync(() -> agentRepository.queryWorkflowsByAppId(appId));
        CompletableFuture<AiRunnerVO> runnerFuture = supplyAsync(() -> agentRepository.queryRunnerByAppId(appId));

        try {
            CompletableFuture.allOf(apiFuture, modelFuture, mcpFuture, skillFuture,
                    agentFuture, workflowFuture, runnerFuture).join();

            dynamicContext.setApp(app);
            dynamicContext.setApiConfigs(List.copyOf(apiFuture.join()));
            dynamicContext.setChatModelConfigs(List.copyOf(modelFuture.join()));
            dynamicContext.setMcpToolConfigs(List.copyOf(mcpFuture.join()));
            dynamicContext.setSkillConfigs(List.copyOf(skillFuture.join()));
            dynamicContext.setAgentConfigs(List.copyOf(agentFuture.join()));
            dynamicContext.setWorkflowConfigs(List.copyOf(workflowFuture.join()));
            dynamicContext.setRunnerConfig(runnerFuture.join());

            validateLoadedData(dynamicContext);
            log.info("Agent 配置并行加载完成: agentId={}, api={}, model={}, mcp={}, skill={}, agent={}, workflow={}",
                    agentId,
                    dynamicContext.getApiConfigs().size(),
                    dynamicContext.getChatModelConfigs().size(),
                    dynamicContext.getMcpToolConfigs().size(),
                    dynamicContext.getSkillConfigs().size(),
                    dynamicContext.getAgentConfigs().size(),
                    dynamicContext.getWorkflowConfigs().size());
        } catch (CompletionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new IllegalStateException("加载 Agent 配置失败: " + agentId, cause);
        }
    }

    private <T> CompletableFuture<T> supplyAsync(java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    private void validateLoadedData(DefaultArmoryFactory.DynamicContext dynamicContext) {
        if (dynamicContext.getChatModelConfigs().isEmpty()) {
            throw new IllegalStateException("应用未配置可用模型: " + dynamicContext.getApp().getAppCode());
        }
        if (dynamicContext.getAgentConfigs().isEmpty()) {
            throw new IllegalStateException("应用未配置 Agent 节点: " + dynamicContext.getApp().getAppCode());
        }
        if (dynamicContext.getRunnerConfig() == null) {
            throw new IllegalStateException("应用未配置 Runner: " + dynamicContext.getApp().getAppCode());
        }

        Set<Long> apiIds = collectUniqueIds(
                dynamicContext.getApiConfigs().stream().map(AiApiVO::getId).toList(), "API");
        Set<Long> modelIds = collectUniqueIds(
                dynamicContext.getChatModelConfigs().stream().map(AiChatModelVO::getId).toList(), "Model");
        Set<Long> mcpIds = collectUniqueIds(
                dynamicContext.getMcpToolConfigs().stream().map(AiMcpToolVO::getId).toList(), "MCP Tool");
        Set<Long> skillIds = collectUniqueIds(
                dynamicContext.getSkillConfigs().stream().map(AiSkillVO::getId).toList(), "Skill");
        Set<Long> agentIds = collectUniqueIds(
                dynamicContext.getAgentConfigs().stream().map(AiAgentVO::getId).toList(), "Agent");
        Set<Long> workflowIds = collectUniqueIds(
                dynamicContext.getWorkflowConfigs().stream().map(AiAgentWorkflowVO::getId).toList(), "Workflow");

        dynamicContext.getChatModelConfigs().forEach(model ->
                requireReference(apiIds, model.getApiRefId(), "Model " + model.getChatId() + " -> API"));
        dynamicContext.getAgentConfigs().forEach(agent -> {
            requireReference(modelIds, agent.getChatModelId(), "Agent " + agent.getName() + " -> Model");
            requireReferences(mcpIds, agent.getMcpToolIds(), "Agent " + agent.getName() + " -> MCP Tool");
            requireReferences(skillIds, agent.getSkillIds(), "Agent " + agent.getName() + " -> Skill");
        });
        dynamicContext.getWorkflowConfigs().forEach(workflow ->
                requireReferences(agentIds, workflow.getSubAgentIds(),
                        "Workflow " + workflow.getName() + " -> Agent"));

        AiRunnerVO runner = dynamicContext.getRunnerConfig();
        if ((runner.getAgentId() == null) == (runner.getWorkflowId() == null)) {
            throw new IllegalStateException("Runner 必须且只能配置一个 Agent 或 Workflow 入口");
        }
        if (runner.getAgentId() != null) {
            requireReference(agentIds, runner.getAgentId(), "Runner -> Agent");
        } else {
            requireReference(workflowIds, runner.getWorkflowId(), "Runner -> Workflow");
        }
    }

    private Set<Long> collectUniqueIds(List<Long> ids, String type) {
        Set<Long> uniqueIds = new HashSet<>();
        for (Long id : ids) {
            if (id == null || !uniqueIds.add(id)) {
                throw new IllegalStateException(type + " 存在空 ID 或重复 ID: " + id);
            }
        }
        return uniqueIds;
    }

    private void requireReferences(Set<Long> availableIds, List<Long> referenceIds, String relation) {
        if (referenceIds == null) {
            return;
        }
        referenceIds.forEach(id -> requireReference(availableIds, id, relation));
    }

    private void requireReference(Set<Long> availableIds, Long referenceId, String relation) {
        if (referenceId == null || !availableIds.contains(referenceId)) {
            throw new IllegalStateException(relation + " 引用了不存在的 ID: " + referenceId);
        }
    }
}
