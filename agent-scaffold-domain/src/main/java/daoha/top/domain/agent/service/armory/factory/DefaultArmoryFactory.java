package daoha.top.domain.agent.service.armory.factory;

import com.google.adk.agents.LlmAgent;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.expression.spel.ast.OpNE;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName : DefaultArmoryFactory
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:07
 */

@Service
public class DefaultArmoryFactory {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext{

        private OpenAiApi openAiApi;
        private ChatModel chatModel;
        private Map<String,LlmAgent> agentMap;
        private List<AiAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = new ArrayList<>();

        private Map<String,Object> dataObjects = new HashMap<>();

        public <T> void setValue(String key, T value){
            dataObjects.put(key,value);
        }

        public <T> T getValue(String key){
            return (T) dataObjects.get(key);
        }
    }
}
