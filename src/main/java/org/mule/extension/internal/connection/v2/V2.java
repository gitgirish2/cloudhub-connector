/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.connection.v2;

import org.mule.extension.internal.connection.CloudHubConnection;

public class V2 {

  public final Applications applications;
  public final Alerts alerts;

  public V2(CloudHubConnection cloudHubConnection) {
    this.applications = new Applications(cloudHubConnection);
    this.alerts = new Alerts(cloudHubConnection);
  }
}
