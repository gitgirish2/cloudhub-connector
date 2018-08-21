/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal;

import static org.mule.extension.api.NotificationStatus.ALL;
import static org.mule.extension.internal.CloudHubError.EXECUTION;
import static org.mule.extension.internal.CloudHubError.INVALID_CREDENTIALS;
import static org.mule.runtime.api.metadata.DataType.JSON_STRING;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;

import org.mule.extension.api.MarkStatus;
import org.mule.extension.api.NotificationStatus;
import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.extension.internal.metadata.GetApplicationsOutputResolver;
import org.mule.extension.internal.metadata.NotificationResolver;
import org.mule.extension.internal.model.Notification;
import org.mule.extension.internal.value.provider.DomainsValueProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.values.OfValues;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import com.google.gson.Gson;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
@Throws(CloudHubErrorProvider.class)
public class CloudHubOperations {

  @Inject
  ExpressionManager expressionManager;

  /**
   * Retrieves all applications in the specified environment.
   *
   * @param cloudHubConnection
   * @param retrieveLogLevels        Whether should retrieve Application log levels
   * @param retrieveTrackingSettings Whether should retrieve Application tracking settings
   * @param retrieveIpAddresses      Whether should retrieve Application IP Addresses
   * @param competitionCallback
   */
  @DisplayName("List Applications")
  @MediaType(MediaType.APPLICATION_JSON)
  @OutputResolver(output = GetApplicationsOutputResolver.class)
  public void listApplications(
                               @Connection CloudHubConnection cloudHubConnection,
                               @ParameterGroup(name = "Statistics Information") StatisticsInformation statisticsInformation,
                               @Optional(defaultValue = "true") boolean retrieveLogLevels,
                               @Optional(defaultValue = "true") boolean retrieveTrackingSettings,
                               @Optional(defaultValue = "true") boolean retrieveIpAddresses,
                               CompletionCallback<InputStream, Void> competitionCallback) {
    cloudHubConnection.v2.applications
        .get(statisticsInformation.isRetrieveStatistics(),
             String.valueOf(statisticsInformation.getPeriodTimeUnit().toMillis(statisticsInformation.getPeriod())),
             retrieveLogLevels, retrieveTrackingSettings, retrieveIpAddresses)
        .whenCompleteAsync(createCompletionHandler(competitionCallback));
  }




  /**
   * Retrieves a single application by name.
   *
   * @param cloudHubConnection
   * @param domain              The name of the application
   * @param competitionCallback
   */
  @DisplayName("Get Application")
  @MediaType(MediaType.APPLICATION_JSON)
  @OutputResolver(output = GetApplicationsOutputResolver.class)
  public void getApplication(
                             @Connection CloudHubConnection cloudHubConnection,
                             @OfValues(DomainsValueProvider.class) String domain,
                             CompletionCallback<InputStream, Void> competitionCallback) {
    cloudHubConnection.v2.applications
        .getApplication(domain)
        .whenCompleteAsync(createCompletionHandler(competitionCallback));
  }

  /**
   * Creates a new Notification
   *
   * @param connection
   * @param domain             Name of the application to bind the new notification
   * @param message            Notification's message
   * @param customProperties   Additional properties for the notification
   * @param transactionId      Transaction ID for the Notification
   * @param completionCallback
   */
  @DisplayName("Create Notification")
  public void createNotification(@Connection CloudHubConnection connection,
                                 @OfValues(DomainsValueProvider.class) String domain,
                                 @Content(primary = true) InputStream message,
                                 @Content Map<String, String> customProperties,
                                 @Optional String transactionId,
                                 CompletionCallback<Void, Void> completionCallback) {

    String notification = new Gson()
        .toJson(new Notification(domain, transactionId, IOUtils.toString(message), customProperties));

    connection.notifications
        .post(new ByteArrayInputStream(notification.getBytes()))
        .whenCompleteAsync(createCompletionHandler((CompletionCallback) completionCallback));
  }

