/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.connection.v1.current;

import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.extension.internal.connection.v1.Users;
import org.mule.extension.internal.connection.v1.current.permissions.Permissions;

public class Current {

  private final Users users;
  private final CloudHubConnection cloudHubConnection;

  public Permissions permissions;

  public Current(Users users, CloudHubConnection cloudHubConnection) {
    this.users = users;
    this.cloudHubConnection = cloudHubConnection;
    this.permissions = new Permissions(this, cloudHubConnection);
  }
}
