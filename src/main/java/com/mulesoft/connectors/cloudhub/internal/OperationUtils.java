/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal;

import static com.mulesoft.connectors.cloudhub.internal.error.CloudHubError.EXECUTION;
import static com.mulesoft.connectors.cloudhub.internal.error.CloudHubError.INVALID_CREDENTIALS;
import static org.mule.runtime.api.metadata.DataType.JSON_STRING;
import static org.mule.runtime.api.metadata.MediaType.APPLICATION_JSON;

import com.mulesoft.connectors.cloudhub.internal.error.CloudHubException;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

public class OperationUtils {

  private OperationUtils() {}

  static BiConsumer<HttpResponse, Throwable> createCompletionHandler(CompletionCallback<InputStream, Void> competitionCallback) {
    return (s, e) -> {
      if (s.getStatusCode() >= 300) {
        validateHttpResponse(competitionCallback, s, e);
      } else {
        competitionCallback.success(Result.<InputStream, Void>builder().output(s.getEntity().getContent())
            .mediaType(APPLICATION_JSON)
            .build());
      }
    };
  }

  public static BiConsumer<HttpResponse, Throwable> createCompletionHandler(CompletionCallback competitionCallback,
                                                                            String payloadExpression,
                                                                            ExpressionManager expressionManager) {
    return (s, e) -> {
      if (e != null || s.getStatusCode() >= 300) {
        validateHttpResponse(competitionCallback, s, e);
      } else {
        InputStream content = s.getEntity().getContent();

        competitionCallback
            .success(Result.builder().output(expressionManager.evaluate(payloadExpression, createBinding(content)).getValue())
                .mediaType(APPLICATION_JSON)
                .build());
      }
    };
  }

  private static void validateHttpResponse(CompletionCallback<?, ?> competitionCallback, HttpResponse response,
                                           Throwable throwable) {
    if (throwable != null) {
      competitionCallback.error(new RuntimeException("Unexpected Error Occurred", throwable));
    } else {
      String responseMessage;
      try {
        responseMessage = new String(response.getEntity().getBytes());
      } catch (IOException e) {
        competitionCallback.error(new RuntimeException("Unexpected Error Occurred", e));
        return;
      }
      switch (response.getStatusCode()) {
        case 401:
        case 403: {
          competitionCallback
              .error(new CloudHubException("Invalid Credentials. Original Message: " + responseMessage, INVALID_CREDENTIALS,
                                           new ConnectionException("Invalid Credentials")));
          break;
        }
        default:
          competitionCallback.error(new CloudHubException("Unknown Error. Original Message: " + responseMessage, EXECUTION));
      }
    }
  }


  private static BindingContext createBinding(InputStream content) {
    return BindingContext.builder().addBinding("payload", new TypedValue<>(content, JSON_STRING)).build();
  }
}
