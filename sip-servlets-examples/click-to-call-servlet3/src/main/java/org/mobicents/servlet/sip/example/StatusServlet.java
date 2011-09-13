package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@author gvagenas

@WebServlet(urlPatterns={"/status"}, asyncSupported=true, loadOnStartup=1)
public class StatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String BEGIN_SCRIPT_TAG = "<script type='text/javascript'>\n";
	private static final String END_SCRIPT_TAG = "</script>\n";

	private static final Queue<AsyncContext> queue = new ConcurrentLinkedQueue<AsyncContext>();

	private static final String JUNK = "<!-- Click-to-Call Application using Servlet 3.0 Server Push -->\n";

	HashMap<String, String> users = null;
	CallStatusContainer calls = null;
	BlockingQueue<String> eventsQueue = null;

	private Thread notifierThread = null;

	/*
	 * Start a new thread that using the LinkedBlockingQueue, every time there is a new element in the collection
	 * will retrieve the last updated users HashMap and calls container and will use them to produce and dispatch a new repsonse
	 * to the clients in the queue.
	 * Important here is the fact that we never close the stream and we just flush the response so it can be used later when needed.  
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		eventsQueue = new LinkedBlockingQueue<String>();
		if (eventsQueue == null) eventsQueue = new LinkedBlockingQueue<String>();

		config.getServletContext().setAttribute("eventsQueue", eventsQueue);
		
		Runnable notifierRunnable = new Runnable() {
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
		};
		notifierThread = new Thread(notifierRunnable);
		notifierThread.start();
	}
	/*
	 * doGet will will start an AsyncContext, store the new AsyncContext in order for this to get processed later and last
	 * update the client's page with the current data.
	 * Also sets a timeout in the AsyncContext, and registers an AsyncListener in order to handle the events needed. 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter writer = response.getWriter();
		writer.write(JUNK);

		writer.flush();

		if(!request.isAsyncSupported()){
			PrintWriter out = response.getWriter();
			out.println("Asynchronous request processing is not supported");
			out.flush();
			out.close();
			return;
		}

		final AsyncContext ac = request.startAsync();
		ac.setTimeout(10  * 60 * 1000);
		ac.addListener(new AsyncListener() {
			public void onComplete(AsyncEvent event) throws IOException {
				queue.remove(ac);
			}
			public void onTimeout(AsyncEvent event) throws IOException {
				queue.remove(ac);
			}
			public void onError(AsyncEvent event) throws IOException {
				queue.remove(ac);
			}
			public void onStartAsync(AsyncEvent event) throws IOException {
			}
		});
		//Add this to the queue for further processing when there is a response to dispatch
		queue.add(ac);
		//First time request will get the current data
		firstTimeReq(ac);
	}

	@Override
	public void destroy() {
		queue.clear();
		notifierThread.interrupt();
	}

	/*
	 * Create the response here
	 */
	protected String printTable(HashMap<String, String> users, CallStatusContainer calls) {
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

	@SuppressWarnings("unchecked")
	protected void firstTimeReq(AsyncContext ac){
		calls = (CallStatusContainer) ac.getRequest().getServletContext().getAttribute("activeCalls");
		users = (HashMap<String, String>) ac.getRequest().getServletContext().getAttribute("registeredUsersMap");

		try {
			PrintWriter acWriter = ac.getResponse().getWriter();
			acWriter.println(printTable(users, calls));
			acWriter.flush();
		} catch(IOException ex) {
			System.out.println(ex);
			queue.remove(ac);
		}
	}
}


