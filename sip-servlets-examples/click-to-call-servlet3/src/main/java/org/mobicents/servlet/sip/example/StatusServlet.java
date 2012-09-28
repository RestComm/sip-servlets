package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.AsyncContext;
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

	private static final Queue<AsyncContext> queue = new ConcurrentLinkedQueue<AsyncContext>();

	private static final String JUNK = "<!-- Click-to-Call Application using Servlet 3.0 Server Push -->\n";

	StatusServletNotifier data;
	Thread notifierThread;
	/*
	 * Start a new thread that using the LinkedBlockingQueue, every time there is a new element in the collection
	 * will retrieve the last updated users HashMap and calls container and will use them to produce and dispatch a new repsonse
	 * to the clients in the queue.
	 * Important here is the fact that we never close the stream and we just flush the response so it can be used later when needed.  
	 */
	@Override
	public void init(final ServletConfig config) throws ServletException {
		data = new StatusServletNotifier(queue, config);
		data.eventsQueue = new LinkedBlockingQueue<String>();
		if (data.eventsQueue == null) data.eventsQueue = new LinkedBlockingQueue<String>();

		config.getServletContext().setAttribute("eventsQueue", data.eventsQueue);
				
		notifierThread = new Thread(data);
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
		StatusServletAsyncListener asyncListener = new StatusServletAsyncListener(ac, queue, data);
		ac.addListener(asyncListener);
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

	@SuppressWarnings("unchecked")
	protected void firstTimeReq(AsyncContext ac){
		data.calls = (CallStatusContainer) ac.getRequest().getServletContext().getAttribute("activeCalls");
		data.users = (HashMap<String, String>) ac.getRequest().getServletContext().getAttribute("registeredUsersMap");

		try {
			PrintWriter acWriter = ac.getResponse().getWriter();
			acWriter.println(StatusServletNotifier.printTable(data.users, data.calls));
			acWriter.flush();
		} catch(IOException ex) {
			System.out.println(ex);
			queue.remove(ac);
		}
	}
}


