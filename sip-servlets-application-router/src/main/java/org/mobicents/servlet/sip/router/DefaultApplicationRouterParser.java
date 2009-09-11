/*
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
package org.mobicents.servlet.sip.router;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipApplicationRoutingRegionType;
import javax.servlet.sip.ar.SipRouteModifier;

import org.apache.log4j.Logger;

/**
 * This is a utility class used for parsing the default application router file as
 * defined in Appendix C of JSR289.
 *
 */
public class DefaultApplicationRouterParser {
	private static final int SIP_APPLICATION_ROUTER_INFO_PARAM_NB = 6;
	
	//the logger
	private static Logger log = Logger.getLogger(DefaultApplicationRouterParser.class);
	private Properties properties;
	
	public DefaultApplicationRouterParser() {
		properties = new Properties();  
	}
	/**
	 * Load the configuration file as defined in JSR289 Appendix C ie 
	 * as a system property "javax.servlet.sip.dar"
	 * @throws IllegalArgumentException if anything goes wrong when trying to load the configuration file
	 */
	public void init() {
		//load the configuration file
		String darConfigurationFileLocation = getDarConfigurationFileLocation();
		if(log.isDebugEnabled()) {
			log.debug("Default Application Router file Location : "+darConfigurationFileLocation);
		}
		File darConfigurationFile = null;
		//hack to get around space char in path see http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html, 
		// we create a URL since it's permissive enough
		URL url = null;
		try {
			url = new URL(darConfigurationFileLocation);
		} catch (MalformedURLException e) {
			log.fatal("Cannot find the default application router file ! ",e);
			throw new IllegalArgumentException("The Default Application Router file Location : "+darConfigurationFileLocation+" is not valid ! ",e);
		}
		try {
			darConfigurationFile = new File(new URI(darConfigurationFileLocation));
		} catch (URISyntaxException e) {
			//if the uri contains space this will fail, so getting the path will work
			darConfigurationFile = new File(url.getPath());
		}				
		try {
			FileInputStream fis = new FileInputStream(darConfigurationFile);
			properties.load(fis);
		} catch (FileNotFoundException e) {
			log.fatal("Cannot find the default application router file ! ",e);
			throw new IllegalArgumentException("The Default Application Router file Location : "+darConfigurationFileLocation+" is not valid ! ",e);
		} catch (IOException e) {
			log.fatal("Cannot load the default application router file ! ",e);
			throw new IllegalArgumentException("The Default Application Router file Location : "+darConfigurationFileLocation+" cannot be loaded ! ",e);
		}		
	}
	
	/**
	 * Parse the global default application router file from the loaded properties file from the init method
	 * @return a Map of key as sip method and value as a list of SipApplicationRouterInfo
	 * @throws ParseException if anything goes wrong during the parsing
	 */
	public Map<String, List<DefaultSipApplicationRouterInfo>> parse() throws ParseException {
		Map<String, List<DefaultSipApplicationRouterInfo>> sipApplicationRoutingInfo = 
			new HashMap<String, List<DefaultSipApplicationRouterInfo>>();
		
		Iterator darEntriesIterator = properties.entrySet().iterator();
		while(darEntriesIterator.hasNext()) {
			Entry<String, String> darEntry = (Entry<String, String>)darEntriesIterator.next();
			//get the key
			String sipMethod = darEntry.getKey();
			String sipApplicationRouterInfosStringified = darEntry.getValue();
			//parse the corresponding value  
			List<DefaultSipApplicationRouterInfo> sipApplicationRouterInfoList = 
				parseSipApplicationRouterInfos(sipApplicationRouterInfosStringified);
			sipApplicationRoutingInfo.put(sipMethod, sipApplicationRouterInfoList);
		}
		return sipApplicationRoutingInfo;
	}
	
	/**
	 * Same method as above, but loads DAR configuration from a string.
	 * @param configuration
	 * @return
	 * @throws ParseException
	 */
	public Map<String, List<DefaultSipApplicationRouterInfo>> parse(String configuration) throws ParseException {
		Properties tempProperties = new Properties();
		// tempProperties.load(new StringReader(configuration)); // This needs Java 1.6
		ByteArrayInputStream stringStream = new ByteArrayInputStream(configuration.getBytes());
		try {
			tempProperties.load(stringStream);
		} catch (IOException e) {
			log.warn("Failed to update AR configuration. Will use the old properties.");
			return null;
		}
		this.properties = tempProperties;
		return parse();
	}
	
	/**
	 * Same method as above, but loads DAR configuration from properties.
	 * @param configuration
	 * @return
	 * @throws ParseException
	 */
	public Map<String, List<DefaultSipApplicationRouterInfo>> parse(Properties properties) throws ParseException {
		this.properties = properties;
		return parse();
	}
		
