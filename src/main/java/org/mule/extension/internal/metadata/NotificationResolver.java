/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.metadata;

import static org.mule.metadata.api.model.MetadataFormat.JSON;

import org.mule.metadata.api.builder.ArrayTypeBuilder;
import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.builder.ObjectTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.resolving.OutputStaticTypeResolver;

/**
 * Resolver that describes the structure of a Notification.
 *
 * @since 1.0.0
 */
public class NotificationResolver extends OutputStaticTypeResolver {

  @Override
  public MetadataType getStaticMetadata() {
    ArrayTypeBuilder arrayType = BaseTypeBuilder.create(JSON)
        .arrayType().id("Notifications");
    ObjectTypeBuilder notificationType = arrayType.of()
        .objectType().id("Notification");
    notificationType.addField().key("id").required().value().stringType();
    notificationType.addField().key("domain").required().value().stringType();
    notificationType.addField().key("message").value().stringType();
    notificationType.addField().key("properties").value()
        .objectType().openWith().stringType();
    notificationType.addField().key("read").required().value().booleanType();
    notificationType.addField().key("readOn").value().dateTimeType();
    notificationType.addField().key("createdAt").required().value().dateTimeType();
    notificationType.addField().key("href").required().value().stringType();

    return arrayType.build();
  }
}
