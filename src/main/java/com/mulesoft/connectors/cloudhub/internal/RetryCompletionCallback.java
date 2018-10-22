/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal;

import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;

import com.mulesoft.connectors.cloudhub.internal.error.CloudHubException;

import java.util.function.Consumer;

/**
 * CompletionCallback decorator which intercepts error notifications and if the error is a CloudHub Internal Server Error
 * retries the given runnable up to 5 times.
 *
 * @since 1.0.1
 */
public class RetryCompletionCallback<P, A> implements CompletionCallback<P, A> {

  private Consumer<CompletionCallback<P, A>> runnable;
  private CompletionCallback<P, A> completionCallback;
  private int counter = 0;

  public RetryCompletionCallback(Consumer<CompletionCallback<P, A>> runnable, CompletionCallback<P, A> completionCallback) {
    this.runnable = runnable;
    this.completionCallback = completionCallback;
  }

  @Override
  public void success(Result<P, A> result) {
    completionCallback.success(result);
  }

  @Override
  public void error(Throwable e) {
    if (e instanceof CloudHubException && ((CloudHubException) e).getStatusCode() == 500 && counter < 5) {
      counter++;
      runnable.accept(this);
    } else {
      completionCallback.error(e);
    }
  }

  public void execute() {
    runnable.accept(this);
  }
}
