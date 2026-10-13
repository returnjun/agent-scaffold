package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiAgentWorkflowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent 工作流查询接口。
 */
@Mapper
public interface IAiAgentWorkflowDao {

    AiAgentWorkflowPO queryById(@Param("id") Long id);

    List<AiAgentWorkflowPO> queryEnabledByAppId(@Param("appId") Long appId);

    AiAgentWorkflowPO queryEnabledByAppIdAndWorkflowCode(@Param("appId") Long appId,
                                                         @Param("workflowCode") String workflowCode);
}
