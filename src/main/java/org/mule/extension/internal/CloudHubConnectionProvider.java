/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal;

import static org.mule.extension.internal.CloudHubError.INVALID_CREDENTIALS;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import org.mule.extension.internal.connection.CloudHubConnection;
import org.mule.extension.internal.value.provider.AnypointPlatformUrlProvider;
import org.mule.extension.internal.value.provider.EnvironmentsValueProvider;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.values.OfValues;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class (as it's name implies) provides connection instances and the funcionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be
 * declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider
 * will be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and
 * caches connections or simply {@link ConnectionProvider} if you want a new connection each time something requires one.
 */
public class CloudHubConnectionProvider implements PoolingConnectionProvider<CloudHubConnection> {

  private final Logger LOGGER = LoggerFactory.getLogger(CloudHubConnectionProvider.class);

  /**
   * Username of the Anypoint Platform Account
   */
  @Parameter
  private String username;

  @Parameter
  @Password
  private String password;

  @Parameter
  @Optional
  @OfValues(EnvironmentsValueProvider.class)
  private String defaultEnvironment;

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
    return new CloudHubConnection(username, password, defaultEnvironment, anypointPlatformUrl, httpService);
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
        byte[] bytes = httpResponse.getEntity().getBytes();
        switch (statusCode) {
          case 401:
          case 403: {
            return ConnectionValidationResult.failure("Invalid Credentials. Original Message: " + new String(bytes),
                                                      new ModuleException("Invalid Credentials", INVALID_CREDENTIALS));
          }
        }
        return ConnectionValidationResult.failure("Unknown error", new RuntimeException());
      }
    } catch (InterruptedException | ExecutionException e) {
      return ConnectionValidationResult.failure("Unknown error", e);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return ConnectionValidationResult.success();
  }
}
