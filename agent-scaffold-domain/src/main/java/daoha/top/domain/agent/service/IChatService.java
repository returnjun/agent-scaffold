package daoha.top.domain.agent.service;

import com.google.adk.events.Event;
import daoha.top.domain.agent.model.entity.ChatCommandEntity;
import daoha.top.domain.agent.model.valobj.AiAgentConfigTableVO;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;

public interface IChatService {
    //能看到都有那些agentid
    List<AiAgentConfigTableVO.Agent> queryAiAgentConfigList();
    //那个用户使用了那个agent
    String createSession(String agentId, String userId);
    //消息的传递，如果没传sessionid就创建一个
    List<String> handleMessage(String agentId, String userId, String message);
    //如果传了就是用这个，为空也要创建一个
    List<String> handleMessage(String agentId, String userId, String sessionId, String message);
    //
    Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message);

    List<String> handleMessage(ChatCommandEntity chatCommandEntity);

}
