package io.dongtai.iast.core.bytecode.enhance.plugin.limiter.checker.impl;

import io.dongtai.iast.common.entity.performance.PerformanceMetrics;
import io.dongtai.iast.common.entity.performance.metrics.CpuInfoMetrics;
import io.dongtai.iast.common.entity.performance.metrics.MemoryUsageMetrics;

import java.util.Properties;


/**
 * cpu使用率检查器
 *
 * @author chenyi
 * @date 2022/3/4
 */
public class CpuUsageChecker extends BasePerformanceChecker {

    @Override
    public boolean isPerformanceRisk(PerformanceMetrics metrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchRiskThreshold(metrics.getMetricsKey(), cfg);
        if (thresholdMetrics != null) {
            final CpuInfoMetrics threshold = thresholdMetrics.getMetricsValue(CpuInfoMetrics.class);
            final CpuInfoMetrics now = metrics.getMetricsValue(CpuInfoMetrics.class);
            // cpu使用率
            if (threshold.getCpuUsagePercentage() != null) {
                return now.getCpuUsagePercentage() >= threshold.getCpuUsagePercentage();
            }
        }
        return false;
    }

    @Override
    public boolean isPerformanceOverLimit(PerformanceMetrics metrics, Properties cfg) {
        final PerformanceMetrics thresholdMetrics = getMatchMaxThreshold(metrics.getMetricsKey(), cfg);
        if (thresholdMetrics != null) {
            final CpuInfoMetrics threshold = thresholdMetrics.getMetricsValue(CpuInfoMetrics.class);
            final CpuInfoMetrics now = metrics.getMetricsValue(CpuInfoMetrics.class);
            // cpu使用率
            if (threshold.getCpuUsagePercentage() != null) {
                return now.getCpuUsagePercentage() >= threshold.getCpuUsagePercentage();
            }
        }
        return false;
    }
}
