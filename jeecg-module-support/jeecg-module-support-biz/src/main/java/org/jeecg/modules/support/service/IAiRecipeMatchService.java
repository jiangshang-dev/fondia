package org.jeecg.modules.support.service;

import org.jeecg.modules.support.vo.AiRecipeCardVO;

import java.util.List;

public interface IAiRecipeMatchService {

    List<AiRecipeCardVO> matchFromReply(String aiReply);
}
