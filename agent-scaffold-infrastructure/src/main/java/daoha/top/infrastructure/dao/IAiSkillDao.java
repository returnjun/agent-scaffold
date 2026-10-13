package daoha.top.infrastructure.dao;

import daoha.top.infrastructure.dao.po.AiSkillPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Skill 查询接口。
 */
@Mapper
public interface IAiSkillDao {

    AiSkillPO queryById(@Param("id") Long id);

    List<AiSkillPO> queryEnabledByAppId(@Param("appId") Long appId);
}
