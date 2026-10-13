package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiApiPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型 API 查询接口。
 */
@Mapper
public interface IAiApiDao {

    AiApiPO queryById(@Param("id") Long id, @Param("masterKey") String masterKey);

    List<AiApiPO> queryEnabledByAppId(@Param("appId") Long appId,
                                     @Param("masterKey") String masterKey);
}
