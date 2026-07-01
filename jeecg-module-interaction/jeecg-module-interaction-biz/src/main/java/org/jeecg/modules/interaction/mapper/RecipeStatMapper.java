package org.jeecg.modules.interaction.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RecipeStatMapper {

    @Update("UPDATE fd_recipe SET like_count = #{count} WHERE id = #{recipeId}")
    void updateLikeCount(@Param("recipeId") Long recipeId, @Param("count") int count);

    @Update("UPDATE fd_recipe SET collect_count = #{count} WHERE id = #{recipeId}")
    void updateCollectCount(@Param("recipeId") Long recipeId, @Param("count") int count);
}
