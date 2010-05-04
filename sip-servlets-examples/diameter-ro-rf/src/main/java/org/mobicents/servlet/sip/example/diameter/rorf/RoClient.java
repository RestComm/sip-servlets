package org.mobicents.servlet.sip.example.diameter.rorf;

public interface RoClient {

  /**
   * Reserve some initial units to make sure the call should proceed.
   * No units are consumed in this stage, only reserved.
   * 
   * @param userId the user id.
   * @param serviceContextId the service-id.
   * @throws Exception
   */
  public abstract void reserveInitialUnits(String userId, String serviceContextId) throws Exception;

  /**
   * Starts the actual charging process, consuming the initially reserved units.
   * Updates are automatically handled at the end of each period.
   * 
   * @param userId the user id.
   * @param serviceContextId the service-id.
   * @throws Exception
   */
  public abstract void startCharging(String userId, String serviceContextId) throws Exception;

  /**
   * Stops the charging process, by returning the non-used units.
   * 
   * @param userId the user id.
   * @param serviceContextId the service-id.
   * @throws Exception
   */
  public abstract void stopCharging(String userId, String serviceContextId) throws Exception;

}