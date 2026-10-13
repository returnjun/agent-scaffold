package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiAgentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LLM Agent 查询接口。
 */
@Mapper
public interface IAiAgentDao {

    AiAgentPO queryById(@Param("id") Long id);

    List<AiAgentPO> queryEnabledByAppId(@Param("appId") Long appId);

    AiAgentPO queryEnabledByAppIdAndAgentCode(@Param("appId") Long appId,
                                             @Param("agentCode") String agentCode);
}
