/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal;

import org.mule.extension.api.NotificationStatus;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

public class NotificationFilterConfiguration {

  /**
   * Filters the notifications by status. Read, Unread or All.
   */
  @Parameter
  @Optional(defaultValue = "UNREAD")
  private NotificationStatus status;

  /**
   * If specified, only return notifications where the message contains this string. (Case Insensitive)
   */
  @Optional
  @Parameter
  private String search;

  public NotificationStatus getStatus() {
    return status;
  }

  public String getSearch() {
    return search;
  }
}
