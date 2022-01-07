package cc.ricecx.ricestats.utils;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * @author Lucko
 * credits = https://github.com/lucko/spark
 */
public class RollingAverage {

    private final Queue<BigDecimal> samples;
    private final int size;
    private BigDecimal total = BigDecimal.ZERO;

    public RollingAverage(int size) {
        this.size = size;
        this.samples = new ArrayDeque<>(this.size);
    }

    public void add(BigDecimal num) {
        synchronized (this) {
            this.total = this.total.add(num);
            this.samples.add(num);
            if (this.samples.size() > this.size) {
                this.total = this.total.subtract(this.samples.remove());
            }
        }
    }

    public double getAverage() {
        synchronized (this) {
            if (this.samples.isEmpty()) {
                return 0;
            }
            return this.total.divide(BigDecimal.valueOf(this.samples.size()), 30, RoundingMode.HALF_UP).doubleValue();
        }
    }

    public double getMax() {
        synchronized (this) {
            BigDecimal max = BigDecimal.ZERO;
            for (BigDecimal sample : this.samples) {
                if (sample.compareTo(max) > 0) {
                    max = sample;
                }
            }
            return max.doubleValue();
        }
    }

    public double getMin() {
        synchronized (this) {
            BigDecimal min = BigDecimal.ZERO;
            for (BigDecimal sample : this.samples) {
                if (min == BigDecimal.ZERO || sample.compareTo(min) < 0) {
                    min = sample;
                }
            }
            return min.doubleValue();
        }
    }

    public double getMedian() {
        return getPercentile(50);
    }

    public double getPercentile(int percentile) {
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Invalid percentage " + percentile);
        }

        List<BigDecimal> sortedSamples;
        synchronized (this) {
            if (this.samples.isEmpty()) {
                return 0;
            }
            sortedSamples = new ArrayList<>(this.samples);
        }
        sortedSamples.sort(null);

        int rank = (int) Math.ceil((percentile / 100d) * (sortedSamples.size() - 1));
        return sortedSamples.get(rank).doubleValue();
    }

}
