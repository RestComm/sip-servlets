package org.mobicents.servlet.sip.example.diameter.rorf;

public interface RoClientListener {

  /**
   * Callback method for successful request for credit units.
   * 
   * @param amount the amount of granted units
   * @param finalUnits true if these are the last units
   * @throws Exception
   */
  public abstract void creditGranted(long amount, boolean finalUnits) throws Exception;

  /**
   * Callback method for unsuccessful request for credit units.
   * 
   * @param failureCode the code specifying why it failed
   * @throws Exception
   */
  public abstract void creditDenied(int failureCode) throws Exception;

  /**
   * Callback method for end of credit. Service should be terminated.
   * 
   * @throws Exception
   */
  public abstract void creditTerminated() throws Exception;

}
