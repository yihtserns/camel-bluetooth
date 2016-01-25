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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ResponseCodes;
import javax.obex.ServerRequestHandler;
import javax.obex.SessionNotifier;
import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.IOHelper;

/**
 *
 * @author yihtserns
 */
public class ObexObjectPushProfileEndpoint extends DefaultEndpoint {

    static final UUID OBEX_OBJECT_PUSH_PROFILE = new UUID(0x1105);

    public ObexObjectPushProfileEndpoint(String endpointUri, Component component) {
        super(endpointUri, component);
    }

    public Producer createProducer() throws Exception {
        String deviceId = new URI(getEndpointUri()).getPath();
        final String url = "btgoep:/" + deviceId + ":1;authenticate=false;encrypt=false;master=false";

        return new DefaultProducerImpl(url);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        final ExecutorService executor = getCamelContext().getExecutorServiceStrategy().newSingleThreadExecutor(this, getEndpointUri());

        return new DefaultConsumerImpl(processor, executor);
    }

    public boolean isSingleton() {
        return true;
    }

    private class DefaultProducerImpl extends DefaultProducer {

        private final String url;

        public DefaultProducerImpl(String url) {
            super(ObexObjectPushProfileEndpoint.this);
            this.url = url;
        }

        public void process(Exchange exchange) throws Exception {
            ClientSession clientSession = (ClientSession) Connector.open(url);

            try {
                HeaderSet connectReply = clientSession.connect(null);
                if (connectReply.getResponseCode() != ResponseCodes.OBEX_HTTP_OK) {

                    // TODO: How to fail?
                    throw new IllegalStateException("Failed to connect");
                }
                File file = exchange.getIn().getMandatoryBody(File.class);

                HeaderSet operation = clientSession.createHeaderSet();
                operation.setHeader(HeaderSet.NAME, file.getName());
                operation.setHeader(HeaderSet.LENGTH, (long) file.length());
                operation.setHeader(HeaderSet.TYPE, URLConnection.guessContentTypeFromName(file.getName()));

                Operation putOperation = clientSession.put(operation);
                try {
                    OutputStream os = putOperation.openOutputStream();
                    try {
                        IOHelper.copyAndCloseInput(new FileInputStream(file), os);
                    } finally {
                        os.close();
                    }
                } finally {
                    putOperation.close();
                }
            } finally {
                try {
                    clientSession.disconnect(null);
                } finally {
                    clientSession.close();
                }
            }
        }
    }

    private class DefaultConsumerImpl extends DefaultConsumer implements Callable<Void> {

        private CountDownLatch startSignal = new CountDownLatch(1);
        private SessionNotifier sessionNotifier = null;
        private final ExecutorService executor;

        public DefaultConsumerImpl(Processor processor, ExecutorService executor) {
            super(ObexObjectPushProfileEndpoint.this, processor);
            this.executor = executor;
        }

        @Override
        protected void doStart() throws Exception {
            super.doStart();
            Future<Void> execution = executor.submit(this);
            startSignal.await();

            if (sessionNotifier == null) {
                // Didn't start properly, must've thrown exception
                execution.get();
            }
        }

        @Override
        protected void doStop() throws Exception {
            executor.shutdown();
            try {
                sessionNotifier.close();
            } catch (IOException ex) {
                // Can't close connection, hmm...
            }
            try {
                LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
            } catch (BluetoothStateException ex) {
                // Can't change the state, oh well
            }
            super.doStop();
        }

        @Override
        public Void call() throws BluetoothStateException, IOException {
            try {
                LocalDevice.getLocalDevice().setDiscoverable(DiscoveryAgent.GIAC);

                String url = "btgoep://localhost:" + OBEX_OBJECT_PUSH_PROFILE + ";authenticate=false;encrypt=false";
                sessionNotifier = (SessionNotifier) Connector.open(url);
            } finally {
                startSignal.countDown();
            }

            ServerRequestHandler sendBluetoothStreamToProcessor = new ServerRequestHandler() {

                @Override
                public int onPut(Operation op) {
                    try {
                        HeaderSet headerSet = op.getReceivedHeaders();
                        Exchange exchange = createExchange();

                        Message inMessage = exchange.getIn();
                        inMessage.setHeader(Exchange.FILE_NAME, (String) headerSet.getHeader(HeaderSet.NAME));
                        inMessage.setHeader("CamelFileLength", op.getLength());
                        inMessage.setHeader("CamelFileContentType", op.getType());
                        inMessage.setBody(op.openInputStream());

                        getProcessor().process(exchange);
                        return ResponseCodes.OBEX_HTTP_OK;
                    } catch (Exception ex) {
                        getExceptionHandler().handleException(ex);
                        return ResponseCodes.OBEX_HTTP_UNAVAILABLE;
                    }
                }
            };

            while (true) {
                try {
                    sessionNotifier.acceptAndOpen(sendBluetoothStreamToProcessor);
                } catch (IOException ex) {
                    // Session closed
                    break;
                }
            }

            return null;
        }
    }
}
