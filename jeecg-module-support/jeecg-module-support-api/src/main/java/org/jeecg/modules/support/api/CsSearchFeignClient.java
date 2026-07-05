package org.jeecg.modules.support.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.support.api.vo.RecipeSearchFeignVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 调用 jeecg-search 微服务搜索菜谱（替代 RestTemplate + 服务名 URL）
 */
@FeignClient(
        name = "jeecg-search",
        contextId = "csSearchFeignClient",
        url = "${jeecg.support.search-feign-url:}"
)
public interface CsSearchFeignClient {

    @GetMapping("/search/recipe")
    Result<Page<RecipeSearchFeignVO>> searchRecipe(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize);
}
