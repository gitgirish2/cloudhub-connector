/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.metadata;

import static org.mule.metadata.api.model.MetadataFormat.JAVA;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonTypeLoader;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.OutputStaticTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

public abstract class BaseStaticJsonTypeResolver implements OutputTypeResolver {

  @Override
  public String getCategoryName() {
    return getJsonTypeName();
  }

  @Override
  public String getResolverName() {
    return getJsonTypeName();
  }

  @Override
  public MetadataType getOutputType(MetadataContext metadataContext, Object o)
      throws MetadataResolvingException, ConnectionException {
    return getMetadataFrom(getJsonLocation(), getJsonTypeName());
  }

  abstract String getJsonTypeName();

  abstract String getJsonLocation();

  public static MetadataType getMetadataFrom(String resource, String typeAlias) {
    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    return new JsonTypeLoader(IOUtils.toString(resourceAsStream)).load(null, typeAlias).get();
  }
}
