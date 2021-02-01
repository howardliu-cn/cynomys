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

import cn.howardliu.gear.monitor.core.jvm.PID;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Informations sur un thread java, sans code html de présentation. L'état d'une
 * instance est initialisé à son instanciation et non mutable; il est donc de
 * fait thread-safe. Cet état est celui d'un thread java à un instant t. Les
 * instances sont sérialisables pour pouvoir être transmises au serveur de
 * collecte.
 *
 * @author Emeric Vernat
 */
public class ThreadInformations implements Serializable {
    private static final long serialVersionUID = 3604281253550723654L;
    @SuppressWarnings("all")
    private final String name;
    private final long id;
    private final int priority;
    private final boolean daemon;
    private final Thread.State state;
    private final long cpuTimeMillis;
    private final long userTimeMillis;
    private final boolean deadlocked;
    private final String globalThreadId;
    @SuppressWarnings("all")
    private final List<StackTraceElement> stackTrace;

    @SuppressWarnings("all")
    public ThreadInformations(Thread thread, List<StackTraceElement> stackTrace, long cpuTimeMillis,
                              long userTimeMillis, boolean deadlocked, String hostAddress) {
        super();
        assert thread != null;
        assert stackTrace == null || stackTrace instanceof Serializable;

        this.name = thread.getName();
        this.id = thread.getId();
        this.priority = thread.getPriority();
        this.daemon = thread.isDaemon();
        this.state = thread.getState();
        this.stackTrace = stackTrace;
        this.cpuTimeMillis = cpuTimeMillis;
        this.userTimeMillis = userTimeMillis;
        this.deadlocked = deadlocked;
        this.globalThreadId = buildGlobalThreadId(thread, hostAddress);
    }

    private static String buildGlobalThreadId(Thread thread, String hostAddress) {
        return PID.getPID() + '_' + hostAddress + '_' + thread.getId();
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public Thread.State getState() {
        return state;
    }

    public List<StackTraceElement> getStackTrace() {
        if (stackTrace != null) {
            return Collections.unmodifiableList(stackTrace);
        }
        return Collections.emptyList();
    }

    public String getExecutedMethod() {
        final List<StackTraceElement> trace = stackTrace;
        if (trace != null && !trace.isEmpty()) {
            return trace.get(0).toString();
        }
        return "";
    }

    public long getCpuTimeMillis() {
        return cpuTimeMillis;
    }

    public long getUserTimeMillis() {
        return userTimeMillis;
    }

    public boolean isDeadlocked() {
        return deadlocked;
    }

    public String getGlobalThreadId() {
        return globalThreadId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass()
                .getSimpleName() + "[id=" + getId() + ", name=" + getName() + ", daemon=" + isDaemon() + ", priority=" + getPriority() + ", deadlocked=" + isDeadlocked() + ", state="
                + getState() + ']';
    }
}
