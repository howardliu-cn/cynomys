package cn.howardliu.monitor.cynomys.agent.cache;

/**
 * <br>created at 2019-05-29
 *
 * @author liuxh
 * @since 1.0.0
 */
public interface SqlHolder {
    boolean contains(int hashCode);

    String add(int hashCode, String sql);

    String get(int hashCode);

    String remove(int hashCode);
}
