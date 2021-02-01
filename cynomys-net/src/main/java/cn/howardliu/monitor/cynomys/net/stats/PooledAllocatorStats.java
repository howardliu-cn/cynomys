package cn.howardliu.monitor.cynomys.net.stats;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <br>created at 17-10-10
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum PooledAllocatorStats {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(PooledAllocatorStats.class);

    private static Map<String, Object> metricsOfPoolArena(final PoolArenaMetric poolArenaMetric) {
        final Map<String, Object> metrics = new HashMap<>();
        metrics.put("1_numThreadCaches", poolArenaMetric.numThreadCaches());
        metrics.put("2_0_numAllocations", poolArenaMetric.numAllocations());
        metrics.put("2_1_numTinyAllocations", poolArenaMetric.numTinyAllocations());
        metrics.put("2_2_numSmallAllocations", poolArenaMetric.numSmallAllocations());
        metrics.put("2_3_numNormalAllocations", poolArenaMetric.numNormalAllocations());
        metrics.put("2_4_numHugeAllocations", poolArenaMetric.numHugeAllocations());
        metrics.put("3_0_numDeallocations", poolArenaMetric.numDeallocations());
        metrics.put("3_1_numTinyDeallocations", poolArenaMetric.numTinyDeallocations());
        metrics.put("3_2_numSmallDeallocations", poolArenaMetric.numSmallDeallocations());
        metrics.put("3_3_numNormalDeallocations", poolArenaMetric.numNormalDeallocations());
        metrics.put("3_4_numHugeDeallocations", poolArenaMetric.numHugeDeallocations());
        metrics.put("4_0_numActiveAllocations", poolArenaMetric.numActiveAllocations());
        metrics.put("4_1_numActiveTinyAllocations", poolArenaMetric.numActiveTinyAllocations());
        metrics.put("4_2_numActiveSmallAllocations", poolArenaMetric.numActiveSmallAllocations());
        metrics.put("4_3_numActiveNormalAllocations", poolArenaMetric.numActiveNormalAllocations());
        metrics.put("4_4_numActiveHugeAllocations", poolArenaMetric.numActiveHugeAllocations());
        metrics.put("5_numActiveBytes", poolArenaMetric.numActiveBytes());
        metrics.put("6_0_numChunkLists", poolArenaMetric.numChunkLists());

        /**
         * Returns an unmodifiable {@link List} which holds {@link PoolChunkListMetric}s.
         */
        metrics.put("6_1_chunkLists", metricsOfChunkLists(poolArenaMetric.chunkLists()));
        metrics.put("7_0_numTinySubpages", poolArenaMetric.numTinySubpages());

        /**
         * Returns an unmodifiable {@link List} which holds {@link PoolSubpageMetric}s for tiny sub-pages.
         */
        metrics.put(
                "7_1_tinySubpages",
                metricsOfSubpages(
                        poolArenaMetric.numTinySubpages(),
                        poolArenaMetric.tinySubpages()
                )
        );
        metrics.put("8_0_numSmallSubpages", poolArenaMetric.numSmallSubpages());

        /**
         * Returns an unmodifiable {@link List} which holds {@link PoolSubpageMetric}s for small sub-pages.
         */
        metrics.put(
                "8_1_smallSubpage",
                metricsOfSubpages(
                        poolArenaMetric.numSmallSubpages(),
                        poolArenaMetric.smallSubpages()
                )
        );

        return metrics;
    }

    private static Map<String, Object> metricsOfChunkLists(final List<PoolChunkListMetric> chunkLists) {
        final Map<String, Object> metrics = new HashMap<>();
        int idx = 0;
        for (PoolChunkListMetric chunkListMetric : chunkLists) {
            metrics.put(idx++ + "_chunkList", metricsOfPoolChunkList(chunkListMetric));
        }
        return metrics;
    }

    private static Map<String, Object> metricsOfPoolChunkList(final PoolChunkListMetric chunkListMetric) {
        final Map<String, Object> metrics = new HashMap<>();

        metrics.put("1_minUsage", chunkListMetric.minUsage());
        metrics.put("2_maxUsage", chunkListMetric.maxUsage());

        final Iterator<PoolChunkMetric> iter = chunkListMetric.iterator();
        int idx = 0;
        while (iter.hasNext()) {
            metrics.put("chunk_" + idx++, metricsOfPoolChunk(iter.next()));
        }
        metrics.put("3_numChunks", idx);
        return metrics;
    }

    private static Map<String, Object> metricsOfPoolChunk(final PoolChunkMetric chunkMetric) {
        final Map<String, Object> metrics = new HashMap<>();
        metrics.put("1_usage", chunkMetric.usage());
        metrics.put("2_freeBytes", chunkMetric.freeBytes());
        metrics.put("3_chunkSize", chunkMetric.chunkSize());
        return metrics;
    }

    private static Map<String, Object> metricsOfSubpages(final int count, final List<PoolSubpageMetric> subpages) {
        final Map<String, Object> metrics = new HashMap<>();

        final int w = (int) (Math.log10(count)) + 1;
        final String fstr = "%0" + w + "d";

        int idx = 0;
        for (PoolSubpageMetric subpageMetric : subpages) {
            metrics.put(String.format(fstr, idx++) + "_tinySubpage", metricsOfPoolSubpage(subpageMetric));
        }
        return metrics;
    }

    private static Map<String, Object> metricsOfPoolSubpage(final PoolSubpageMetric subpageMetric) {
        final Map<String, Object> metrics = new HashMap<>();
        metrics.put("1_elementSize", subpageMetric.elementSize());
        metrics.put("2_maxNumElements", subpageMetric.maxNumElements());
        metrics.put("3_numAvailable", subpageMetric.numAvailable());
        metrics.put("4_pageSize", subpageMetric.pageSize());
        return metrics;
    }

    public Map<String, Object> getMetrics() {
        final PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;
        final Map<String, Object> metrics = new HashMap<>();
        {
            int idx = 0;
            for (PoolArenaMetric poolArenaMetric : allocator.metric().directArenas()) {
                metrics.put("1_DirectArena[" + idx++ + "]", metricsOfPoolArena(poolArenaMetric));
            }
        }
        {
            int idx = 0;
            for (PoolArenaMetric poolArenaMetric : allocator.metric().heapArenas()) {
                metrics.put("2_HeapArena[" + idx++ + "]", metricsOfPoolArena(poolArenaMetric));
            }
        }

        return metrics;
    }

    public String getMetricAsString() {
        return PooledByteBufAllocator.DEFAULT.dumpStats();
    }

    public static void print() {
        logger.trace("PooledAllocator Stats info: {}", JSON.toJSONString(INSTANCE.getMetrics()));
    }
}
