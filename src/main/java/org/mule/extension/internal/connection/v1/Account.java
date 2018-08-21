/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.connection.v1;

import static org.mule.extension.internal.connection.Operations.ACCOUNT;
import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class Account {

  private CloudHubConnection cloudHubConnection;

  public Account(CloudHubConnection cloudHubConnection) {
    this.cloudHubConnection = cloudHubConnection;
  }

  public CompletableFuture<HttpResponse> get() {
    return cloudHubConnection.sendRequest(requestBuilder -> {
    }, ACCOUNT, GET);
  }
}
