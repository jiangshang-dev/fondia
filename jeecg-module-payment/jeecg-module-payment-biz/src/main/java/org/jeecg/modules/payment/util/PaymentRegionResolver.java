package org.jeecg.modules.payment.util;

import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.payment.constant.PayRegion;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PaymentRegionResolver {

    private static final Set<String> CN_PRIVATE_PREFIXES = new HashSet<>(Arrays.asList(
            "10.", "172.16.", "172.17.", "172.18.", "172.19.", "172.20.", "172.21.", "172.22.",
            "172.23.", "172.24.", "172.25.", "172.26.", "172.27.", "172.28.", "172.29.", "172.30.",
            "172.31.", "192.168.", "127."
    ));

    private PaymentRegionResolver() {
    }

    /**
     * 简化版地区判断：私网/本地视为国内；后续可接入 GeoIP。
     */
    public static String resolveRegion(String ip) {
        if (oConvertUtils.isEmpty(ip)) {
            return PayRegion.CN;
        }
        String normalized = ip.trim();
        if ("0:0:0:0:0:0:0:1".equals(normalized) || "::1".equals(normalized)) {
            return PayRegion.CN;
        }
        for (String prefix : CN_PRIVATE_PREFIXES) {
            if (normalized.startsWith(prefix)) {
                return PayRegion.CN;
            }
        }
        try {
            InetAddress address = InetAddress.getByName(normalized);
            if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
                return PayRegion.CN;
            }
        } catch (Exception ignored) {
            return PayRegion.CN;
        }
        // 公网 IP 暂无法精确区分时默认国内，可通过 fondia.payment.overseas-ip-enabled=false 强制国内
        return PayRegion.CN;
    }
}
