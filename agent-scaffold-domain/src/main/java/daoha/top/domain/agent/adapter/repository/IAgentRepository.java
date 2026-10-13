package daoha.top.domain.agent.adapter.repository;

import daoha.top.domain.agent.model.valobj.AiAgentVO;
import daoha.top.domain.agent.model.valobj.AiAgentWorkflowVO;
import daoha.top.domain.agent.model.valobj.AiApiVO;
import daoha.top.domain.agent.model.valobj.AiAppVO;
import daoha.top.domain.agent.model.valobj.AiChatModelVO;
import daoha.top.domain.agent.model.valobj.AiMcpToolVO;
import daoha.top.domain.agent.model.valobj.AiRunnerVO;
import daoha.top.domain.agent.model.valobj.AiSkillVO;

import java.util.List;

/**
 * Agent 配置仓储端口。领域层只依赖该接口，不感知 MyBatis 和数据库表结构。
 */
public interface IAgentRepository {

    List<AiAppVO> queryEnabledApps();

    AiAppVO queryEnabledAppByAgentId(String agentId);

    List<AiApiVO> queryAiApisByAppId(Long appId);

    List<AiChatModelVO> queryChatModelsByAppId(Long appId);

    List<AiMcpToolVO> queryMcpToolsByAppId(Long appId);

    List<AiSkillVO> querySkillsByAppId(Long appId);

    List<AiAgentVO> queryAgentsByAppId(Long appId);

    List<AiAgentWorkflowVO> queryWorkflowsByAppId(Long appId);

    AiRunnerVO queryRunnerByAppId(Long appId);
}
