package org.jeecg.common.util;

import cn.hutool.core.util.IdUtil;

/**
 * id生成器工具类
 *
 * @author mrhum
 */
public final class IdGeneratorUtil {

    /**
     * 简化的UUID，去掉了横线
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String simpleUUID() {
        return IdUtil.simpleUUID();
    }

    /**
     * 获取Snowflake 的 nextId
     * 终端ID 数据中心ID 默认为PID和MAC地址生成
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String getSnowflakeNextIdStr() {
        return IdUtil.getSnowflakeNextIdStr();
    }
}
