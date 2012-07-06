/**
 * Mule CloudHub Connector
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

/**
 * This file was automatically generated by the Mule Development Kit
 */
package org.mule.modules.cloudhub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.construct.Flow;
import org.mule.tck.junit4.AbstractMuleContextTestCase;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.transport.NullPayload;

import com.mulesoft.cloudhub.client.Application;
import com.mulesoft.cloudhub.client.Notification;
import com.mulesoft.cloudhub.client.NotificationResults;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class CloudHubConnectorTest extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "mule-config.xml";
    }
    
    @Test
    @Ignore
    public void testStart() throws Exception
    {
        runFlowAndExpect("start", NullPayload.getInstance());
    }

    @Test
    public void testGetListApplications() throws Exception
    {
        MuleEvent event = runFlow("listApplications");
        
        Object payload = event.getMessage().getPayload();
        assertTrue(payload instanceof List);
    }

    @Test
    @Ignore
    public void testGetApplication() throws Exception
    {
        MuleEvent event = runFlow("getApplication");

        Object payload = event.getMessage().getPayload();
        assertTrue(payload instanceof Application);
    }

    @Test
    @Ignore
    public void testStop() throws Exception
    {
        runFlowAndExpect("stop", NullPayload.getInstance());
    }

    @Test
    public void testCreateNotification() throws Exception
    {
        MuleEvent event = runFlow("createNotification");

        Object payload = event.getMessage().getPayload();
        assertTrue(payload instanceof Notification);
        assertEquals("Hello World", ((Notification)payload).getMessage());
    }

    @Test
    public void testDismissAllNotifications() throws Exception
    {
        MuleEvent event = runFlow("dismissAllNotifications");

        Object payload = event.getMessage().getPayload();
        assertTrue(payload instanceof NotificationResults);
        assertEquals(0, ((NotificationResults)payload).getTotal());
    }

    @Test
    public void testDismissNotification() throws Exception
    {
        MuleEvent event = runFlow("dismissNotification");

        Object payload = event.getMessage().getPayload();
        assertTrue(payload instanceof NotificationResults);
        assertEquals(0, ((NotificationResults)payload).getTotal());
    }
    
    @Test
    public void testCreateNotificationFromException() throws Exception
    {
        runFlowAndExpect("createNotificationFromException", NullPayload.getInstance());
    }

    /**
    * Run the flow specified by name and assert equality on the expected output
    *
    * @param flowName The name of the flow to run
    * @param expect The expected output
    */
    protected <T> void runFlowAndExpect(String flowName, T expect) throws Exception
    {
        MuleEvent responseEvent = runFlow(flowName);

        assertEquals(expect, responseEvent.getMessage().getPayload());
    }

    private MuleEvent runFlow(String flowName) throws Exception, MuleException {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = AbstractMuleContextTestCase.getTestEvent(null);
        MuleEvent responseEvent = flow.process(event);
        return responseEvent;
    }

    /**
    * Run the flow specified by name using the specified payload and assert
    * equality on the expected output
    *
    * @param flowName The name of the flow to run
    * @param expect The expected output
    * @param payload The payload of the input event
    */
    protected <T, U> void runFlowWithPayloadAndExpect(String flowName, T expect, U payload) throws Exception
    {
        Flow flow = lookupFlowConstruct(flowName);
        MuleEvent event = AbstractMuleContextTestCase.getTestEvent(payload);
        MuleEvent responseEvent = flow.process(event);

        assertEquals(expect, responseEvent.getMessage().getPayload());
    }

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name)
    {
        return (Flow) muleContext.getRegistry().lookupFlowConstruct(name);
    }
}
