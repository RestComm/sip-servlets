package org.jboss.mobicents.seam.actions;

import javax.ejb.Local;

@Local
public interface Authenticator
{
  boolean authenticate();
}
