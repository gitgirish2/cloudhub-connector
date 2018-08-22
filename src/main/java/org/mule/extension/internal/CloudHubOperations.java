/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal;

import static java.util.Collections.emptyList;
import static org.mule.extension.api.NotificationStatus.ALL;
import static org.mule.extension.internal.OperationUtils.createCompletionHandler;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import org.mule.extension.api.MarkStatus;
import org.mule.extension.api.NotificationStatus;
import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.extension.internal.metadata.AnyResolver;
import org.mule.extension.internal.metadata.GetApplicationsOutputResolver;
import org.mule.extension.internal.metadata.NotificationResolver;
import org.mule.extension.internal.model.Notification;
import org.mule.extension.internal.value.provider.DomainsValueProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Ignore;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
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
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;


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
                                 @Content(primary = true) @TypeResolver(AnyResolver.class) InputStream message,
                                 @NullSafe @Optional @Content Map<String, String> customProperties,
                                 @Optional String transactionId,
                                 CompletionCallback<Void, Void> completionCallback) {

    String notification = new Gson()
        .toJson(new Notification(domain, transactionId, IOUtils.toString(message), customProperties));

    connection.notifications
        .post(new ByteArrayInputStream(notification.getBytes()))
        .whenCompleteAsync(createCompletionHandler((CompletionCallback) completionCallback));
  }

  /**
   * Lists all the available notifications for a giver domain.
   *
   * @param cloudHubConnection
   * @param domain             Name of the application to gather notifications from
   * @param limit              Max number of notifications to retrieve
   * @param pageSize           Offset of the notifications to retrieve
   * @param status             Filters the notifications by status. Read, Unread or All.
   * @param search             If specified, only return notifications where the message contains this string. (Insensitive)
   * @param completionCallback
   */
  @DisplayName("List Notifications")
  @MediaType("application/java")
  @OutputResolver(output = NotificationResolver.class)
  public PagingProvider<CloudHubConnection, Map<String, Object>> listNotifications(
                                                                                   @OfValues(DomainsValueProvider.class) String domain,
                                                                                   @Optional(defaultValue = "-1") int limit,
                                                                                   @Placement(tab = ADVANCED_TAB) @Optional(
                                                                                       defaultValue = "25") int pageSize,
                                                                                   @Optional(
                                                                                       defaultValue = "UNREAD") NotificationStatus status,
                                                                                   @Optional String search) {

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
        int notificationsToGather;
        if (limit < 0) {
          notificationsToGather = 25;
        } else {
          int missingNotifications = limit - currentOffset;
          notificationsToGather = missingNotifications <= pageSize ? missingNotifications : pageSize;
        }

        if (notificationsToGather <= 0) {
          return emptyList();
        }

        connection.notifications.get(domain, notificationsToGather, currentOffset, toQueryParams(status), search)
            .whenCompleteAsync(createCompletionHandler(completionCallback, "#[output application/java --- payload.data]",
                                                       expressionManager));

        try {
          resolutionLatch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
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

      }
    };

    //    cloudHubConnection.notifications.get(domain, limit, offset, toQueryParams(status), search)
    //        .whenCompleteAsync(createCompletionHandler(completionCallback, "#[payload.data]", expressionManager));
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
