/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.connection.v1;

import static org.mule.extension.internal.connection.Operations.NOTIFICATIONS;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;
import static org.mule.runtime.http.api.HttpConstants.Method.PUT;

import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class Notifications {

  private CloudHubConnection cloudHubConnection;

  public Notifications(CloudHubConnection cloudHubConnection) {
    this.cloudHubConnection = cloudHubConnection;
  }

  public CompletableFuture<HttpResponse> get(
                                             String domain,
                                             int limit,
                                             int offset,
                                             String status,
                                             String search) {

    return cloudHubConnection.sendRequest(
                                          requestBuilder -> {
                                            requestBuilder.addQueryParam("domain", domain);
                                            requestBuilder.addQueryParam("limit", String.valueOf(limit));
                                            requestBuilder.addQueryParam("offset", String.valueOf(offset));
                                            if (status != null) {
                                              requestBuilder.addQueryParam("status", status);
                                            }
                                            if (search != null) {
                                              requestBuilder.addQueryParam("search", search);
                                            }
                                          }, NOTIFICATIONS, GET);
  }

  public CompletableFuture<HttpResponse> post(InputStream notification) {
    return cloudHubConnection.sendRequest(
                                          requestBuilder -> {
                                            requestBuilder.addHeader("Content-Type", "application/json");
                                            requestBuilder.entity(new InputStreamHttpEntity(notification));
                                          }, NOTIFICATIONS, POST);
  }

  public CompletableFuture<HttpResponse> put(String notificationId, String notificationStatus) {
    return cloudHubConnection.sendRequest(
                                          requestBuilder -> {
                                            requestBuilder.addHeader("Content-Type", "application/json");
                                            requestBuilder
                                                .entity(new InputStreamHttpEntity(new ByteArrayInputStream((" { \"status\" : \""
                                                    + notificationStatus + "\" }").getBytes())));
                                          }, NOTIFICATIONS + "/" + notificationId, PUT);
  }
}
