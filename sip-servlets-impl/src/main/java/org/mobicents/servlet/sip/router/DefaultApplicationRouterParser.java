package org.mobicents.servlet.sip.router;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.servlet.sip.SipApplicationRouterInfo;
import javax.servlet.sip.SipRouteModifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a utility class used for parsing the default application router file as
 * defined in Appendix C of JSR289.
 *
 */
public class DefaultApplicationRouterParser {
	private static final int SIP_APPLICATION_ROUTER_INFO_PARAM_NB = 6;
	//the logger
	private static Log log = LogFactory.getLog(DefaultApplicationRouterParser.class);
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
		String darConfigurationFileLocation = System.getProperty("javax.servlet.sip.dar");
		if(log.isDebugEnabled()) {
			log.debug("Default Application Router file Location : "+darConfigurationFileLocation);
		}
		File darConfigurationFile = null;
		try {
			darConfigurationFile = new File(new URI(darConfigurationFileLocation));
		} catch (URISyntaxException e) {
			log.fatal("Cannot find the default application router file ! ",e);
			throw new IllegalArgumentException("The Default Application Router file Location : "+darConfigurationFileLocation+" is not valid ! ",e);
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
	public Map<String, List<SipApplicationRouterInfo>> parse() throws ParseException {
		Map<String, List<SipApplicationRouterInfo>> sipApplicationRoutingInfo = 
			new HashMap<String, List<SipApplicationRouterInfo>>();
		
		Iterator darEntriesIterator = properties.entrySet().iterator();
		while(darEntriesIterator.hasNext()) {
			Entry<String, String> darEntry = (Entry<String, String>)darEntriesIterator.next();
			//get the key
			String sipMethod = darEntry.getKey();
			String sipApplicationRouterInfosStringified = darEntry.getValue();
			//parse the corresponding value  
			List<SipApplicationRouterInfo> sipApplicationRouterInfoList = 
				parseSipApplicationRouterInfos(sipApplicationRouterInfosStringified);			
			sipApplicationRoutingInfo.put(sipMethod, sipApplicationRouterInfoList);
		}
		return sipApplicationRoutingInfo;
	}
		
	/**
	 * Parse a string corresponding to one or more definition of SipApplicationRouterInfo
	 * ex : ("SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0"), ("SimpleSipServlet", "DAR:To", "TERMINATING", "", "NO_ROUTE", "1")
	 * and return the corresponding object list  
	 * @param sipApplicationRouterInfosStringified the stringified list of SipApplicationRouterInfo
	 * @return a list of SipApplicationRouterInfo
	 * @throws ParseException if anything goes wrong during the parsing
	 */
	private List<SipApplicationRouterInfo> parseSipApplicationRouterInfos(String sipApplicationRouterInfosStringified) throws ParseException {
		List<SipApplicationRouterInfo> sipApplicationRouterInfos = new ArrayList<SipApplicationRouterInfo>();
		while(sipApplicationRouterInfosStringified.indexOf("(") != -1) {
			int indexOfLeftParenthesis = sipApplicationRouterInfosStringified.indexOf("(");
			int indexOfRightParenthesis = sipApplicationRouterInfosStringified.indexOf(")");
			if(indexOfLeftParenthesis == -1 || indexOfRightParenthesis == -1) {
				throw new ParseException("Cannot parse the following string from the default application router file" + sipApplicationRouterInfosStringified,0);
			}
				
			String sipApplicationRouterInfoStringified = 
				sipApplicationRouterInfosStringified.substring(indexOfLeftParenthesis, indexOfRightParenthesis +1);
			SipApplicationRouterInfo sipApplicationRouterInfo = parseSipApplicationRouterInfo(sipApplicationRouterInfoStringified);
			//TODO don't add them in list order but get the index order from the default application router properties file
			sipApplicationRouterInfos.add(sipApplicationRouterInfo);
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
	private SipApplicationRouterInfo parseSipApplicationRouterInfo(String sipApplicationRouterInfoStringified) throws ParseException {
		//there will always have 6 parameters in a SipApplicationRouterInfo for the default applicationRouterInfo
		String[] sipApplicationRouterInfoParameters = new String[SIP_APPLICATION_ROUTER_INFO_PARAM_NB];
		
		for (int i = 0; i < SIP_APPLICATION_ROUTER_INFO_PARAM_NB; i++) {
			int indexOfLeftQuote = sipApplicationRouterInfoStringified.indexOf("\"");
			if(indexOfLeftQuote == -1) {
				throw new ParseException("Cannot parse the following string from the default application router file" + sipApplicationRouterInfoStringified,0);
			}
			int indexOfRightQuote = sipApplicationRouterInfoStringified.substring(indexOfLeftQuote + 1).indexOf("\"");
			if(indexOfRightQuote == -1) {				
				throw new ParseException("Cannot parse the following string from the default application router file" + sipApplicationRouterInfoStringified,0);
			}				
			indexOfRightQuote += indexOfLeftQuote;
			String sipApplicationRouterInfoParameter = 
				sipApplicationRouterInfoStringified.substring(indexOfLeftQuote + 1, indexOfRightQuote + 1);
			sipApplicationRouterInfoParameters[i] = sipApplicationRouterInfoParameter;
			sipApplicationRouterInfoStringified = sipApplicationRouterInfoStringified.substring(indexOfRightQuote + 2);
		}	
		//TODO ask EG : the SipApplicationRoutingRegion is missing from the constructor !!
		return new SipApplicationRouterInfo(
				//application name
				sipApplicationRouterInfoParameters[0],
				//subsriberURI
				sipApplicationRouterInfoParameters[1],
				//route
				sipApplicationRouterInfoParameters[3],
				//sip route modifier
				SipRouteModifier.valueOf(SipRouteModifier.class,sipApplicationRouterInfoParameters[4]),
				//stateinfo
				sipApplicationRouterInfoParameters[5]);		
	}
}
