/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.howardliu.monitor.cynomys.agent.handler.wrapper;


import cn.howardliu.monitor.cynomys.agent.conf.Parameter;
import cn.howardliu.monitor.cynomys.agent.conf.Parameters;
import cn.howardliu.monitor.cynomys.agent.dto.ConnectionInformations;
import cn.howardliu.monitor.cynomys.agent.dto.Counter;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 这个类是构建代理数据源或*连接JDBC有用。*和结果，
 * 包括它在目录rebinde在JNDI的DataSource的JDBC*代监控代理。
 *
 * @author Emeric Vernat
 */
public final class JdbcWrapper {
    /**
     * JDBC包装的单一实例（在这里我们不知道的ServletContext*）。
     */
    public static final JdbcWrapper SINGLETON = new JdbcWrapper(new Counter(Counter.SQL_COUNTER_NAME, "db.png"));

    // 而是采用INT遍及同步，用于
    //的AtomicInteger
    public static final AtomicInteger ACTIVE_CONNECTION_COUNT = new AtomicInteger();
    public static final AtomicInteger USED_CONNECTION_COUNT = new AtomicInteger();
    public static final AtomicLong TRANSACTION_COUNT = new AtomicLong();
    public static final AtomicInteger ACTIVE_THREAD_COUNT = new AtomicInteger();
    public static final AtomicInteger RUNNING_BUILD_COUNT = new AtomicInteger();
    public static final AtomicInteger BUILD_QUEUE_LENGTH = new AtomicInteger();
    public static final Map<Integer, ConnectionInformations> USED_CONNECTION_INFORMATIONS = new ConcurrentHashMap<>();

    public static final int MAX_USED_CONNECTION_INFORMATIONS = 65535;

    //这个变量sqlCounter认为是全局性的过滤和状态
    //应用程序（因此线程安全）。
    private final Counter sqlCounter;
    private ServletContext servletContext;
    private boolean connectionInformationsEnabled;
    private boolean jboss;
    private boolean glassfish;
    private boolean weblogic;
    private boolean jonas;

