/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal.connection.v2;

import static com.mulesoft.connectors.cloudhub.internal.connection.Operations.V2_APPLICATIONS;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import com.mulesoft.connectors.cloudhub.internal.connection.CloudHubConnection;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class Applications {

  private CloudHubConnection cloudHubConnection;

  Applications(CloudHubConnection cloudHubConnection) {
    this.cloudHubConnection = cloudHubConnection;
  }

  public CompletableFuture<HttpResponse> get(
                                             boolean retrieveStatistics,
                                             String period,
                                             boolean retrieveLogLevels,
                                             boolean retrieveTrackingSettings,
                                             boolean retrieveIpAddresses) {

    return cloudHubConnection.sendRequest(
                                          requestBuilder -> {
                                            requestBuilder.addQueryParam("retrieveStatistics",
                                                                         String.valueOf(retrieveStatistics));
                                            requestBuilder.addQueryParam("period", period);
                                            requestBuilder.addQueryParam("retrieveLogLevels", String.valueOf(retrieveLogLevels));
                                            requestBuilder.addQueryParam("retrieveTrackingSettings",
                                                                         String.valueOf(retrieveTrackingSettings));
                                            requestBuilder.addQueryParam("retrieveIpAddresses",
                                                                         String.valueOf(retrieveIpAddresses));
                                          }, V2_APPLICATIONS, GET);
  }

  public CompletableFuture<HttpResponse> getApplication(String domain) {
    return cloudHubConnection.sendRequest(V2_APPLICATIONS + "/" + domain, GET);
  }
}
