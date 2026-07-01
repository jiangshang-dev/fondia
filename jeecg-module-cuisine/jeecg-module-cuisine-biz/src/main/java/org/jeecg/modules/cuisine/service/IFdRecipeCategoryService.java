package org.jeecg.modules.cuisine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.cuisine.entity.FdRecipeCategory;
import org.jeecg.modules.cuisine.vo.CategoryAdminTreeVO;
import org.jeecg.modules.cuisine.vo.CategoryBriefVO;
import org.jeecg.modules.cuisine.vo.CategoryTreeVO;
import org.jeecg.modules.cuisine.vo.CuisineDetailVO;

import java.util.List;

public interface IFdRecipeCategoryService extends IService<FdRecipeCategory> {

    List<CategoryBriefVO> listHotCuisines();

    List<CategoryBriefVO> listAllCuisines();

    List<CategoryBriefVO> listSubCategories(Long cuisineId);

    CuisineDetailVO getCuisineDetail(Long cuisineId);

    CategoryTreeVO getCategoryTree(Long cuisineId);

    /**
     * 管理端树形列表
     */
    List<CategoryAdminTreeVO> listAdminTree(String name, Integer categoryLevel, Integer status);
}
