package daoha.top.domain.agent.service.armory.factory;

import com.google.adk.agents.BaseAgent;
import com.google.adk.tools.BaseTool;
import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.*;
import daoha.top.domain.agent.service.armory.node.RootNode;
import daoha.top.types.design.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName : DefaultArmoryFactory
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:07
 */

@Service
public class DefaultArmoryFactory {

    @Resource
    private RootNode rootNode;

    @Resource
    private ApplicationContext applicationContext;

    public StrategyHandler<ArmoryCommandEntity, DynamicContext, AiAgentRegisterVO> armoryStrategyHandler() {
        return rootNode;
    }

    public AiAgentRegisterVO getAiAgentRegisterVOById(String agentId) {
        return applicationContext.getBean(agentId,AiAgentRegisterVO.class);
    }

    public boolean hasAiAgentRegisterVO(String agentId) {
        return applicationContext.containsBean(agentId);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext{

        private AiAppVO app;

        @Builder.Default
        private List<AiApiVO> apiConfigs = Collections.emptyList();

        @Builder.Default
        private List<AiChatModelVO> chatModelConfigs = Collections.emptyList();

        @Builder.Default
        private List<AiMcpToolVO> mcpToolConfigs = Collections.emptyList();

        @Builder.Default
        private List<AiSkillVO> skillConfigs = Collections.emptyList();

        @Builder.Default
        private List<AiAgentVO> agentConfigs = Collections.emptyList();

        @Builder.Default
        private List<AiAgentWorkflowVO> workflowConfigs = Collections.emptyList();

        private AiRunnerVO runnerConfig;

        @Builder.Default
        private Map<Long, AiApiVO> aiApis = new LinkedHashMap<>();

        @Builder.Default
        private Map<Long, ChatModel> chatModels = new LinkedHashMap<>();

        /** 类型前缀 + 工具表主键 -> 该配置创建出的一个或多个 ADK 工具。 */
        @Builder.Default
        private Map<String, List<BaseTool>> tools = new LinkedHashMap<>();


        @Builder.Default
        private Map<Long, BaseAgent> agentMap = new LinkedHashMap<>();

        @Builder.Default
        private Map<Long, BaseAgent> workflowMap = new LinkedHashMap<>();

        @Builder.Default
        private AtomicInteger currentStepIndex = new AtomicInteger(0);
        private AiAgentWorkflowVO currentAgentWorkflow;

        @Builder.Default
        private Map<String,Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value){
            dataObjects.put(key,value);
        }

        public <T> T getValue(String key){
            return (T) dataObjects.get(key);
        }

        public List<BaseAgent> queryAgentList(List<Long> agentIds){
            if (agentIds == null || agentIds.isEmpty() || agentMap == null) {
                return Collections.emptyList();
            }

            List<BaseAgent> agents = new ArrayList<>();
            for (Long agentId : agentIds) {
                BaseAgent agent = agentMap.get(agentId);
                if (agent == null) {
                    throw new IllegalArgumentException("未找到子 Agent: " + agentId);
                }
                agents.add(agent);
            }

            return agents;
        }

        public void addcurrentStepIndex(){
            currentStepIndex.incrementAndGet();
        }

        public int getCurrentStepIndex(){
            return currentStepIndex.get();
        }
    }
}
