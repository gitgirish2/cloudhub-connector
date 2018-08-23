/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal.connection.v1;

import com.mulesoft.connectors.cloudhub.internal.connection.CloudHubConnection;
import com.mulesoft.connectors.cloudhub.internal.connection.v1.current.Current;

public class Users {

  CloudHubConnection cloudHubConnection;

  public Current current;

  public Users(CloudHubConnection cloudHubConnection) {
    this.cloudHubConnection = cloudHubConnection;
    this.current = new Current(this, cloudHubConnection);
  }


}
