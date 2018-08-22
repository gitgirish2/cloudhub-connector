/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.error;

import static org.mule.runtime.extension.api.error.MuleErrors.ANY;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;

public enum CloudHubError implements ErrorTypeDefinition<CloudHubError> {

  CONNECTIVITY(MuleErrors.CONNECTIVITY),

  INVALID_CREDENTIALS(CONNECTIVITY),

  EXECUTION(ANY);

  private final ErrorTypeDefinition parent;

  CloudHubError(ErrorTypeDefinition parent) {
    this.parent = parent;
  }

  @Override
  public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
    return Optional.ofNullable(parent);
  }
}
