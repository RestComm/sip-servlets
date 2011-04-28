/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
