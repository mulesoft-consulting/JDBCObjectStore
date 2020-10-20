/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright law. All use of this software is subject to
 * MuleSoft's Master Subscription Agreement (or other Terms of Service) separately entered into between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mulesoft.objectstore.custom.internal.os;

import org.mule.runtime.extension.api.stereotype.MuleStereotypeDefinition;

public class JDBCStereotype extends MuleStereotypeDefinition {

  private static final String JDBC_OBJECT_STORE_STEREOTYPE_NAME = "JDBC_OBJECT_STORE_STEREOTYPE";

  @Override
  public String getName() {
    return JDBC_OBJECT_STORE_STEREOTYPE_NAME;
  }
}