  /**
   * Lists all the available notifications
   *
   * @param cloudHubConnection
   * @param domain             Name of the application to gather notifications from
   * @param limit              Max number of notifications to retrieve
   * @param offset             Offset of the notifications to retrieve
   * @param status             Filters the notifications by status. Read, Unread or All.
   * @param search             If specified, only return notifications where the message contains this string. (Insensitive)
   * @param completionCallback
   */
  @DisplayName("List Notifications")
  @MediaType(MediaType.APPLICATION_JSON)
  @OutputResolver(output = NotificationResolver.class)
  public void listNotifications(
                                @Connection CloudHubConnection cloudHubConnection,
                                @OfValues(DomainsValueProvider.class) String domain,
                                @Optional(defaultValue = "25") int limit,
                                @Optional(defaultValue = "0") int offset,
                                @Optional(defaultValue = "UNREAD") NotificationStatus status,
                                @Optional String search,
                                CompletionCallback<InputStream, Void> completionCallback) {

    cloudHubConnection.notifications.get(domain, limit, offset, toQueryParams(status), search)
        .whenCompleteAsync(createCompletionHandler(completionCallback, "#[payload.data]"));
  }



  /**
   * Marks a notification as Read or Unread
   *
   * @param cloudHubConnection
   * @param notificationId     ID of the notification
   * @param markAs             Action to do. Mark as Read or Unread
   * @param completionCallback
   */
  public void markNotification(@Connection CloudHubConnection cloudHubConnection,
                               String notificationId,
                               MarkStatus markAs,
                               CompletionCallback<Void, Void> completionCallback) {
    cloudHubConnection.notifications.put(notificationId, markAs.toString())
        .whenCompleteAsync(createCompletionHandler((CompletionCallback) completionCallback));
  }


  private String toQueryParams(NotificationStatus status) {
    if (status.equals(ALL)) {
      return null;
    } else {
      return status.toString();
    }
  }


  private BiConsumer<HttpResponse, Throwable> createCompletionHandler(CompletionCallback<InputStream, Void> competitionCallback) {
    return (s, e) -> {
      if (s.getStatusCode() >= 300) {
        validateHttpResponse(competitionCallback, s, e);
      } else {
        competitionCallback.success(Result.<InputStream, Void>builder().output(s.getEntity().getContent())
            .mediaType(APPLICATION_JSON)
            .build());
      }
    };
  }

  @Ignore
  public void getAccount(@Connection CloudHubConnection cloudHubConnection,
                         CompletionCallback<InputStream, Void> competitionCallback) {
    cloudHubConnection.account
        .get()
        .whenCompleteAsync(createCompletionHandler(competitionCallback));
  }

  private void validateHttpResponse(CompletionCallback<?, ?> competitionCallback, HttpResponse response, Throwable throwable) {
    if (throwable != null) {
      competitionCallback.error(throwable);
    } else {
      String responseMessage;
      try {
        responseMessage = new String(response.getEntity().getBytes());
      } catch (IOException e) {
        competitionCallback.error(e);
        return;
      }
      switch (response.getStatusCode()) {
        case 401:
        case 403: {
          competitionCallback
              .error(new ModuleException("Invalid Credentials. Original Message: " + responseMessage, INVALID_CREDENTIALS,
                                         new ConnectionException("Invalid Credentials")));
          break;
        }
        default:
          competitionCallback.error(new ModuleException("Unknown Error. Original Message: " + responseMessage, EXECUTION));
      }
    }
  }

  private BiConsumer<HttpResponse, Throwable> createCompletionHandler(CompletionCallback competitionCallback,
                                                                      String payloadExpression) {
    return (s, e) -> {
      if (e != null || s.getStatusCode() >= 300) {
        validateHttpResponse(competitionCallback, s, e);
      } else {
        InputStream content = s.getEntity().getContent();

        competitionCallback
            .success(Result.builder().output(expressionManager.evaluate(payloadExpression, createBinding(content)).getValue())
                .mediaType(APPLICATION_JSON)
                .build());
      }
    };
  }

  private BindingContext createBinding(InputStream content) {
    return BindingContext.builder().addBinding("payload", new TypedValue<>(content, JSON_STRING)).build();
  }

}
