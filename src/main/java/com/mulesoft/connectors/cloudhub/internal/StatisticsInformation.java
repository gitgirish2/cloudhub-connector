/*
 * (c) 2003-2018 MuleSoft, Inc. This software is protected under international copyright
 * law. All use of this software is subject to MuleSoft's Master Subscription Agreement
 * (or other master license agreement) separately entered into in writing between you and
 * MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package com.mulesoft.connectors.cloudhub.internal;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.concurrent.TimeUnit;

/**
 * Parameter Group that contains all the parameter related to Statistics Retrieval Information
 *
 * @since 1.0.0
 */
public class StatisticsInformation {

  public StatisticsInformation() {}

  public StatisticsInformation(boolean retrieveStatistics, int period, TimeUnit periodTimeUnit) {
    this.retrieveStatistics = retrieveStatistics;
    this.period = period;
    this.periodTimeUnit = periodTimeUnit;
  }

  /**
   *  Whether should retrieve Application statistics
   */
  @Parameter
  @Optional(defaultValue = "false")
  private boolean retrieveStatistics;

  /**
   * Time of statistics to gather
   */
  @Parameter
  @Optional(defaultValue = "1")
  private int period = 1;

  /**
   * Time Unit of the period
   */
  @Parameter
  @Optional(defaultValue = "HOURS")
  private TimeUnit periodTimeUnit = TimeUnit.HOURS;

  public boolean isRetrieveStatistics() {
    return retrieveStatistics;
  }

  public int getPeriod() {
    return period;
  }

  public TimeUnit getPeriodTimeUnit() {
    return periodTimeUnit;
  }
}
