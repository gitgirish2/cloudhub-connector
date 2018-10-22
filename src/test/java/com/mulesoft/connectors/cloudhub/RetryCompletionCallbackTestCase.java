/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub;

import static com.mulesoft.connectors.cloudhub.internal.error.CloudHubError.EXECUTION;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;

import com.mulesoft.connectors.cloudhub.internal.RetryCompletionCallback;
import com.mulesoft.connectors.cloudhub.internal.error.CloudHubException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RetryCompletionCallbackTestCase {

  @Mock
  CompletionCallback<Void, Void> delegateCallback;

  @Test
  public void successExecution() {
    RetryCompletionCallback<Void, Void> retryCompletionCallback =
        spy(new RetryCompletionCallback<>((callback) -> callback.success(Result.<Void, Void>builder().build()),
                                          delegateCallback));
    retryCompletionCallback.execute();

    verify(retryCompletionCallback, times(1)).success(any());
    verify(delegateCallback, times(1)).success(any());
  }

  @Test
  public void internalErrorRetries5Times() {
    RetryCompletionCallback retryCompletionCallback =
        spy(new RetryCompletionCallback<>((callback) -> callback.error(new CloudHubException("Some Error", 500, EXECUTION)),
                                          delegateCallback));
    retryCompletionCallback.execute();

    verify(retryCompletionCallback, times(6)).error(any(CloudHubException.class));
    verify(delegateCallback, times(1)).error(any(RuntimeException.class));
  }


  @Test
  public void nonInternalDoesntTriggerRetry() {
    RetryCompletionCallback retryCompletionCallback =
        spy(new RetryCompletionCallback<>((callback) -> callback.error(new CloudHubException("Some Error", 304, EXECUTION)),
                                          delegateCallback));
    retryCompletionCallback.execute();

    verify(retryCompletionCallback, times(1)).error(any(RuntimeException.class));
    verify(delegateCallback, times(1)).error(any(RuntimeException.class));

  }
}
