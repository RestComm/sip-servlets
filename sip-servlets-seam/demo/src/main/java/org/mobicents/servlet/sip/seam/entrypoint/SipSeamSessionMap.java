package org.mobicents.servlet.sip.seam.entrypoint;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipSession;

public class SipSeamSessionMap implements Map<String,Object>{
	   private SipSession session;

	   public SipSeamSessionMap(SipSession session)
	   {
	      this.session = session;
	   }

	   public void clear()
	   {
	      throw new UnsupportedOperationException(); 
	   }

	   public boolean containsKey(Object key)
	   {
	      return session==null ? false : session.getAttribute( (String) key )!=null;
	   }

	   public boolean containsValue(Object value)
	   {
	      throw new UnsupportedOperationException();
	   }

	   public Set<java.util.Map.Entry<String, Object>> entrySet()
	   {
	      throw new UnsupportedOperationException();
	   }

	   public Object get(Object key)
	   {
	      return session==null ? null : session.getAttribute( (String) key );
	   }

	   public boolean isEmpty()
	   {
	      throw new UnsupportedOperationException();
	   }

	   public Set<String> keySet()
	   {
	      if (session==null)
	      {
	         return Collections.EMPTY_SET;
	      }
	      else
	      {
	         Set<String> keys = new HashSet<String>();
	         Enumeration<String> names = session.getAttributeNames();
	         while ( names.hasMoreElements() )
	         {
	            keys.add( names.nextElement() );
	         }
	         return keys;
	      }
	   }

	   public Object put(String key, Object value)
	   {
	      Object result = session.getAttribute(key);
	      session.setAttribute(key, value);
	      return result;
	   }

	   public void putAll(Map<? extends String, ? extends Object> t)
	   {
	      throw new UnsupportedOperationException();
	   }

	   public Object remove(Object key)
	   {
	      if (session==null)
	      {
	         return null;
	      }
	      else
	      {
	         Object result = session.getAttribute( (String) key );
	         session.removeAttribute( (String) key );
	         return result;
	      }
	   }

	   public int size()
	   {
	      throw new UnsupportedOperationException();
	   }

	   public Collection<Object> values()
	   {
	      throw new UnsupportedOperationException();
	   }
}
