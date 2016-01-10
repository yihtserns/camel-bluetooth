/*
 * Copyright 2016 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.camel.bluetooth.testutil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.camel.spi.ExecutorServiceStrategy;
import org.apache.camel.spi.ThreadPoolProfile;

/**
 * @author yihtserns
 */
public class DelegatingExecutorServiceStrategy implements ExecutorServiceStrategy {

    private ExecutorServiceStrategy delegate;

    public DelegatingExecutorServiceStrategy(ExecutorServiceStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public void registerThreadPoolProfile(ThreadPoolProfile profile) {
        delegate.registerThreadPoolProfile(profile);
    }

    @Override
    public ThreadPoolProfile getThreadPoolProfile(String id) {
        return delegate.getThreadPoolProfile(id);
    }

    @Override
    public ThreadPoolProfile getDefaultThreadPoolProfile() {
        return delegate.getDefaultThreadPoolProfile();
    }

    @Override
    public void setDefaultThreadPoolProfile(ThreadPoolProfile defaultThreadPoolProfile) {
        delegate.setDefaultThreadPoolProfile(defaultThreadPoolProfile);
    }

    @Override
    public String getThreadName(String name) {
        return delegate.getThreadName(name);
    }

    @Override
    public String getThreadNamePattern() {
        return delegate.getThreadNamePattern();
    }

    @Override
    public void setThreadNamePattern(String pattern) throws IllegalArgumentException {
        delegate.setThreadNamePattern(pattern);
    }

    @Override
    public ExecutorService lookup(Object source, String name, String executorServiceRef) {
        return delegate.lookup(source, name, executorServiceRef);
    }

    @Override
    public ScheduledExecutorService lookupScheduled(Object source, String name, String executorServiceRef) {
        return delegate.lookupScheduled(source, name, executorServiceRef);
    }

    @Override
    public ExecutorService newDefaultThreadPool(Object source, String name) {
        return delegate.newDefaultThreadPool(source, name);
    }

    @Override
    public ExecutorService newThreadPool(Object source, String name, String threadPoolProfileId) {
        return delegate.newThreadPool(source, name, threadPoolProfileId);
    }

    @Override
    public ExecutorService newCachedThreadPool(Object source, String name) {
        return delegate.newCachedThreadPool(source, name);
    }

    @Override
    public ScheduledExecutorService newScheduledThreadPool(Object source, String name, int poolSize) {
        return delegate.newScheduledThreadPool(source, name, poolSize);
    }

    @Override
    public ScheduledExecutorService newScheduledThreadPool(Object source, String name) {
        return delegate.newScheduledThreadPool(source, name);
    }

    @Override
    public ExecutorService newFixedThreadPool(Object source, String name, int poolSize) {
        return delegate.newFixedThreadPool(source, name, poolSize);
    }

    @Override
    public ExecutorService newSingleThreadExecutor(Object source, String name) {
        return delegate.newSingleThreadExecutor(source, name);
    }

    @Override
    public ExecutorService newThreadPool(Object source, String name, int corePoolSize, int maxPoolSize) {
        return delegate.newThreadPool(source, name, corePoolSize, maxPoolSize);
    }

    @Override
    public ExecutorService newThreadPool(Object source, String name, int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit timeUnit, int maxQueueSize, RejectedExecutionHandler rejectedExecutionHandler, boolean daemon) {
        return delegate.newThreadPool(source, name, corePoolSize, maxPoolSize, keepAliveTime, timeUnit, maxQueueSize, rejectedExecutionHandler, daemon);
    }

    @Override
    public void shutdown(ExecutorService executorService) {
        delegate.shutdown(executorService);
    }

    @Override
    public List<Runnable> shutdownNow(ExecutorService executorService) {
        return delegate.shutdownNow(executorService);
    }

    @Override
    public void shutdown() throws Exception {
        delegate.shutdown();
    }

    @Override
    public void start() throws Exception {
        delegate.start();
    }

    @Override
    public void stop() throws Exception {
        delegate.stop();
    }
}
