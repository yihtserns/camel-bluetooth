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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author yihtserns
 */
public class DelegatingExecutorService implements ExecutorService {

    private ExecutorService delegate;

    public DelegatingExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit tu) throws InterruptedException {
        return delegate.awaitTermination(l, tu);
    }

    @Override
    public <T> Future<T> submit(Callable<T> clbl) {
        return delegate.submit(clbl);
    }

    @Override
    public <T> Future<T> submit(Runnable r, T t) {
        return delegate.submit(r, t);
    }

    @Override
    public Future<?> submit(Runnable r) {
        return delegate.submit(r);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> clctn) throws InterruptedException {
        return delegate.invokeAll(clctn);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> clctn, long l, TimeUnit tu) throws InterruptedException {
        return delegate.invokeAll(clctn, l, tu);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> clctn) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(clctn);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> clctn, long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(clctn, l, tu);
    }

    @Override
    public void execute(Runnable r) {
        delegate.execute(r);
    }
}
