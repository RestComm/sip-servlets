package org.mobicents.servlet.sip.example;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * MissedCall.java
 *
 * <br>Super project:  mobicents
 * <br>5:36:54 PM Dec 19, 2008 
 * <br>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a> 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a> 
 * @author Erick Svenson
 */
public class MissedCall
{
  private String callee;
  private String  date;

  public MissedCall(String callee, Date date)
  {
    this.callee = callee;
    this.date = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss").format(date);
  }

  public String getNotification()
  {
    return this.callee + " has called you on " + date;
  }
  
  @Override
  public int hashCode()
  {
    return (callee + date).hashCode();
  }

  @Override
  public boolean equals( Object obj )
  {
    if(obj != null && obj instanceof MissedCall)
    {
      MissedCall other = (MissedCall)obj;
      return this.callee.equals(other.callee) && this.date.equals(other.date);
    }

    return false;
  }

  public String getDate()
  {
    return date;
  }

}
