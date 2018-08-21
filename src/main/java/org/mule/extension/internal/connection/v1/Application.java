/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.connection.v1;

import org.mule.extension.internal.connection.CloudHubConnection;

public class Application {

  private final CloudHubConnection cloudHubConnection;
  private final String domain;

  public Application(CloudHubConnection cloudHubConnection, String domain) {

    this.cloudHubConnection = cloudHubConnection;
    this.domain = domain;
  }
}
