package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiModelPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型查询接口。
 */
@Mapper
public interface IAiModelDao {

    AiModelPO queryById(@Param("id") Long id);

    List<AiModelPO> queryEnabledByAppId(@Param("appId") Long appId);
}
