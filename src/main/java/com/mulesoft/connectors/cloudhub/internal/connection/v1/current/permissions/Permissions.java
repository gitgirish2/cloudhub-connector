/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal.connection.v1.current.permissions;

import static org.mule.runtime.http.api.HttpConstants.Method.GET;

import com.mulesoft.connectors.cloudhub.internal.connection.CloudHubConnection;
import com.mulesoft.connectors.cloudhub.internal.connection.v1.current.Current;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class Permissions {

  private final Current current;
  private final CloudHubConnection cloudHubConnection;

  public Permissions(Current current, CloudHubConnection cloudHubConnection) {
    this.current = current;
    this.cloudHubConnection = cloudHubConnection;
  }

  public CompletableFuture<HttpResponse> getPermissions() {
    return cloudHubConnection.sendRequest("/users/current/permissions", GET);
  }
}
