/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.connection.v2;

import static org.mule.extension.internal.connection.Operations.V2_ALERTS;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;
import static org.mule.runtime.http.api.HttpConstants.Method.POST;

import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.runtime.http.api.domain.entity.InputStreamHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class Alerts {

  private CloudHubConnection cloudHubConnection;

  public Alerts(CloudHubConnection cloudHubConnection) {
    this.cloudHubConnection = cloudHubConnection;
  }

  public CompletableFuture<HttpResponse> get(Integer offset, Integer limit, String resource) {

    return cloudHubConnection.sendRequest(
                                          requestBuilder -> {
                                            if (offset != null) {
                                              requestBuilder.addQueryParam("offset", String.valueOf(offset));
                                            }
                                            if (limit != null) {
                                              requestBuilder.addQueryParam("limit", String.valueOf(limit));
                                            }
                                            requestBuilder.addQueryParam("resource", resource);
                                          }, V2_ALERTS, GET);
  }

  public CompletableFuture<HttpResponse> post(InputStream alert) {
    return cloudHubConnection.sendRequest(
                                          requestBuilder -> {
                                            requestBuilder.entity(new InputStreamHttpEntity(alert));
                                            requestBuilder.addHeader("Content-Type", "application/json");
                                          }, V2_ALERTS, POST);
  }
}
