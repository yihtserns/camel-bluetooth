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
package com.github.yihtserns.camel.bluetooth;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import javax.obex.SessionNotifier;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 *
 * @author yihtserns
 */
public class ObexObjectPushProfileEndpoint extends DefaultEndpoint {

    private static final UUID OBEX_OBJECT_PUSH_PROFILE = new UUID(0x1105);

    public ObexObjectPushProfileEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        final ExecutorService executor = getCamelContext().getExecutorServiceStrategy().newSingleThreadExecutor(this, getEndpointUri());

        return new DefaultConsumer(this, processor) {

            @Override
            protected void doStart() throws Exception {
                super.doStart();

                LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);
                String url = "btgoep://localhost:" + OBEX_OBJECT_PUSH_PROFILE + ";authenticate=false;encrypt=false";

                final SessionNotifier sessionNotifier = (SessionNotifier) Connector.open(url);

                executor.submit(new Runnable() {

                    public void run() {
                        while (true) {
                            try {
                                sessionNotifier.acceptAndOpen(new ServerRequestHandler() {

                                    @Override
                                    public int onPut(Operation op) {
                                        try {
                                            HeaderSet headerSet = op.getReceivedHeaders();
                                            String name = (String) headerSet.getHeader(HeaderSet.NAME);

                                            Exchange exchange = createExchange();
                                            exchange.getIn().setBody(op);
                                            exchange.getIn().setHeader("Name", name);

                                            getProcessor().process(exchange);
                                            return ResponseCodes.OBEX_HTTP_OK;
                                        } catch (Exception ex) {
                                            getExceptionHandler().handleException(ex);
                                            return ResponseCodes.OBEX_HTTP_UNAVAILABLE;
                                        } finally {
                                            try {
                                                op.close();
                                            } catch (IOException ex) {
                                                getExceptionHandler().handleException(ex);
                                            }
                                        }
                                    }
                                });
                            } catch (IOException ex) {
                                getExceptionHandler().handleException(ex);
                            }
                        }
                    }
                });
            }

            @Override
            protected void doStop() throws Exception {
                executor.shutdown();

                super.doStop();
            }

        };
    }

    public boolean isSingleton() {
        return true;
    }
}
