package org.jeecg.modules.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.interaction.entity.FdRecipeCollect;

@Mapper
public interface FdRecipeCollectMapper extends BaseMapper<FdRecipeCollect> {

    @Select("SELECT * FROM fd_recipe_collect WHERE user_id = #{userId} AND recipe_id = #{recipeId} LIMIT 1")
    FdRecipeCollect selectOneIgnoreLogic(@Param("userId") Long userId, @Param("recipeId") Long recipeId);

    @Delete("DELETE FROM fd_recipe_collect WHERE user_id = #{userId} AND recipe_id = #{recipeId}")
    int physicalDeleteByUserRecipe(@Param("userId") Long userId, @Param("recipeId") Long recipeId);

    @Update("UPDATE fd_recipe_collect SET del_flag = 0, update_time = NOW() WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
