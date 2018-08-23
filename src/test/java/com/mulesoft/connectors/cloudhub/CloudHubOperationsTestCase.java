/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.mulesoft.connectors.cloudhub.internal.error.CloudHubError;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connectivity.ConnectivityTestingService;
import org.mule.runtime.api.value.ValueProviderService;
import org.mule.tck.junit4.matcher.ErrorTypeMatcher;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

@ArtifactClassLoaderRunnerConfig(exportPluginClasses = CloudHubError.class)
public class CloudHubOperationsTestCase extends MuleArtifactFunctionalTestCase {

  @Override
  public boolean enableLazyInit() {
    return true;
  }

  @Override
  public boolean disableXmlValidations() {
    return true;
  }

  @Inject
  @Named(ConnectivityTestingService.CONNECTIVITY_TESTING_SERVICE_KEY)
  ConnectivityTestingService connectivityTestingService;

  @Inject
  @Named(ValueProviderService.VALUE_PROVIDER_SERVICE_KEY)
  ValueProviderService valueProviderService;

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void testConnectivity() throws Exception {
    ConnectionValidationResult config =
        connectivityTestingService.testConnection(Location.builder().globalName("config").build());
    assertThat(config.isValid(), is(true));
  }

  @Test
  public void failureTestConnectivity() throws Exception {
    ConnectionValidationResult config =
        connectivityTestingService.testConnection(Location.builder().globalName("config-invalid").build());
    assertThat(config.isValid(), is(false));
    assertThat(config.getErrorType().get(), is(ErrorTypeMatcher.errorType(CloudHubError.INVALID_CREDENTIALS)));
  }

}
