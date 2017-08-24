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
package cn.howardliu.monitor.cynomys.agent.dto;

import cn.howardliu.monitor.cynomys.agent.util.MBeans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.Serializable;
import java.util.*;

/**
 * 检查Tomcat 信息
 *
 * @author Emeric Vernat
 */
public final class TomcatInformations implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(TomcatInformations.class);

    private static final boolean TOMCAT_USED = System.getProperty("catalina.home") != null;
    private static final long serialVersionUID = -6145865427461051370L;
    @SuppressWarnings("all")
    private static final List<ObjectName> THREAD_POOLS = new ArrayList<ObjectName>();
    @SuppressWarnings("all")
    private static final List<ObjectName> GLOBAL_REQUEST_PROCESSORS = new ArrayList<ObjectName>();
    private static int mbeansInitAttemps;

    private final String name;
    private final String httpPort;
    private final int maxThreads;
    private final int currentThreadCount;
    private final int currentThreadsBusy;
    private final long bytesReceived;
    private final long bytesSent;
    private final int requestCount;
    private final int errorCount;
    private final long processingTime;
    private final long maxTime;

    private TomcatInformations(MBeans mBeans, ObjectName threadPool) throws JMException {
        super();
        name = threadPool.getKeyProperty("name");
        httpPort = getHttpPort(mBeans, "8080");
        maxThreads = (Integer) mBeans.getAttribute(threadPool, "maxThreads");
        currentThreadCount = (Integer) mBeans.getAttribute(threadPool, "currentThreadCount");
        currentThreadsBusy = (Integer) mBeans.getAttribute(threadPool, "currentThreadsBusy");
        ObjectName grp = null;
        for (final ObjectName globalRequestProcessor : GLOBAL_REQUEST_PROCESSORS) {
            if (name.equals(globalRequestProcessor.getKeyProperty("name"))) {
                grp = globalRequestProcessor;
                break;
            }
        }
        if (grp != null) {
            bytesReceived = (Long) mBeans.getAttribute(grp, "bytesReceived");
            bytesSent = (Long) mBeans.getAttribute(grp, "bytesSent");
            requestCount = (Integer) mBeans.getAttribute(grp, "requestCount");
            errorCount = (Integer) mBeans.getAttribute(grp, "errorCount");
            processingTime = (Long) mBeans.getAttribute(grp, "processingTime");
            maxTime = (Long) mBeans.getAttribute(grp, "maxTime");
        } else {
            bytesReceived = 0;
            bytesSent = 0;
            requestCount = 0;
            errorCount = 0;
            processingTime = 0;
            maxTime = 0;
        }
    }

    private static String getHttpPort(MBeans server, String defaultPort) {
        String portString = defaultPort;
        try {

            Set<ObjectName> names = server.getAttribute(new ObjectName("*:type=Connector,*"), (QueryExp) null);
            Iterator<ObjectName> iterator = names.iterator();

            ObjectName name;
            while (iterator.hasNext()) {
                name = iterator.next();

                String protocol = server.getAttribute(name, "protocol").toString();
                String scheme = server.getAttribute(name, "scheme").toString();

                if (protocol.toLowerCase().contains("http") && scheme.toLowerCase().contains("http")) {
                    portString = server.getAttribute(name, "port").toString();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("got port use MXBean exception", e);
        }
        return portString;
    }

    public static List<TomcatInformations> buildTomcatInformationsList() {
        if (!TOMCAT_USED) {
            return Collections.emptyList();
        }
        try {
            synchronized (THREAD_POOLS) {
                if ((THREAD_POOLS.isEmpty() || GLOBAL_REQUEST_PROCESSORS.isEmpty()) && mbeansInitAttemps < 10) {
                    initMBeans();
                    mbeansInitAttemps++;
                }
            }
            final List<TomcatInformations> tomcatInformationsList = new ArrayList<>(THREAD_POOLS.size());
            for (final ObjectName threadPool : THREAD_POOLS) {
                tomcatInformationsList.add(new TomcatInformations(new MBeans(), threadPool));
            }
            return tomcatInformationsList;
        } catch (final InstanceNotFoundException | AttributeNotFoundException e) {
            return Collections.emptyList();
        } catch (final JMException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void initMBeans() throws MalformedObjectNameException {
        final MBeans mBeans = new MBeans();
        THREAD_POOLS.clear();
        GLOBAL_REQUEST_PROCESSORS.clear();
        THREAD_POOLS.addAll(mBeans.getTomcatThreadPools());
        GLOBAL_REQUEST_PROCESSORS.addAll(mBeans.getTomcatGlobalRequestProcessors());
    }

    public String getName() {
        return name;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getCurrentThreadCount() {
        return currentThreadCount;
    }

    public int getCurrentThreadsBusy() {
        return currentThreadsBusy;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public String getHttpPort() {
        return httpPort;
    }

    @Override
    public String toString() {
        return getClass()
                .getSimpleName() + "[name=" + getName() + ", port=" + getHttpPort() + ", maxThreads=" + getMaxThreads()
                + ", currentThreadCount=" + getCurrentThreadCount() + ", currentThreadsBusy="
                + getCurrentThreadsBusy() + ", bytesReceived=" + getBytesReceived() + ", bytesSent="
                + getBytesSent() + ", requestCount=" + getRequestCount() + ", errorCount="
                + getErrorCount() + ", processingTime=" + getProcessingTime() + ", maxTime="
                + getMaxTime() + ']';
    }
}