	/**
	 * Parse a string corresponding to one or more definition of SipApplicationRouterInfo
	 * ex : ("SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0"), ("SimpleSipServlet", "DAR:To", "TERMINATING", "", "NO_ROUTE", "1")
	 * and return the corresponding object list  
	 * @param sipApplicationRouterInfosStringified the stringified list of SipApplicationRouterInfo
	 * @return a list of SipApplicationRouterInfo
	 * @throws ParseException if anything goes wrong during the parsing
	 */
	private List<DefaultSipApplicationRouterInfo> parseSipApplicationRouterInfos(String sipApplicationRouterInfosStringified) throws ParseException {
		List<DefaultSipApplicationRouterInfo> sipApplicationRouterInfos = new ArrayList<DefaultSipApplicationRouterInfo>();
		while(sipApplicationRouterInfosStringified.indexOf("(") != -1) {
			int indexOfLeftParenthesis = sipApplicationRouterInfosStringified.indexOf("(");
			int indexOfRightParenthesis = sipApplicationRouterInfosStringified.indexOf(")");
			if(indexOfLeftParenthesis == -1 || indexOfRightParenthesis == -1) {
				throw new ParseException("Cannot parse the following string from the default application router file" + sipApplicationRouterInfosStringified,0);
			}
				
			String sipApplicationRouterInfoStringified = 
				sipApplicationRouterInfosStringified.substring(indexOfLeftParenthesis, indexOfRightParenthesis +1);
			DefaultSipApplicationRouterInfo sipApplicationRouterInfo = parseSipApplicationRouterInfo(sipApplicationRouterInfoStringified);
			//get the index order from the default application router properties file			
			sipApplicationRouterInfos.add(sipApplicationRouterInfo.getOrder(),sipApplicationRouterInfo);
			sipApplicationRouterInfosStringified = sipApplicationRouterInfosStringified.substring(indexOfRightParenthesis + 1);
		}
		return sipApplicationRouterInfos;
	}

	/**
	 * Parse a string corresponding to one definition of SipApplicationRouterInfo and return the corresponding Object
	 * ex : ("SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0") 
	 * @param sipApplicationRouterInfoStringified the stringified SipApplicationRouterInfo
	 * @return the corresponding SipApplicationRouterInfo
	 * @throws ParseException if anything goes wrong during the parsing
	 */
	private DefaultSipApplicationRouterInfo parseSipApplicationRouterInfo(String sipApplicationRouterInfoStringified) throws ParseException {
		//there will always have 6 parameters in a SipApplicationRouterInfo for the default applicationRouterInfo
		String[] sipApplicationRouterInfoParameters = new String[SIP_APPLICATION_ROUTER_INFO_PARAM_NB];
		
		for (int i = 0; i < SIP_APPLICATION_ROUTER_INFO_PARAM_NB; i++) {
			int indexOfLeftQuote = sipApplicationRouterInfoStringified.indexOf("\"");
			if(indexOfLeftQuote == -1) {
				throw new ParseException("Cannot parse the following string from the default application router file" + sipApplicationRouterInfoStringified,0);
			}
			int indexOfRightQuote = sipApplicationRouterInfoStringified.substring(indexOfLeftQuote + 1).indexOf("\"");
			if(indexOfRightQuote == -1) {				
				throw new ParseException("Cannot parse the following string from the default application router file " + sipApplicationRouterInfoStringified,0);
			}				
			indexOfRightQuote += indexOfLeftQuote;
			String sipApplicationRouterInfoParameter = 
				sipApplicationRouterInfoStringified.substring(indexOfLeftQuote + 1, indexOfRightQuote + 1);
			sipApplicationRouterInfoParameters[i] = sipApplicationRouterInfoParameter;
			sipApplicationRouterInfoStringified = sipApplicationRouterInfoStringified.substring(indexOfRightQuote + 2);
		}		
		int order = -1;
		try{
			order = Integer.parseInt(sipApplicationRouterInfoParameters[5]);
		} catch (NumberFormatException nfe) {
			throw new ParseException("Impossible to parse the state info into an integer for this line " + sipApplicationRouterInfoStringified, 0);
		}
		return new DefaultSipApplicationRouterInfo(
				//application name
				sipApplicationRouterInfoParameters[0],
				//subsriberURI
				sipApplicationRouterInfoParameters[1],
				//routing region
				new SipApplicationRoutingRegion(
						sipApplicationRouterInfoParameters[2],
						SipApplicationRoutingRegionType.valueOf(
								SipApplicationRoutingRegionType.class,sipApplicationRouterInfoParameters[2])),
				//route
				new String[]{sipApplicationRouterInfoParameters[3]},
				//sip route modifier
				SipRouteModifier.valueOf(SipRouteModifier.class,sipApplicationRouterInfoParameters[4]),
				//stateinfo
				order);		
	}
	
	public String getDarConfigurationFileLocation() {
		return System.getProperty("javax.servlet.sip.dar");
	}
	
	public Properties getProperties() {
		return properties;
	}
}
