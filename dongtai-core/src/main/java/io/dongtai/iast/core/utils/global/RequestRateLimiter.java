package io.dongtai.iast.core.utils.global;

import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.RateLimiterWithCapacity;
import io.dongtai.iast.core.utils.PropertyUtils;

/**
 * 全局请求限速器
 *
 * @author liyuan40
 * @date 2022/3/2 11:15
 */
public class RequestRateLimiter {
    /**
     * 默认每次尝试获取的许可数
     */
    public static final int DEFAULT_PERMITS = 1;

    RateLimiter rateLimiter;
    /**
     * 每秒颁发令牌速率
     */
    double tokenPerSecond;
    /**
     * 初始预放置令牌时间
     */
    double initBurstSeconds;

    public RequestRateLimiter(double a, double b) {
        this.tokenPerSecond = a;
        this.initBurstSeconds = b;
        this.rateLimiter = RateLimiterWithCapacity.createSmoothBurstyLimiter(tokenPerSecond, initBurstSeconds);
    }

    public RequestRateLimiter(PropertyUtils properties) {
        this.tokenPerSecond = properties.getDefaultTokenPerSecond();
        this.initBurstSeconds = properties.getDefaultInitBurstSeconds();
        this.rateLimiter = RateLimiterWithCapacity.createSmoothBurstyLimiter(tokenPerSecond, initBurstSeconds);
    }

    /**
     * 获取限速器速率
     *
     * @return double 速率
     */
    public double getRate() {
        return rateLimiter.getRate();
    }

    /**
     * 尝试获取令牌
     *
     * @return 是否获取成功
     */
    public boolean acquire() {
        // 未开启全局自动降级开关,不尝试获取令牌
        if (!PropertyUtils.getInstance().getAutoFallback()) {
            return true;
        }
        return acquire(DEFAULT_PERMITS);
    }

    /**
     * 尝试获取令牌
     *
     * @param permits 许可数
     * @return 是否获取成功
     */
    public boolean acquire(int permits) {
        return rateLimiter.tryAcquire(permits);
    }
}
