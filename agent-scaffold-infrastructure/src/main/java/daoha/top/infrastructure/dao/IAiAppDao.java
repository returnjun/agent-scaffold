package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiAppPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体应用查询接口。
 */
@Mapper
public interface IAiAppDao {

    AiAppPO queryById(@Param("id") Long id);

    AiAppPO queryEnabledByAppCode(@Param("appCode") String appCode);

    AiAppPO queryEnabledByAgentCode(@Param("agentCode") String agentCode);

    List<AiAppPO> queryAllEnabled();
}
