package cn.howardliu.monitor.cynomys.agent.cache;

import org.junit.Test;

import static cn.howardliu.monitor.cynomys.agent.cache.SqlCacheHolder.SQL_HOLDER;

/**
 * <br>created at 2019-07-08
 *
 * @author liuxh
 * @since 1.0.0
 */
public class SqlCacheHolderTest {

    @Test
    public void contains() {
        SQL_HOLDER.add(100, "Hello, World!");
        System.out.println(SQL_HOLDER.contains(100));
    }

    @Test
    public void add() {
        SQL_HOLDER.add(100, "Hello, World!");
        System.out.println(SQL_HOLDER.get(100));
    }

    @Test
    public void get() {
        System.out.println(SQL_HOLDER.get(100));
    }

    @Test
    public void remove() {
        SQL_HOLDER.add(100, "Hello, World!");
        System.out.println(SQL_HOLDER.get(100));
        SQL_HOLDER.remove(100);
        System.out.println(SQL_HOLDER.get(100));
    }
}
