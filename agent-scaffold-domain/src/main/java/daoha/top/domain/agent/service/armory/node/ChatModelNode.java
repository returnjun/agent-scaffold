package daoha.top.domain.agent.service.armory.node;

import daoha.top.domain.agent.model.entity.ArmoryCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.service.armory.AbstractArmorySupport;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.design.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

/**
 * @ClassName : ChatModelNode
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:33
 */
@Slf4j
@Service
public class ChatModelNode extends AbstractArmorySupport {

    @Resource
    private AgentNode  agentNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {

        log.info("chatModel 装配");

        // 获取上下文对象
        AiAgentConfigTableVO.Module.AiApi aiApi = dynamicContext.getAiApi();

        // 获取配置对象
        AiAgentConfigTableVO aiAgentConfigTableVO = armoryCommandEntity.getAiAgentConfigTableVO();
        AiAgentConfigTableVO.Module.ChatModel chatModelConfig = aiAgentConfigTableVO.getModule().getChatModel();
        ChatModel chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .baseUrl(resolveOpenAiBaseUrl(aiApi))
                        .apiKey(aiApi.getApiKey())
                        .model(chatModelConfig.getModel())
                        .build())
                .build();

        dynamicContext.setChatModel(chatModel);

        return router(armoryCommandEntity, dynamicContext);
    }

    /**
     * Spring AI 2.x uses the official OpenAI SDK, which appends /chat/completions
     * to the configured base URL. Convert the legacy full completions path to
     * the SDK-style base URL while keeping existing YAML files compatible.
     */
    private String resolveOpenAiBaseUrl(AiAgentConfigTableVO.Module.AiApi aiApi) {
        String baseUrl = StringUtils.removeEnd(aiApi.getBaseUrl(), "/");
        String completionsPath = StringUtils.defaultIfBlank(aiApi.getCompletionsPath(), "/v1/chat/completions");
        completionsPath = "/" + StringUtils.removeStart(completionsPath, "/");

        String endpointSuffix = "/chat/completions";
        if (!completionsPath.endsWith(endpointSuffix)) {
            throw new IllegalArgumentException("OpenAI completionsPath must end with " + endpointSuffix);
        }

        return baseUrl + StringUtils.removeEnd(completionsPath, endpointSuffix);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return agentNode;
    }
}
