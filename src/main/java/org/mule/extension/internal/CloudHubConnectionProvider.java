/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal;

import static org.mule.extension.internal.error.CloudHubError.INVALID_CREDENTIALS;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.extension.internal.error.CloudHubConnectivityException;
import org.mule.extension.internal.error.CloudHubException;
import org.mule.extension.internal.value.provider.AnypointPlatformUrlProvider;
import org.mule.extension.internal.value.provider.EnvironmentsValueProvider;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.values.OfValues;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Connection Provider that provides {@link CloudHubConnection}.
 *
 * @since 1.0.0
 */
public class CloudHubConnectionProvider implements CachedConnectionProvider<CloudHubConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(CloudHubConnectionProvider.class);

  /**
   * Username of the Anypoint Platform Account
   */
  @Parameter
  private String username;

  /**
   * Password of the Anypoint Platform Account
   */
  @Parameter
  @Password
  private String password;

  /**
   * Platform Environment to use. It is Optional, and by default will be used the one defined as Default in the platform.
   */
  @Parameter
  @Optional
  @OfValues(EnvironmentsValueProvider.class)
  private String environment;

  /**
   * This is used to change between the US and EU Anypoint Platform if is required.
   */
  @Parameter
  @Optional(defaultValue = "https://anypoint.mulesoft.com")
  @Placement(tab = ADVANCED_TAB)
  @OfValues(AnypointPlatformUrlProvider.class)
  @DisplayName("Anypoint Platform URL")
  private String anypointPlatformUrl;

  @Inject
  HttpService httpService;

  @Override
  public CloudHubConnection connect() {
    return new CloudHubConnection(username, password, environment, anypointPlatformUrl, httpService);
  }

  @Override
  public void disconnect(CloudHubConnection connection) {
    connection.stop();
  }

  @Override
  public ConnectionValidationResult validate(CloudHubConnection connection) {
    try {
      HttpResponse httpResponse = connection.users.current.permissions.getPermissions().get();
      int statusCode = httpResponse.getStatusCode();
      if (statusCode > 299) {
        String response = getResponseBody(httpResponse);
        switch (statusCode) {
          case 401:
          case 403: {
            return ConnectionValidationResult.failure("Invalid Credentials. Original Message: " + response,
                                                      new CloudHubException("Invalid Credentials", INVALID_CREDENTIALS));
          }
        }
        return ConnectionValidationResult
            .failure("Unknown Error occurred tyring to validate the connection. Original Message: " + response,
                     new CloudHubConnectivityException("Unknown Error occurred tyring to validate the connection"));
      }
    } catch (InterruptedException | ExecutionException | IOException e) {
      return ConnectionValidationResult.failure("Unexpected Error occurred trying to validate the connection.",
                                                new CloudHubConnectivityException(e.getMessage(), e));
    }
    return ConnectionValidationResult.success();
  }

  private String getResponseBody(HttpResponse response) throws IOException {
    return new String(response.getEntity().getBytes());
  }
}
