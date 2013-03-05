package org.mule.modules.cloudhub;

import com.mulesoft.cloudhub.client.CloudhubConnection;
import com.mulesoft.cloudhub.client.Connection;
import com.mulesoft.cloudhub.client.Notification;
import com.mulesoft.cloudhub.client.NotificationResults;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mule.api.ExceptionPayload;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mule.modules.cloudhub.CloudHubConnector.getStackTrace;

/**
 * <p>
 *     Unit test for the Cloudhub connector
 * </p>
 */
public class CloudHubConnectorTest {
    public static final NotificationResults NOTIFICATION_RESULTS = new NotificationResults();
    public static final String TENANT_ID_PROPERTY = "tenant.id";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String TENANT_ID = "tenantId";
    public static final Exception EXCEPTION = new Exception();
    public static final String EXCEPTION_MESSAGE = "exceptionMessage";

    CloudhubConnection connection = mock(CloudhubConnection.class);
    MuleEvent muleEvent = mock(MuleEvent.class);
    MuleMessage muleMessage = mock(MuleMessage.class);
    ExceptionPayload exceptionPayload = mock(ExceptionPayload.class);

    @Before
    public void defineBehaviour(){
        when(muleEvent.getMessage()).thenReturn(muleMessage);
    }

    /**
     * <p>
     *     The connection object allows null values when listing notifications we validate that we don't do unnecessary
     *     parameteres checking
     * </p>
     */
    @Test
    public void listingNotificationsWithNullValues(){

        when(muleMessage.getInboundProperty(TENANT_ID_PROPERTY)).thenReturn(null);
        when(connection.listNotifications(null,null,null)).thenReturn(NOTIFICATION_RESULTS);

        assertEquals(NOTIFICATION_RESULTS, connector().listNotifications(null,null, muleEvent));


        verify(muleMessage, times(1)).getInboundProperty("tenant.id");
        verify(connection, times(1)).listNotifications(null,null,null);
    }

    /**
     * <p>
     *     If values are set then call Cloudhub connection correctly
     * </p>
     */
    @Test
    public void listingNotificationsWithCorrectValues(){
        when(muleMessage.getInboundProperty(TENANT_ID_PROPERTY)).thenReturn(TENANT_ID);
        when(connection.listNotifications(5,10,TENANT_ID)).thenReturn(NOTIFICATION_RESULTS);

        assertEquals(NOTIFICATION_RESULTS, connector().listNotifications(5,10, muleEvent));

        verify(muleMessage, times(1)).getInboundProperty(TENANT_ID_PROPERTY);
        verify(connection, times(1)).listNotifications(5,10, TENANT_ID);
    }


    /**
     * <p>
     *     The tenant id is an valid argument if it null. It can be sent to the connection
     * </p>
     */
    @Test
    public void createNotificationWithoutTenantId(){
        when(muleMessage.getInboundProperty(TENANT_ID_PROPERTY)).thenReturn(null);
        when(muleMessage.getMessageRootId()).thenReturn(TRANSACTION_ID);
        when(muleMessage.getExceptionPayload()).thenReturn(null);

        connector().createNotification("message", Notification.Priority.ERROR, null, muleEvent);

        verify(connection).create(argThat(new NotificationMatcher(null, null,  Notification.Priority.ERROR, null, TRANSACTION_ID)));
    }


    /**
     * <p>
     *     Validate that the tenant id is sent to the connection
     * </p>
     */
    @Test
    public void createNotificationWithTenantId(){
        when(muleMessage.getInboundProperty(TENANT_ID_PROPERTY)).thenReturn(TENANT_ID);
        when(muleMessage.getMessageRootId()).thenReturn(TRANSACTION_ID);
        when(muleMessage.getExceptionPayload()).thenReturn(null);

        connector().createNotification("message", Notification.Priority.ERROR, null, muleEvent);

        verify(connection).create(argThat(new NotificationMatcher(TENANT_ID, null,  Notification.Priority.ERROR, null, TRANSACTION_ID)));
    }

    /**
     * <p>
     *     Validate that the exception properties are set
     * </p>
     */
    @Test
    public void createNotificationWithExceptionPayload(){
        when(muleMessage.getInboundProperty(TENANT_ID_PROPERTY)).thenReturn(TENANT_ID);
        when(muleMessage.getMessageRootId()).thenReturn(TRANSACTION_ID);
        when(muleMessage.getExceptionPayload()).thenReturn(exceptionPayload);
        when(exceptionPayload.getMessage()).thenReturn(EXCEPTION_MESSAGE);
        when(exceptionPayload.getException()).thenReturn(EXCEPTION);

        connector().createNotification("message", Notification.Priority.ERROR, null, muleEvent);

        verify(connection).create(argThat(new NotificationMatcher(TENANT_ID, null, Notification.Priority.ERROR, expectedExceptionProperties(), TRANSACTION_ID)));
    }

    /**
     * <p>
     *     Validate that the exception properties are set
     * </p>
     */
    @Test
    public void mergeProperties(){
        when(muleMessage.getInboundProperty(TENANT_ID_PROPERTY)).thenReturn(TENANT_ID);
        when(muleMessage.getMessageRootId()).thenReturn(TRANSACTION_ID);
        when(muleMessage.getExceptionPayload()).thenReturn(exceptionPayload);
        when(exceptionPayload.getMessage()).thenReturn(EXCEPTION_MESSAGE);
        when(exceptionPayload.getException()).thenReturn(EXCEPTION);

        Map<String, String> customProperties = new HashMap<String, String>();
        customProperties.put("myPropertyKey", "myPropertyValue");

        connector().createNotification("message", Notification.Priority.ERROR, customProperties, muleEvent);

        customProperties.putAll(expectedExceptionProperties());
        verify(connection).create(argThat(new NotificationMatcher(TENANT_ID, null, Notification.Priority.ERROR, customProperties, TRANSACTION_ID)));
    }

    private HashMap<String, String> expectedExceptionProperties() {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("exception.message", EXCEPTION_MESSAGE);
        properties.put("exception.stacktrace", getStackTrace(EXCEPTION));
        return properties;
    }


    private MockConnector connector() {
        return new MockConnector();
    }



    private class MockConnector extends CloudHubConnector{

        @Override
        protected synchronized CloudhubConnection getConnection() {
            return connection;
        }

        @Override
        protected synchronized CloudhubConnection createConnection() {
            return connection;
        }
    }

    private static class NotificationMatcher implements Matcher<Notification> {
        String tenantId;
        String domain;
        Notification.Priority priority;
        Map<String,String> properties;
        String transactionId;

        private NotificationMatcher(String tenantId,
                                    String domain,
                                    Notification.Priority priority,
                                    Map<String, String> properties,
                                    String transactionId) {
            this.tenantId = tenantId;
            this.domain = domain;
            this.priority = priority;
            this.properties = properties;
            this.transactionId = transactionId;
        }

        @Override
        public boolean matches(Object o) {
            Notification notification = (Notification) o;

            return validateEquals(notification.getTenantId(), tenantId) &&
                    validateEquals(notification.getDomain(), domain) &&
                    validateEquals(notification.getPriority(), priority) &&
                    validateEquals(notification.getTransactionId(), transactionId) &&
                    validateEquals(notification.getCustomProperties(), properties);
        }

        @Override
        public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
        }

        @Override
        public void describeTo(Description description) {
        }

        private static boolean validateEquals(Object notificationValue, Object expectedValue) {
            return ( (notificationValue == null && expectedValue == null) || (notificationValue != null && notificationValue.equals(expectedValue)));
        }

    }


}
