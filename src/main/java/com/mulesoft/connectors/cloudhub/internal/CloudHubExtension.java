/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal;

import static org.mule.runtime.api.meta.Category.SELECT;

import com.mulesoft.connectors.cloudhub.internal.error.CloudHubError;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;

/**
 * Connector that provides the capability of Create and List Notifications and retrieve the status of created applications
 * on CloudHub.
 */
@Xml(prefix = "cloudhub")
@Extension(name = "CloudHub", category = SELECT)
@ErrorTypes(CloudHubError.class)
@RequiresEnterpriseLicense(allowEvaluationLicense = true)
@Operations(CloudHubOperations.class)
@ConnectionProviders(CloudHubConnectionProvider.class)
public class CloudHubExtension {

}
