/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.connection;


import org.mule.extension.internal.connection.v1.Account;
import org.mule.extension.internal.connection.v1.Application;
import org.mule.extension.internal.connection.v1.Applications;
import org.mule.extension.internal.connection.v1.Notifications;
import org.mule.extension.internal.connection.v1.Users;
import org.mule.extension.internal.connection.v2.V2;
import org.mule.runtime.http.api.HttpConstants;
import org.mule.runtime.http.api.HttpService;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpClientConfiguration;
import org.mule.runtime.http.api.client.auth.HttpAuthentication;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class CloudHubConnection {

  private final String username;
  private final String password;
  private final String defaultEnviroment;
  private String anypointPlatformUrl;
  private final HttpClient httpClient;
  public final V2 v2;
  public final Account account;
  public final Applications applications;
  public final Users users;
  public final Notifications notifications;

  public CloudHubConnection(String username, String password, String defaultEnviroment, String anypointPlatformUrl,
                            HttpService httpService) {
    this.username = username;
    this.password = password;
    this.defaultEnviroment = defaultEnviroment;
    this.anypointPlatformUrl = anypointPlatformUrl;
    httpClient = createClient(httpService);
    this.v2 = new V2(this);
    this.account = new Account(this);
    this.applications = new Applications(this);
    this.users = new Users(this);
    this.notifications = new Notifications(this);
  }

  public Application application(String domain) {
    return new Application(this, domain);
  }

  private HttpClient createClient(HttpService httpService) {
    HttpClientConfiguration.Builder builder = new HttpClientConfiguration.Builder();
    builder.setName("cloudhub-connector");
    HttpClient httpClient = httpService.getClientFactory().create(builder.build());
    httpClient.start();
    return httpClient;
  }

  public CompletableFuture<HttpResponse> sendRequest(String endpoint, HttpConstants.Method method) {
    return sendRequest(null, endpoint, method);
  }

  public CompletableFuture<HttpResponse> sendRequest(Consumer<HttpRequestBuilder> httpRequestBuilderConsumer, String endpoint,
                                                     HttpConstants.Method method) {
    HttpRequestBuilder builder = HttpRequest.builder();
    if (httpRequestBuilderConsumer != null) {
      httpRequestBuilderConsumer.accept(builder);
    }
    builder.method(method);
    if (defaultEnviroment != null) {
      builder.addHeader("X-ANYPNT-ENV-ID", defaultEnviroment);
    }

    builder.uri(anypointPlatformUrl + "/cloudhub/api" + endpoint);
    return httpClient.sendAsync(builder.build(), 0, false, HttpAuthentication.basic(username, password).build());
  }

  public void stop() {
    httpClient.stop();
  }

}
