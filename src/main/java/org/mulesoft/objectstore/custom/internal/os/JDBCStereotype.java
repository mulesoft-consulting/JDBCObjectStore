/**
 * All code herein is provided "AS IS". Developer makes no warranties, 
 * express or implied, and hereby disclaims all implied warranties, 
 * including any warranty of merchantability and warranty of fitness for a particular purpose.
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
