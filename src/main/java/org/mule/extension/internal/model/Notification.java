/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.extension.internal.model;

import java.util.Map;

public class Notification {

  private String domain;
  private String id;
  private String message;
  private Map<String, String> customProperties;

  public Notification(String domain, String id, String message, Map<String, String> customProperties) {
    this.domain = domain;
    this.id = id;
    this.message = message;
    this.customProperties = customProperties;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getId() {
    return id;
  }

  public void setId(String transactionId) {
    this.id = transactionId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Map<String, String> getCustomProperties() {
    return customProperties;
  }

  public void setCustomProperties(Map<String, String> customProperties) {
    this.customProperties = customProperties;
  }
}
