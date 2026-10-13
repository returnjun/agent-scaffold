package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiRunnerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Agent 运行入口查询接口。
 */
@Mapper
public interface IAiRunnerDao {

    AiRunnerPO queryById(@Param("id") Long id);

    AiRunnerPO queryEnabledByAppId(@Param("appId") Long appId);
}
