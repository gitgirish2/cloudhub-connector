/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.value.provider;

import org.mule.extension.internal.CloudHubOperations;
import org.mule.extension.internal.StatisticsInformation;
import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class DomainsValueProvider extends BaseValueProvider {

  private static final String EXPRESSION = "%dw 2.0\n" +
      "output application/java\n" +
      "---\n" +
      "payload map ((val, key) -> {\n" +
      "    \"$(val.domain)\" : val.domain\n" +
      "} ) reduce ($$ ++ $)";

  @Connection
  private CloudHubConnection cloudHubConnection;

  @Override
  public Set<Value> resolve() throws ValueResolvingException {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    Reference<Set<Value>> values = new Reference<>();
    Reference<Throwable> throwableReference = new Reference<>();

    new CloudHubOperations()
        .listApplications(cloudHubConnection, new StatisticsInformation(false, 0, null), false, false, false,
                          createCallbackHandler(countDownLatch, values, throwableReference, EXPRESSION));
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new ValueResolvingException("Unexpected Error", "UNKNOWN", e);
    }

    if (throwableReference.get() != null) {
      throw new ValueResolvingException("Unexpected Error", "UNKNOWN", throwableReference.get());
    }

    return values.get();
  }
}
