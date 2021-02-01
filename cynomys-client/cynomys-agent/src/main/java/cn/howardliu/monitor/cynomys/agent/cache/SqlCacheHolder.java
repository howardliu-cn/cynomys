package cn.howardliu.monitor.cynomys.agent.cache;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.PooledExecutionServiceConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <br>created at 2019-07-08
 *
 * @author liuxh
 * @since 1.0.0
 */
public enum SqlCacheHolder implements SqlHolder {
    SQL_HOLDER;

    private static final String CACHE_NAME = "cynomys-ehcache";
    private static Cache<Integer, String> cache;

    static {
        final ResourcePools resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
                .disk(50, MemoryUnit.MB)
                .heap(2000, EntryUnit.ENTRIES)
                .build();
        final CacheConfiguration<Integer, String> cacheConfiguration = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Integer.class, String.class, resourcePools)
                .withSizeOfMaxObjectSize(4, MemoryUnit.MB)
                .withExpiry(Expirations.timeToIdleExpiration(new Duration(2, TimeUnit.MINUTES)))
                .build();
        final CacheManager cacheManager = CacheManagerBuilder
                .newCacheManagerBuilder()
                .using(
                        PooledExecutionServiceConfigurationBuilder
                                .newPooledExecutionServiceConfigurationBuilder()
                                .defaultPool("dflt", 0, 20)
                                .pool("defaultDiskPool", 1, 6)
                                .pool("cache2Pool", 2, 2)
                                .build()
                )
                .with(new CacheManagerPersistenceConfiguration(new File(getStoragePath(), UUID.randomUUID().toString())))
                .withDefaultDiskStoreThreadPool("defaultDiskPool")
                .withCache(CACHE_NAME, cacheConfiguration)
                .build(true);
        cache = cacheManager.getCache(CACHE_NAME, Integer.class, String.class);
    }

    @Override
    public boolean contains(final int hashCode) {
        return StringUtils.isNotBlank(cache.get(hashCode));
    }

    @Override
    public String add(final int hashCode, final String sql) {
        cache.put(hashCode, sql);
        return null;
    }

    @Override
    public String get(final int hashCode) {
        return cache.get(hashCode);
    }

    @Override
    public String remove(final int hashCode) {
        cache.remove(hashCode);
        return null;
    }

    private static String getStoragePath() {
        return System.getProperty("user.home") + File.separator + ".cynomys";
    }
}
