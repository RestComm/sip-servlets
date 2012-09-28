package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;

public class StatusServletNotifier implements Runnable {

	private static final String BEGIN_SCRIPT_TAG = "<script type='text/javascript'>\n";
	private static final String END_SCRIPT_TAG = "</script>\n";
	
	HashMap<String, String> users;
	CallStatusContainer calls;
	BlockingQueue<String> eventsQueue;
	ServletConfig config;
	Queue<AsyncContext> queue;

	public StatusServletNotifier() {}
	
	public StatusServletNotifier(Queue<AsyncContext> queue, ServletConfig servletConfig) {
		this.queue = queue;
		this.config = servletConfig;
	}
	
	public void run() {
		boolean done = false;
		while (!done) {
			String event = null;
			try {
				event = eventsQueue.take();
				for (AsyncContext ac : queue) {
					try {
						//Retrieve the Updated users and calls here
						users = (HashMap<String, String>) config.getServletContext().getAttribute("registeredUsersMap");
						if(users == null) users = new HashMap<String, String>();
						calls = (CallStatusContainer) config.getServletContext().getAttribute("activeCalls");
						if(calls == null) calls = new CallStatusContainer();
						
						//Create and dispatch the response. Keep the stream open though.
						PrintWriter acWriter = ac.getResponse().getWriter();
						acWriter.println(printTable(users, calls));
						acWriter.flush();
					} catch(IOException ex) {
						System.out.println(ex);
						queue.remove(ac);
					}
				}
			} catch(InterruptedException iex) {
				done = true;
				System.out.println(iex);
			}
		}
	}
	
	/*
	 * Create the response here
	 */
	public static String printTable(HashMap<String, String> users, CallStatusContainer calls) {
		StringBuffer tableData = new StringBuffer();
		StringBuffer result = new StringBuffer();


		if(users == null || users.isEmpty()) {
			tableData.append("<br/><a>No registered users. Please register at least two SIP User Agents.</a><br/>");
		} else {
			String[] userArray = new String[0];
			userArray = users.keySet().toArray(userArray);

			tableData.append("<table class=\"calltable\">");
			tableData.append("<tr><td bgcolor=\"#DDDDDD\">From \\ To</td>");
			for(String col:userArray) tableData.append("<td class=\"calltable\" bgcolor=\"#EEEEEE\" id=\"to\"><b>" + col + "</b></td>");
			tableData.append("</tr>");
			for(String fromAddr:userArray) {
				tableData.append("<tr><td bgcolor=\"#EEEEEE\" id=\"from\"><b>"+fromAddr +"</b></td>");
				String fromAddrV = users.get(fromAddr);
				for(String toAddr:userArray) {
					if(!toAddr.equals(fromAddr)) {
						String toAddrV = users.get(toAddr);
						String status = calls==null? null:calls.getStatus(fromAddrV, toAddrV);
						if(status == null) status = "FFFFFF"; // This is hex RGB color
						if(status.equals("FFFFFF"))
							tableData.append("<td class=\"calltable\" bgcolor=\"#" + status
									+ "\" align=\"center\"><button type=\"button\" " +
									"onclick=\"call('"+users.get(toAddr)+"','"+users.get(fromAddr)+"')\">call</button></td>");
						else
							tableData.append("<td class=\"calltable\" bgcolor=\"#" + status 
									+ "\" align=\"center\"><a>call in progress </a><button type=\"button\" " +
									"onclick=\"bye('"+users.get(toAddr)+"','"+users.get(fromAddr)+"')\">end</button></td>");
					} else {
						tableData.append("<td class=\"calltable\" ></td>");
					}
				}
				tableData.append("</tr>");
			}
			tableData.append("</table>");
			tableData.append("<br/><button type=\"button\" onclick=\"byeAll()\">Close all calls</button>");
		}

		//Wrap the response in a JSONP 
		result.append(BEGIN_SCRIPT_TAG);
		result.append("parseJsonp({ msg: \"" + tableData.toString().replace("\"", "\\\"") + "\"});\n");
		result.append(END_SCRIPT_TAG);

		return result.toString();
	}
}