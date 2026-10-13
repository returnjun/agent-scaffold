package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiMcpToolPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MCP/本地工具查询接口。
 */
@Mapper
public interface IAiMcpToolDao {

    AiMcpToolPO queryById(@Param("id") Long id, @Param("masterKey") String masterKey);

    List<AiMcpToolPO> queryEnabledByAppId(@Param("appId") Long appId,
                                         @Param("masterKey") String masterKey);
}
