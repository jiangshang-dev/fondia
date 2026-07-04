package org.jeecg.modules.member.service.app.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.constant.AiQuotaFeature;
import org.jeecg.modules.member.entity.FdAiQuotaConfig;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.entity.FdMemberSubscription;
import org.jeecg.modules.member.service.IFdAiQuotaConfigService;
import org.jeecg.modules.member.service.IFdAiQuotaUsageService;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.IFdMemberSubscriptionService;
import org.jeecg.modules.member.service.app.IAiQuotaAppService;
import org.jeecg.modules.member.vo.AiQuotaAdminSummaryVO;
import org.jeecg.modules.member.vo.AiQuotaStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AiQuotaAppServiceImpl implements IAiQuotaAppService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int DEFAULT_FREE_LIMIT = 2;

    @Resource
    private IFdAiQuotaConfigService configService;
    @Resource
    private IFdAiQuotaUsageService usageService;
    @Resource
    private IFdMemberSubscriptionService subscriptionService;
    @Resource
    private IFdMemberPlanService planService;

    @Override
    public AiQuotaStatusVO getStatus(Long userId, String featureCode) {
        String code = normalizeFeature(featureCode);
        ResolvedLimit resolved = resolveLimit(userId, code);
        int used = usageService.getUsedCount(userId, code, todayInt());
        return buildStatus(code, resolved, used);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiQuotaStatusVO consume(Long userId, String featureCode) {
        String code = normalizeFeature(featureCode);
        ResolvedLimit resolved = resolveLimit(userId, code);
        int today = todayInt();

        if (resolved.unlimited) {
            usageService.incrementUnlimited(userId, code, today);
            int used = usageService.getUsedCount(userId, code, today);
            return buildStatus(code, resolved, used);
        }

        int affected = usageService.incrementIfUnderLimit(userId, code, today, resolved.limit);
        if (affected <= 0) {
            throw new JeecgBootException(buildQuotaExceededMessage(resolved));
        }
        int used = usageService.getUsedCount(userId, code, today);
        return buildStatus(code, resolved, used);
    }

    @Override
    public AiQuotaAdminSummaryVO getAdminSummary(String featureCode) {
        String code = normalizeFeature(featureCode);
        FdAiQuotaConfig config = configService.getByFeatureCode(code);
        if (config == null) {
            LambdaQueryWrapper<FdAiQuotaConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FdAiQuotaConfig::getFeatureCode, code).last("LIMIT 1");
            config = configService.getOne(wrapper, false);
        }

        AiQuotaAdminSummaryVO summary = new AiQuotaAdminSummaryVO();
        summary.setFeatureCode(code);
        if (config != null) {
            summary.setConfigId(config.getId());
            summary.setFeatureName(config.getFeatureName());
            summary.setFreeDailyLimit(config.getFreeDailyLimit());
            summary.setStatus(config.getStatus());
            summary.setRemark(config.getRemark());
        } else {
            summary.setFeatureName("AI营养专属方案");
            summary.setFreeDailyLimit(DEFAULT_FREE_LIMIT);
            summary.setStatus(1);
        }

        LambdaQueryWrapper<FdMemberPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.orderByAsc(FdMemberPlan::getSortNo);
        List<FdMemberPlan> plans = planService.list(planWrapper);
        List<AiQuotaAdminSummaryVO.PlanQuotaItemVO> items = new ArrayList<>();
        for (FdMemberPlan plan : plans) {
            AiQuotaAdminSummaryVO.PlanQuotaItemVO item = new AiQuotaAdminSummaryVO.PlanQuotaItemVO();
            item.setId(plan.getId());
            item.setPlanCode(plan.getPlanCode());
            item.setName(plan.getName());
            item.setDurationDays(plan.getDurationDays());
            item.setAiNutritionDailyLimit(plan.getAiNutritionDailyLimit());
            item.setStatus(plan.getStatus());
            items.add(item);
        }
        summary.setPlans(items);
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdminConfig(String featureCode, Integer freeDailyLimit, Integer status, String remark) {
        String code = normalizeFeature(featureCode);
        if (freeDailyLimit == null || freeDailyLimit < 0) {
            throw new JeecgBootException("免费次数不能小于0");
        }

        FdAiQuotaConfig config = configService.getByFeatureCode(code);
        if (config == null) {
            LambdaQueryWrapper<FdAiQuotaConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FdAiQuotaConfig::getFeatureCode, code).last("LIMIT 1");
            config = configService.getOne(wrapper, false);
        }

        Date now = new Date();
        if (config == null) {
            config = new FdAiQuotaConfig();
            config.setFeatureCode(code);
            config.setFeatureName("AI营养专属方案");
            config.setCreateTime(now);
            config.setDelFlag(0);
        }
        config.setFreeDailyLimit(freeDailyLimit);
        config.setStatus(status == null ? 1 : status);
        config.setRemark(remark);
        config.setUpdateTime(now);
        configService.saveOrUpdate(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePlanLimits(List<AiQuotaAdminSummaryVO.PlanQuotaItemVO> plans) {
        if (plans == null || plans.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (AiQuotaAdminSummaryVO.PlanQuotaItemVO item : plans) {
            if (item.getId() == null) {
                continue;
            }
            if (item.getAiNutritionDailyLimit() != null && item.getAiNutritionDailyLimit() < 0) {
                throw new JeecgBootException("套餐「" + item.getPlanCode() + "」次数不能小于0");
            }
            FdMemberPlan plan = planService.getById(item.getId());
            if (plan == null) {
                continue;
            }
            plan.setAiNutritionDailyLimit(item.getAiNutritionDailyLimit());
            plan.setUpdateTime(now);
            planService.updateById(plan);
        }
    }

    private AiQuotaStatusVO buildStatus(String featureCode, ResolvedLimit resolved, int used) {
        AiQuotaStatusVO vo = new AiQuotaStatusVO();
        vo.setFeatureCode(featureCode);
        vo.setMember(resolved.member);
        vo.setPlanCode(resolved.planCode);
        vo.setPlanName(resolved.planName);
        vo.setUsedToday(used);
        if (resolved.unlimited) {
            vo.setDailyLimit(-1);
            vo.setRemaining(-1);
            vo.setAvailable(true);
            return vo;
        }
        vo.setDailyLimit(resolved.limit);
        int remaining = Math.max(0, resolved.limit - used);
        vo.setRemaining(remaining);
        vo.setAvailable(remaining > 0);
        return vo;
    }

    private String buildQuotaExceededMessage(ResolvedLimit resolved) {
        if (resolved.member) {
            return "今日专属方案次数已用完（" + resolved.planName + " 每日 " + resolved.limit + " 次），请明日再试";
        }
        return "今日免费次数已用完（每日 " + resolved.limit + " 次），开通会员可享更多专属方案次数";
    }

    private ResolvedLimit resolveLimit(Long userId, String featureCode) {
        FdMemberSubscription active = findActiveSubscription(userId);
        if (active != null && StringUtils.hasText(active.getPlanCode())) {
            FdMemberPlan plan = findPlanByCode(active.getPlanCode());
            if (plan != null && plan.getAiNutritionDailyLimit() != null) {
                ResolvedLimit resolved = new ResolvedLimit();
                resolved.member = true;
                resolved.planCode = plan.getPlanCode();
                resolved.planName = plan.getName();
                if (plan.getAiNutritionDailyLimit() == 0) {
                    resolved.unlimited = true;
                    resolved.limit = 0;
                    return resolved;
                }
                resolved.limit = plan.getAiNutritionDailyLimit();
                return resolved;
            }
        }

        ResolvedLimit resolved = new ResolvedLimit();
        resolved.member = active != null;
        if (active != null) {
            resolved.planCode = active.getPlanCode();
            FdMemberPlan plan = findPlanByCode(active.getPlanCode());
            resolved.planName = plan != null ? plan.getName() : active.getPlanCode();
        }
        resolved.limit = resolveFreeLimit(featureCode);
        return resolved;
    }

    private int resolveFreeLimit(String featureCode) {
        FdAiQuotaConfig config = configService.getByFeatureCode(featureCode);
        if (config != null && config.getFreeDailyLimit() != null) {
            return config.getFreeDailyLimit();
        }
        return DEFAULT_FREE_LIMIT;
    }

    private FdMemberSubscription findActiveSubscription(Long userId) {
        long now = System.currentTimeMillis();
        LambdaQueryWrapper<FdMemberSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberSubscription::getUserId, userId)
                .eq(FdMemberSubscription::getStatus, 1)
                .gt(FdMemberSubscription::getExpireTime, now)
                .orderByDesc(FdMemberSubscription::getExpireTime)
                .last("LIMIT 1");
        return subscriptionService.getOne(wrapper, false);
    }

    private FdMemberPlan findPlanByCode(String planCode) {
        LambdaQueryWrapper<FdMemberPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberPlan::getPlanCode, planCode).last("LIMIT 1");
        return planService.getOne(wrapper, false);
    }

    private String normalizeFeature(String featureCode) {
        if (!StringUtils.hasText(featureCode)) {
            return AiQuotaFeature.AI_NUTRITION;
        }
        return featureCode.trim().toUpperCase();
    }

    private int todayInt() {
        return Integer.parseInt(LocalDate.now().format(DATE_FMT));
    }

    private static class ResolvedLimit {
        private boolean member;
        private boolean unlimited;
        private String planCode;
        private String planName;
        private int limit;
    }
}
