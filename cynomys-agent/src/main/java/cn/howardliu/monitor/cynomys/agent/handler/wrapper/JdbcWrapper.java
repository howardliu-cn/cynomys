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
import java.util.List;
import java.util.Map;
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

    public static final AtomicInteger ACTIVE_CONNECTION_COUNT = new AtomicInteger();
    public static final AtomicInteger USED_CONNECTION_COUNT = new AtomicInteger();
    public static final AtomicLong TRANSACTION_COUNT = new AtomicLong();
    public static final AtomicInteger ACTIVE_THREAD_COUNT = new AtomicInteger();
    public static final AtomicInteger RUNNING_BUILD_COUNT = new AtomicInteger();
    public static final AtomicInteger BUILD_QUEUE_LENGTH = new AtomicInteger();
    public static final Map<Integer, ConnectionInformations> USED_CONNECTION_INFORMATIONS = new ConcurrentHashMap<>();
    public static final int MAX_USED_CONNECTION_INFORMATIONS = 65535;

    private final Counter sqlCounter;
    private boolean connectionInformationsEnabled;

    private JdbcWrapper(Counter sqlCounter) {
        this.sqlCounter = sqlCounter;
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

    public static int getMaxConnectionCount() {
        return JdbcWrapperHelper.getMaxConnectionCount();
    }

    public static Map<String, Map<String, Object>> getBasicDataSourceProperties() {
        return JdbcWrapperHelper.getBasicDataSourceProperties();
    }

    public static Map<String, DataSource> getJndiAndSpringDataSources() throws NamingException {
        return JdbcWrapperHelper.getJndiAndSpringDataSources();
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

    public Counter getSqlCounter() {
        return sqlCounter;
    }

    public boolean isConnectionInformationsEnabled() {
        return connectionInformationsEnabled;
    }

    private static class DelegatingInvocationHandler implements InvocationHandler, Serializable {
        private static final long serialVersionUID = 7515240588169084785L;
        private final InvocationHandler delegate;

        DelegatingInvocationHandler(InvocationHandler delegate) {
            super();
            this.delegate = delegate;
        }

        InvocationHandler getDelegate() {
            return delegate;
        }

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

}
