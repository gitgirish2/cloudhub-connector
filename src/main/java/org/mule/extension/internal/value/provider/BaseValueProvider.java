/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.value.provider;

import static java.util.Collections.emptyMap;

import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.api.scheduler.SchedulerService;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.http.api.HttpService;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

abstract class BaseValueProvider implements ValueProvider {

  @Inject
  HttpService httpService;

  @Inject
  private ExpressionManager expressionManager;

  @Inject
  private SchedulerService schedulerService;


  CompletionCallback<InputStream, Void> createCallbackHandler(CountDownLatch countDownLatch, Reference<Set<Value>> values,
                                                              Reference<Throwable> throwableReference, String expression) {
    return new CompletionCallback<InputStream, Void>() {

      @Override
      public void success(Result<InputStream, Void> result) {
        CountDownLatch transformationLatch = new CountDownLatch(1);
        Scheduler scheduler = schedulerService.cpuLightScheduler();
        scheduler.execute(() -> {
          try {

            TypedValue<?> payload =
                expressionManager.evaluate(expression, BindingContext.builder()
                    .addBinding("payload", getTypedValue(result))
                    .build());

            Map<String, String> value = (Map<String, String>) payload.getValue();
            values.set(ValueBuilder.getValuesFor(value == null ? emptyMap() : value));
          } finally {
            transformationLatch.countDown();
          }
        });
        try {
          transformationLatch.await();
        } catch (InterruptedException e) {
          throwableReference.set(e);
        } finally {
          scheduler.stop();
          scheduler = null;

          countDownLatch.countDown();
        }
      }

      @Override
      public void error(Throwable throwable) {
        throwableReference.set(throwable);
        countDownLatch.countDown();
      }
    };
  }

  private TypedValue getTypedValue(Result<InputStream, Void> result) {
    InputStream output = result.getOutput();
    String s = IOUtils.toString(output);
    return new TypedValue<>(s, DataType.builder().type(InputStream.class).mediaType(result.getMediaType().get()).build());
  }
}
