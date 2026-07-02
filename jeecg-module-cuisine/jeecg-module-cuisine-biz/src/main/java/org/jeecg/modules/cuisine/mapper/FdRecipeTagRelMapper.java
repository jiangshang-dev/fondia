package org.jeecg.modules.cuisine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.cuisine.entity.FdRecipeTagRel;

@Mapper
public interface FdRecipeTagRelMapper extends BaseMapper<FdRecipeTagRel> {

    /**
     * 物理删除菜谱标签关联（关联表含 uk_recipe_tag(recipe_id,tag_id,del_flag)，逻辑删除会导致重复更新冲突）
     */
    @Delete("DELETE FROM fd_recipe_tag_rel WHERE recipe_id = #{recipeId}")
    void physicalDeleteByRecipeId(@Param("recipeId") Long recipeId);
}
