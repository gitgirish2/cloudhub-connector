/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.error;

import static org.mule.extension.internal.error.CloudHubError.CONNECTIVITY;

import org.mule.runtime.extension.api.exception.ModuleException;

public class CloudHubConnectivityException extends ModuleException {

  public <T extends Enum<T>> CloudHubConnectivityException(String message, Exception e) {
    super(message, CONNECTIVITY, e);
  }

  public <T extends Enum<T>> CloudHubConnectivityException(String message) {
    super(message, CONNECTIVITY);
  }

}
