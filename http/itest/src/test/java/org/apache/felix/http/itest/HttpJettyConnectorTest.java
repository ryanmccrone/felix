/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.http.itest;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.felix.http.jetty.ConnectorFactory;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.ServiceRegistration;

/**
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
@RunWith(JUnit4TestRunner.class)
public class HttpJettyConnectorTest extends BaseIntegrationTest
{
    @Test
    public void testRegisterConnectorFactoryOk() throws Exception
    {
        final CountDownLatch openLatch = new CountDownLatch(1);
        final CountDownLatch closeLatch = new CountDownLatch(1);

        ConnectorFactory factory = new ConnectorFactory()
        {
            @Override
            public Connector createConnector()
            {
                return new AbstractConnector()
                {
                    @Override
                    public void open() throws IOException
                    {
                        openLatch.countDown();
                    }

                    @Override
                    public int getLocalPort()
                    {
                        return 8070;
                    }

                    @Override
                    public Object getConnection()
                    {
                        return null;
                    }

                    @Override
                    public void close() throws IOException
                    {
                        closeLatch.countDown();
                    }

                    @Override
                    protected void accept(int acceptorID) throws IOException, InterruptedException
                    {
                        // nop
                    }
                };
            }
        };

        ServiceRegistration reg = m_context.registerService(ConnectorFactory.class.getName(), factory, null);

        // Should be opened automatically when picked up by the Jetty implementation...
        assertTrue("Felix HTTP Jetty did not open the Connection or pick up the registered ConnectionFactory", openLatch.await(5, TimeUnit.SECONDS));

        // Should close our connection...
        reg.unregister();

        assertTrue("Felix HTTP Jetty did not close the Connection", closeLatch.await(5, TimeUnit.SECONDS));
    }
}
