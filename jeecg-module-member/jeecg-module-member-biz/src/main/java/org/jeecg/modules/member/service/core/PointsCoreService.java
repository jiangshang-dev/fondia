package org.jeecg.modules.member.service.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.FdPointsAccount;
import org.jeecg.modules.member.entity.FdPointsLedger;
import org.jeecg.modules.member.mapper.FdUserMemberSyncMapper;
import org.jeecg.modules.member.service.IFdPointsAccountService;
import org.jeecg.modules.member.service.IFdPointsLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointsCoreService {

    @Resource
    private IFdPointsAccountService accountService;
    @Resource
    private IFdPointsLedgerService ledgerService;
    @Resource
    private FdUserMemberSyncMapper userSyncMapper;

    @Transactional(rollbackFor = Exception.class)
    public int addPoints(Long userId, int delta, String bizType, String bizId, String title) {
        if (delta <= 0) {
            throw new JeecgBootException("积分增量必须大于0");
        }
        long now = System.currentTimeMillis();
        FdPointsAccount account = getOrCreateAccount(userId, now);
        int newBalance = account.getBalance() + delta;
        account.setBalance(newBalance);
        account.setTotalEarned(account.getTotalEarned() + delta);
        account.setUpdateTime(now);
        accountService.updateById(account);
        saveLedger(userId, delta, newBalance, bizType, bizId, title, now);
        userSyncMapper.syncPointsOnly(userId, newBalance, now);
        return newBalance;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deductPoints(Long userId, int delta, String bizType, String bizId, String title) {
        if (delta <= 0) {
            throw new JeecgBootException("扣减积分必须大于0");
        }
        long now = System.currentTimeMillis();
        FdPointsAccount account = getOrCreateAccount(userId, now);
        if (account.getBalance() < delta) {
            throw new JeecgBootException("积分不足");
        }
        int newBalance = account.getBalance() - delta;
        account.setBalance(newBalance);
        account.setTotalSpent(account.getTotalSpent() + delta);
        account.setUpdateTime(now);
        accountService.updateById(account);
        saveLedger(userId, -delta, newBalance, bizType, bizId, title, now);
        userSyncMapper.syncPointsOnly(userId, newBalance, now);
        return newBalance;
    }

    public int getBalance(Long userId) {
        LambdaQueryWrapper<FdPointsAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsAccount::getUserId, userId);
        FdPointsAccount account = accountService.getOne(wrapper, false);
        return account != null ? account.getBalance() : 0;
    }

    private FdPointsAccount getOrCreateAccount(Long userId, long now) {
        LambdaQueryWrapper<FdPointsAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsAccount::getUserId, userId);
        FdPointsAccount account = accountService.getOne(wrapper, false);
        if (account == null) {
            account = new FdPointsAccount();
            account.setUserId(userId);
            account.setBalance(0);
            account.setFrozen(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
            account.setVersion(0);
            account.setCreateTime(now);
            account.setUpdateTime(now);
            accountService.save(account);
        }
        return account;
    }

    private void saveLedger(Long userId, int change, int balanceAfter, String bizType, String bizId, String title, long now) {
        FdPointsLedger ledger = new FdPointsLedger();
        ledger.setUserId(userId);
        ledger.setChangeAmount(change);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setBizType(bizType);
        ledger.setBizId(bizId);
        ledger.setTitle(title);
        ledger.setCreateTime(now);
        ledgerService.save(ledger);
    }
}
