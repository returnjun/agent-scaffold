package daoha.top.domain.agent.service.chat;

import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import daoha.top.domain.agent.model.entity.ChatCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import daoha.top.domain.agent.model.valobj.AiAgentRegisterVO;
import daoha.top.domain.agent.model.valobj.properties.AiAgentAutoConfigProperties;
import daoha.top.domain.agent.service.IChatService;
import daoha.top.domain.agent.service.armory.factory.DefaultArmoryFactory;
import daoha.top.types.enums.ResponseCode;
import daoha.top.types.exception.AppException;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName : ChatService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/9/6  16:02
 */
@Slf4j
@Service
public class ChatService implements IChatService {

    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;

    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    private final Map<String,String> userSessions = new ConcurrentHashMap<>();


    @Override
    public List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList() {
        Map<String, AiAgentConfigTableVO> tables = aiAgentAutoConfigProperties.getTables();
        List<AiAgentConfigTableVO.Agent> agents = new ArrayList<>();
        if(null!=tables){
            for(AiAgentConfigTableVO table:tables.values()){
                agents.add(table.getAgent());
            }
        }
        return agents;
    }

    @Override
    public String createSession(String agentId, String userId) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVOById(agentId);
        if(null==aiAgentRegisterVO){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        String appName = aiAgentRegisterVO.getAppName();
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();

        String sessionKey  = buildSessionKey(agentId, userId);
        return userSessions.computeIfAbsent(sessionKey , key -> {
            Session session = runner.sessionService().createSession(appName, userId)
                    .blockingGet();
            return session.id();
        });
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String message) {
        AiAgentRegisterVO aiAgentRegisterVOById = defaultArmoryFactory.getAiAgentRegisterVOById(agentId);
        if(null==aiAgentRegisterVOById){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        String sessionId = createSession(agentId,userId);
        return handleMessage(agentId,userId,sessionId,message);
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVOById = defaultArmoryFactory.getAiAgentRegisterVOById(agentId);
        if(null==aiAgentRegisterVOById){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        InMemoryRunner runner = aiAgentRegisterVOById.getRunner();
        Content userMessage = Content.fromParts(Part.fromText(message));
        Flowable<Event> eventFlowable = runner.runAsync(userId, sessionId, userMessage);

        List<String> outputs = new ArrayList<>();
        finalResponseEvents(eventFlowable)
                .blockingForEach(event -> outputs.add(event.stringifyContent()));
        return outputs;
    }

    @Override
    public Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVOById = defaultArmoryFactory.getAiAgentRegisterVOById(agentId);
        if(null==aiAgentRegisterVOById){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        InMemoryRunner runner = aiAgentRegisterVOById.getRunner();
        Content userMessage = Content.fromParts(Part.fromText(message));
        return finalResponseEvents(runner.runAsync(userId, sessionId, userMessage));
    }

    @Override
    public List<String> handleMessage(ChatCommandEntity chatCommandEntity) {
        String agentId = chatCommandEntity.getAgentId();
        AiAgentRegisterVO aiAgentRegisterVOById = defaultArmoryFactory.getAiAgentRegisterVOById(agentId);
        if(null==aiAgentRegisterVOById){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        List<Part> parts = new ArrayList<>();

        List<ChatCommandEntity.Content.Text> texts = chatCommandEntity.getTexts();
        if(null!=texts && !texts.isEmpty()){
            for(ChatCommandEntity.Content.Text text:texts){
                parts.add(Part.fromText(text.getMessage()));
            }
        }

        List<ChatCommandEntity.Content.File> files = chatCommandEntity.getFiles();
        if (null != files && !files.isEmpty()) {
            for (ChatCommandEntity.Content.File file : files) {
                parts.add(Part.fromUri(file.getFileUri(), file.getMimeType()));
            }
        }

        List<ChatCommandEntity.Content.InlineData> inlineDatas = chatCommandEntity.getInlineDatas();
        if (null != inlineDatas && !inlineDatas.isEmpty()) {
            for (ChatCommandEntity.Content.InlineData inlineData : inlineDatas) {
                parts.add(Part.fromBytes(inlineData.getBytes(), inlineData.getMimeType()));
            }
        }

        Content content = Content.builder().role("user").parts(parts).build();

        // 获取运行体
        InMemoryRunner runner = aiAgentRegisterVOById.getRunner();

        Flowable<Event> events = runner.runAsync(chatCommandEntity.getUserId(), chatCommandEntity.getSessionId(), content);

        List<String> outputs = new ArrayList<>();
        finalResponseEvents(events)
                .blockingForEach(event -> outputs.add(event.stringifyContent()));

        return outputs;
    }

    /**
     * ADK 会把模型片段、工具调用、工具响应和最终回答都作为 Event 发出。
     * 对外只保留本轮最后一个非空最终回答，避免把内部执行细节暴露给客户端。
     */
    private Flowable<Event> finalResponseEvents(Flowable<Event> events) {
        return events
                .filter(event -> event.finalResponse()
                        && !event.stringifyContent().isBlank())
                .takeLast(1);
    }

    private String buildSessionKey(String agentId, String userId) {
        return agentId + ":" + userId;
    }
}
