/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal.error;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;

public class CloudHubException extends ModuleException {

  private int statusCode;

  public CloudHubException(String message, int statusCode, CloudHubError error) {
    super(message, error);
    this.statusCode = statusCode;
  }

  public CloudHubException(String message, int statusCode, CloudHubError error, ConnectionException e) {
    super(message, error, e);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
