/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.value.provider;

import static org.mule.runtime.extension.api.values.ValueBuilder.newValue;

import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueProvider;

import java.util.HashSet;
import java.util.Set;

public class AnypointPlatformUrlProvider implements ValueProvider {

  @Override
  public Set<Value> resolve() {
    Set<Value> values = new HashSet<>();
    values.add(newValue("https://anypoint.mulesoft.com").withDisplayName("https://anypoint.mulesoft.com (US)").build());
    values.add(newValue("https://eu1.anypoint.mulesoft.com").withDisplayName("https://eu1.anypoint.mulesoft.com (EU)").build());
    return values;
  }
}