    public static final class ConnectionInformationsComparator implements Comparator<ConnectionInformations>,
                                                                          Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(ConnectionInformations connection1, ConnectionInformations connection2) {
            return connection1.getOpeningDate().compareTo(connection2.getOpeningDate());
        }
    }

    // ce handler désencapsule les InvocationTargetException des proxy
    private static class DelegatingInvocationHandler implements InvocationHandler, Serializable {
        // classe sérialisable pour MonitoringProxy
        private static final long serialVersionUID = 7515240588169084785L;
        @SuppressWarnings("all")
        private final InvocationHandler delegate;

        DelegatingInvocationHandler(InvocationHandler delegate) {
            super();
            this.delegate = delegate;
        }

        InvocationHandler getDelegate() {
            return delegate;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return delegate.invoke(proxy, method, args);
            } catch (final InvocationTargetException e) {
                if (e.getTargetException() != null) {
                    throw e.getTargetException();
                }
                throw e;
            }
        }
    }

    private JdbcWrapper(Counter sqlCounter) {
        super();
        assert sqlCounter != null;
        this.sqlCounter = sqlCounter;
        // servletContext reste null pour l'instant
        this.servletContext = null;
        connectionInformationsEnabled = Parameters.isSystemActionsEnabled() && !Parameters.isNoDatabase();
    }

    public void initServletContext(ServletContext context) {
        assert context != null;
        this.servletContext = context;
        final String serverInfo = servletContext.getServerInfo();
        jboss = serverInfo.contains("JBoss") || serverInfo.contains("WildFly");
        glassfish = serverInfo.contains("GlassFish") || serverInfo.contains("Sun Java System Application Server");
        weblogic = serverInfo.contains("WebLogic");
        jonas = System.getProperty("jonas.name") != null;
        connectionInformationsEnabled = Parameters.isSystemActionsEnabled() && !Parameters.isNoDatabase();
    }

    public static int getUsedConnectionCount() {
        return USED_CONNECTION_COUNT.get();
    }

    public static int getActiveConnectionCount() {
        return ACTIVE_CONNECTION_COUNT.get();
    }

    public static long getTransactionCount() {
        return TRANSACTION_COUNT.get();
    }

    public static int getActiveThreadCount() {
        return ACTIVE_THREAD_COUNT.get();
    }

    public static int getRunningBuildCount() {
        return RUNNING_BUILD_COUNT.get();
    }

    public static int getBuildQueueLength() {
        return BUILD_QUEUE_LENGTH.get();
    }

    public static List<ConnectionInformations> getConnectionInformationsList() {
        final List<ConnectionInformations> result = new ArrayList<>(
                USED_CONNECTION_INFORMATIONS.values());
        Collections.sort(result, new ConnectionInformationsComparator());
        return Collections.unmodifiableList(result);
    }

    public Counter getSqlCounter() {
        return sqlCounter;
    }

    public boolean isConnectionInformationsEnabled() {
        return connectionInformationsEnabled;
    }

    public static int getMaxConnectionCount() {
        return JdbcWrapperHelper.getMaxConnectionCount();
    }

    public static Map<String, Map<String, Object>> getBasicDataSourceProperties() {
        return JdbcWrapperHelper.getBasicDataSourceProperties();
    }

    public static Map<String, DataSource> getJndiAndSpringDataSources() throws NamingException {
        return JdbcWrapperHelper.getJndiAndSpringDataSources();
    }

    public Object doExecute(String requestName, Statement statement, Method method, Object[] args) throws
            IllegalAccessException, InvocationTargetException {
        assert requestName != null;
        assert statement != null;
        assert method != null;

        // on ignore les requêtes explain exécutées par DatabaseInformations
        if (!sqlCounter.isDisplayed() || requestName.startsWith("explain ")) {
            ACTIVE_CONNECTION_COUNT.incrementAndGet();
            try {
                return method.invoke(statement, args);
            } finally {
                ACTIVE_CONNECTION_COUNT.decrementAndGet();
            }
        }

        final long start = System.currentTimeMillis();
        boolean systemError = true;
        try {
            ACTIVE_CONNECTION_COUNT.incrementAndGet();
            sqlCounter.bindContext(requestName, requestName, null, -1);

            final Object result = method.invoke(statement, args);
            systemError = false;
            return result;
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof SQLException) {
                final int errorCode = ((SQLException) e.getCause()).getErrorCode();
                if (errorCode >= 20000 && errorCode < 30000) {
                    systemError = false;
                }
            }
            throw e;
        } finally {
            ACTIVE_CONNECTION_COUNT.decrementAndGet();
            final long duration = Math.max(System.currentTimeMillis() - start, 0);
            sqlCounter.addRequest(requestName, duration, -1, systemError, -1);
        }
    }

    private boolean isServerNeedsRewrap(String jndiName) {
        return glassfish || jboss || weblogic || jndiName.contains("openejb");
    }

    private boolean isDbcpDataSource(String dataSourceClassName) {
        return "org.apache.tomcat.dbcp.dbcp.BasicDataSource"
                .equals(dataSourceClassName) || "org.apache.tomcat.dbcp.dbcp2.BasicDataSource"
                .equals(dataSourceClassName)
                || "org.apache.commons.dbcp.BasicDataSource"
                .equals(dataSourceClassName) || "org.apache.commons.dbcp2.BasicDataSource".equals(dataSourceClassName)
                || "org.apache.openejb.resource.jdbc.BasicManagedDataSource"
                .equals(dataSourceClassName) || "org.apache.openejb.resource.jdbc.BasicDataSource"
                .equals(dataSourceClassName);
    }

    private boolean isJBossOrGlassfishDataSource(String dataSourceClassName) {
        return jboss && "org.jboss.resource.adapter.jdbc.WrapperDataSource"
                .equals(dataSourceClassName) || jboss && "org.jboss.jca.adapters.jdbc.WrapperDataSource"
                .equals(dataSourceClassName)
                || glassfish && "com.sun.gjc.spi.jdbc40.DataSource40".equals(dataSourceClassName);
    }

    private boolean isWildfly9DataSource(String dataSourceClassName) {
        return jboss && "org.jboss.as.connector.subsystems.datasources.WildFlyDataSource".equals(dataSourceClassName);
    }

    public void fillDataSourceInfo(DataSource dataSource) {
        assert dataSource != null;
        JdbcWrapperHelper
                .pullDataSourceProperties(dataSource.getClass().getName() + "@" + System.identityHashCode(dataSource),
                        dataSource);
    }

    boolean isSqlMonitoringDisabled() {
        return isMonitoringDisabled() || !sqlCounter.isDisplayed();
    }

    private static boolean isMonitoringDisabled() {
        // on doit réévaluer ici le paramètre, car au départ le servletContext
        // n'est pas forcément défini si c'est un driver jdbc sans dataSource
        return Boolean.parseBoolean(Parameters.getParameter(Parameter.DISABLED));
    }

    static boolean isEqualsMethod(Object methodName, Object[] args) {
        // == for perf (strings interned: == is ok)
        return "equals" == methodName && args != null && args.length == 1; // NOPMD
    }

    static boolean isHashCodeMethod(Object methodName, Object[] args) {
        // == for perf (strings interned: == is ok)
        return "hashCode" == methodName && (args == null || args.length == 0); // NOPMD
    }

    public static <T> T createProxy(T object, InvocationHandler invocationHandler) {
        return createProxy(object, invocationHandler, null);
    }

    static <T> T createProxy(T object, InvocationHandler invocationHandler, List<Class<?>> interfaces) {
        if (isProxyAlready(object)) {
            return object;
        }
        final InvocationHandler ih = new DelegatingInvocationHandler(invocationHandler);
        return JdbcWrapperHelper.createProxy(object, ih, interfaces);
    }

    private static boolean isProxyAlready(Object object) {
        return Proxy.isProxyClass(object.getClass()) && Proxy.getInvocationHandler(object).getClass().getName()
                .equals(DelegatingInvocationHandler.class.getName());
    }

}
