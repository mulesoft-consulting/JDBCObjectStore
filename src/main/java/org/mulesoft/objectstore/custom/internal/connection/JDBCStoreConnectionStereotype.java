/*
 * (c) 2003-2020 MuleSoft, Inc. This software is protected under international copyright law. All use of this software is subject to
 * MuleSoft's Master Subscription Agreement (or other Terms of Service) separately entered into between you and MuleSoft. If such an
 * agreement is not in place, you may not use the software.
 */
package org.mulesoft.objectstore.custom.internal.connection;

import static java.util.Optional.of;
import static org.mule.runtime.extension.api.stereotype.MuleStereotypes.CONNECTION_DEFINITION;

import org.mule.runtime.extension.api.stereotype.StereotypeDefinition;

import java.util.Optional;

public class JDBCStoreConnectionStereotype implements StereotypeDefinition {

  @Override
  public String getName() {
    return CONNECTION_DEFINITION.getName();
  }

  @Override
  public Optional<StereotypeDefinition> getParent() {
    return of(new StereotypeDefinition() {

      @Override
      public String getName() {
        return CONNECTION_DEFINITION.getName();
      }

      @Override
      public String getNamespace() {
        return "OS";
      }

      @Override
      public Optional<StereotypeDefinition> getParent() {
        return of(CONNECTION_DEFINITION);
      }
    });
  }
}
