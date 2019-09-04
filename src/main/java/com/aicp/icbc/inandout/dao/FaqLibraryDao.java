package com.aicp.icbc.inandout.dao;

import com.aicp.icbc.inandout.dto.FaqLibraryDto;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Component
public interface FaqLibraryDao {

    @Insert("insert into t_user (user_name,pass_word) VALUES(#{userName},#{passWord})")
    public void create(@Param("userName") String userName, @Param("passWord") String passWord);

    @Select("select *  from faq_library where id = #{id}")
    public FaqLibraryDto selectById(@Param("id") Integer id);

    @Select("select name from faq_library where id = #{id}")
    public String selectNameById(@Param("id") Integer id);

    @Select("select *  from faq_library where agent_id = #{agentId}")
    public List<FaqLibraryDto> selectListByAgentId(@Param("agentId") Integer agentId);

    @Select("select parent_id  from faq_library where id = #{id}")
    public Integer selectParentIdById(@Param("id") Integer id);

    @Select("select dir_id from faq where id = #{faqId}")
    public Integer selectFaqDirIdByFaqId(@Param("faqId") String faqId);
}
