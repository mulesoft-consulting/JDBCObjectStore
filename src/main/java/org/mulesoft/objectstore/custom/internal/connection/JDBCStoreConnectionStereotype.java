/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
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
