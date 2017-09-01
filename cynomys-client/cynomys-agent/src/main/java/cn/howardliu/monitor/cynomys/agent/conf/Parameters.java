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
package cn.howardliu.monitor.cynomys.agent.conf;

import cn.howardliu.gear.monitor.core.NetUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Classe d'accès aux paramètres du monitoring.
 *
 * @author Emeric Vernat
 */
public final class Parameters {
    public static final String DEFAULT_HOST_ADDRESS = "127.0.0.1";
    public static final String DEFAULT_HOST_NAME = "localhost";
    private static boolean dnsLookupsDisabled;

    private Parameters() {
    }

    public static void initialize() {
        if ("1.7".compareTo(SystemUtils.JAVA_VERSION) > 0) {
            throw new IllegalStateException("the minimal JDK version is 1.7, current is " + SystemUtils.JAVA_VERSION);
        }
        dnsLookupsDisabled = false;
    }

    public static String getHostName() {
        if (dnsLookupsDisabled) {
            return DEFAULT_HOST_NAME;
        }
        return NetUtils.getLocalHostname();
    }

    public static String getHostAddress() {
        if (dnsLookupsDisabled) {
            return DEFAULT_HOST_ADDRESS;
        }
        return NetUtils.getLocalHostAddress();
    }

    public static boolean isNoDatabase() {
        return false;
    }

    public static boolean isSystemActionsEnabled() {
        return false;
    }

    public static boolean isCounterHidden(String counterName) {
        // TODO check this attribute
        final String displayedCounters = null;
        //noinspection ConstantConditions
        if (displayedCounters == null) {
            return false;
        }
        for (final String displayedCounter : displayedCounters.split(",")) {
            final String displayedCounterName = displayedCounter.trim();
            if (counterName.equalsIgnoreCase(displayedCounterName)) {
                return false;
            }
        }
        return true;
    }

    public static String getContextPath(ServletContext context) {
        if (context.getMajorVersion() == 2 && context.getMinorVersion() >= 5 || context.getMajorVersion() > 2) {
            return context.getContextPath();
        }
        final URL webXmlUrl;
        try {
            webXmlUrl = context.getResource("/WEB-INF/web.xml");
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        String contextPath = webXmlUrl.toExternalForm();
        contextPath = contextPath.substring(0, contextPath.indexOf("/WEB-INF/web.xml"));
        final int indexOfWar = contextPath.indexOf(".war");
        if (indexOfWar > 0) {
            contextPath = contextPath.substring(0, indexOfWar);
        }
        if (contextPath.startsWith("jndi:/localhost")) {
            contextPath = contextPath.substring("jndi:/localhost".length());
        }
        final int lastIndexOfSlash = contextPath.lastIndexOf('/');
        if (lastIndexOfSlash != -1) {
            contextPath = contextPath.substring(lastIndexOfSlash);
        }
        return contextPath;
    }
}
