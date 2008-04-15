package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChatroomSipServlet extends SipServlet {
	
	private static Log logger = LogFactory.getLog(ChatroomSipServlet.class);
	
	/** Context attribute key to store user list. */
    public static String USER_LIST="userList";
    
    /** Init parameter key to retrieve the chatroom's address. */
    public static String CHATROOM_SERVER_NAME="chatroomservername";

    /** This chatroom server's address, retrieved from the init params. */
    public String serverAddress;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {	
		super.init(servletConfig);
		logger.info("The chat room sip servlet has been started ! ");
		getServletContext().setAttribute(USER_LIST,new ArrayList<String>());
        serverAddress = getServletConfig().getInitParameter(CHATROOM_SERVER_NAME);
        logger.info("serverAddress is : " + serverAddress);
	}

	@Override
	public void destroy() {
		try {
            sendToAll(serverAddress, "Server is shutting down -- goodbye!");
        } catch (Throwable e) { 
        	//ignore all errors when shutting down.
            e.printStackTrace();
        }
		super.destroy();
	}
	
	/** This is called by the container when a MESSAGE message arrives. */    
	protected void doMessage(SipServletRequest request) throws 
            ServletException, IOException {

        request.createResponse(SipServletResponse.SC_OK).send();            

        String message = request.getContent().toString();
        String from = request.getFrom().toString();
        
        //A user asked to quit.
        if(message.equalsIgnoreCase("/quit")) {
            sendToUser(from, "Bye");
            removeUser(from);
            return;
        }
        
        //Add user to the list
        if(!containsUser(from)) {
            sendToUser(from, "Welcome to chatroom " + serverAddress + 
                    ". Type '/quit' to exit.");
            addUser(from);
        }

        if(message.equalsIgnoreCase("/who")) {
            String users = "List of users:\n";
            List<String> list = (List<String>)getServletContext().getAttribute(USER_LIST);
            for (String user : list) {
                users += user + "\n";
            }
            sendToUser(from, users);
            removeUser(from);
            return;
        }

        //If the user is joining the chatroom silently, no message 
        //to broadcast, return. 
        if(message.equalsIgnoreCase("/join")) {
            return;
        }
        
        //We could implement more IRC commands here, 
        //see http://www.mirc.com/cmds.html
        sendToAll(from, message);
	}

    /** 
     * This is called by the container when an error is received 
     * regarding a sent message, including timeouts. 
     */
    protected void doErrorResponse(SipServletResponse response)
            throws ServletException, IOException {
        //The receiver of the message probably dropped off. Remove 
        //him from the list.
        String receiver = response.getTo().toString();
        removeUser(receiver);
    }

    /**
     * This is called by the container when a 2xx-OK message is 
     * received regarding a sent message. 
     */
    protected void doSuccessResponse(SipServletResponse response)
            throws ServletException, IOException {
        //We created the app session, we have to destroy it too.
        response.getApplicationSession().invalidate();  
    }

    private void sendToAll(String from, String message)  
    	    throws ServletParseException, IOException {
        SipFactory factory = (SipFactory)getServletContext().
        	getAttribute("javax.servlet.sip.SipFactory");

        List<String> list = (List<String>)getServletContext().getAttribute(USER_LIST);
        for (String user : list) {
        	SipApplicationSession session = 
                factory.createApplicationSession();
            SipServletRequest request = factory.createRequest(session, 
                    "MESSAGE", serverAddress, user);
            String msg = from + " sent message: \n" + message;
            request.setContent(msg.getBytes(), "text/plain");
            request.send();            
        }
    }

    private void sendToUser(String to, String message)  
            throws ServletParseException, IOException {
        SipFactory factory = (SipFactory)getServletContext().
        	getAttribute("javax.servlet.sip.SipFactory");
        SipApplicationSession session = factory.createApplicationSession();
        SipServletRequest request = factory.createRequest(session, 
                "MESSAGE", serverAddress, to);
        request.setContent(message.getBytes(), "text/plain");
        request.send();
    }

    private boolean containsUser(String from) {
        List<String> list = (List<String>)getServletContext().getAttribute(USER_LIST);
        return list.contains(from);
    }

    private void addUser(String from) {
        List<String> list = (List<String>)getServletContext().getAttribute(USER_LIST);
        list.add(from);
    }

    private void removeUser(String from) {
        List<String> list = (List<String>)getServletContext().getAttribute(USER_LIST);
        list.remove(from);
    }
}