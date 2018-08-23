/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal;

import static java.util.Collections.emptyList;
import static com.mulesoft.connectors.cloudhub.api.NotificationStatus.ALL;
import static com.mulesoft.connectors.cloudhub.internal.OperationUtils.createCompletionHandler;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import com.mulesoft.connectors.cloudhub.api.MarkStatus;
import com.mulesoft.connectors.cloudhub.api.NotificationStatus;
import com.mulesoft.connectors.cloudhub.api.Priority;
import com.mulesoft.connectors.cloudhub.internal.connection.CloudHubConnection;
import com.mulesoft.connectors.cloudhub.internal.error.CloudHubErrorProvider;
import com.mulesoft.connectors.cloudhub.internal.metadata.GetApplicationOutputResolver;
import com.mulesoft.connectors.cloudhub.internal.metadata.GetApplicationsOutputResolver;
import com.mulesoft.connectors.cloudhub.internal.metadata.NotificationResolver;
import com.mulesoft.connectors.cloudhub.internal.model.Notification;
import com.mulesoft.connectors.cloudhub.internal.value.provider.DomainsValueProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.values.OfValues;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.streaming.PagingProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import com.google.gson.Gson;


/**
 * CloudHub Operations
 *
 * @since 1.0.0
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
                               @DisplayName("Retrieve Log Levels") @Optional(defaultValue = "true") boolean retrieveLogLevels,
                               @DisplayName("Retrieve Tracking Settings") @Optional(
                                   defaultValue = "true") boolean retrieveTrackingSettings,
                               @DisplayName("Retrieve IP Addresses") @Optional(defaultValue = "true") boolean retrieveIpAddresses,
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
  @OutputResolver(output = GetApplicationOutputResolver.class)
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
   * @param priority           Priority of the notification to create. INFO, WARN or ERROR.
   * @param completionCallback
   */
  @DisplayName("Create Notification")
  public void createNotification(@Connection CloudHubConnection connection,
                                 @OfValues(DomainsValueProvider.class) String domain,
                                 @Content(primary = true) String message,
                                 @NullSafe @Optional @Content Map<String, String> customProperties,
                                 @Optional Priority priority,
                                 @Optional String transactionId,
                                 CompletionCallback<Void, Void> completionCallback) {

    String notification = new Gson()
        .toJson(new Notification(domain, transactionId, message, customProperties, priority));

    connection.notifications
        .post(new ByteArrayInputStream(notification.getBytes()))
        .whenCompleteAsync(createCompletionHandler((CompletionCallback) completionCallback));
  }

  /**
   * Lists all the available notifications for a giver domain.
   *
   * @param domain             Name of the application to gather notifications from
   * @param limit              Number of notifications to retrieve. -1 means everything available.
   * @param pageSize           Size of the page to retrieve per iteration. This only should be changed for performance purposes.
   */
  @DisplayName("List Notifications")
  @MediaType("application/java")
  @OutputResolver(output = NotificationResolver.class)
  public PagingProvider<CloudHubConnection, Map<String, Object>> listNotifications(
                                                                                   @OfValues(DomainsValueProvider.class) String domain,
                                                                                   @Optional(defaultValue = "-1") int limit,
                                                                                   @Placement(tab = ADVANCED_TAB) @Optional(
                                                                                       defaultValue = "25") int pageSize,
                                                                                   @ParameterGroup(
                                                                                       name = "Notification Filter") NotificationFilterConfiguration filterConfiguration) {

    return new PagingProvider<CloudHubConnection, Map<String, Object>>() {

      int currentOffset = 0;

      @Override
      public List<Map<String, Object>> getPage(CloudHubConnection connection) {
        CountDownLatch resolutionLatch = new CountDownLatch(1);

        Reference<List<Map<String, Object>>> result = new Reference<>();
        Reference<Throwable> error = new Reference<>();

        CompletionCallback<List<Map<String, Object>>, Void> completionCallback =
            new CompletionCallback<List<Map<String, Object>>, Void>() {

              @Override
              public void success(Result<List<Map<String, Object>>, Void> notificationResult) {
                try {
                  result.set(notificationResult.getOutput());
                } catch (Exception e) {
                  e.printStackTrace();
                } finally {
                  resolutionLatch.countDown();
                }
              }

              @Override
              public void error(Throwable e) {
                error.set(e);
                resolutionLatch.countDown();
              }
            };
        int notificationsToGather = getNotificationsToGather();
        if (notificationsToGather <= 0) {
          return emptyList();
        }

        connection.notifications.get(domain, notificationsToGather, currentOffset, toQueryParams(filterConfiguration.getStatus()),
                                     filterConfiguration.getSearch())
            .whenCompleteAsync(createCompletionHandler(completionCallback, "#[output application/java --- payload.data]",
                                                       expressionManager));

        try {
          resolutionLatch.await();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        if (error.get() != null) {

          Throwable throwable = error.get();
          if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
          } else {
            throw new RuntimeException(throwable);
          }
        } else {
          List<Map<String, Object>> maps = result.get();
          currentOffset = currentOffset + maps.size();
          return maps;
        }
      }

      @Override
      public java.util.Optional<Integer> getTotalResults(CloudHubConnection connection) {
        return java.util.Optional.empty();
      }

      @Override
      public void close(CloudHubConnection connection) throws MuleException {
        //nothing to do
      }

      private int getNotificationsToGather() {
        int notificationsToGather;
        if (limit < 0) {
          notificationsToGather = 25;
        } else {
          int missingNotifications = limit - currentOffset;
          notificationsToGather = missingNotifications <= pageSize ? missingNotifications : pageSize;
        }
        return notificationsToGather;
      }
    };
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

  @Ignore
  public void getAccount(@Connection CloudHubConnection cloudHubConnection,
                         CompletionCallback<InputStream, Void> competitionCallback) {
    cloudHubConnection.account
        .get()
        .whenCompleteAsync(createCompletionHandler(competitionCallback));
  }

}
