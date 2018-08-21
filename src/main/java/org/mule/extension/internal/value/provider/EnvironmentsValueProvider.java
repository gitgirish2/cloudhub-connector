/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.value.provider;

import org.mule.extension.internal.CloudHubConnectionProvider;
import org.mule.extension.internal.CloudHubOperations;
import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class EnvironmentsValueProvider extends BaseValueProvider {

  private static final String EXPRESSION = "%dw 2.0\n" +
      "output application/java\n" +
      "---\n" +
      "(payload.environments map ((key, val) -> {\n" +
      "    \"$(key.id)\" : key.name\n" +
      "})) reduce ($$ ++ $)";

  @Parameter
  private String password;

  @Parameter
  private String username;

  @Parameter
  private String anypointPlatformUrl;

  @Override
  public Set<Value> resolve() throws ValueResolvingException {
    CloudHubConnection cloudHubConnection = new CloudHubConnection(username, password, null, anypointPlatformUrl, httpService);
    ConnectionValidationResult validate = new CloudHubConnectionProvider().validate(cloudHubConnection);

    if (!validate.isValid()) {
      throw new ValueResolvingException("Connectivity Issues trying to obtain a connection." + validate.getMessage(),
                                        "CONNECTIVITY", validate.getException());
    }

    CountDownLatch countDownLatch = new CountDownLatch(1);
    Reference<Set<Value>> values = new Reference<>();
    Reference<Throwable> throwableReference = new Reference<>();

    new CloudHubOperations().getAccount(cloudHubConnection,
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
