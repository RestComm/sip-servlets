package io.undertow.examples.sipservlet.asc;

import java.io.IOException;

//import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

//import org.example.messaging.JMSMessageSender;


public class MySipServlet extends SipServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2366426255151200498L;
	private static SipServletRequest sfullFirstInvite;
	private static SipServletRequest sfullSecondInvite;

	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		System.out.println("the MySipServlet servlet has been started");
		super.init(servletConfig);
	}

	@Override
	//TODO kell ez?
	protected void doInfo(SipServletRequest req) throws ServletException,IOException {
	    System.out.println("MySipServlet INFO: Got request:\n" + req.getMethod());

	    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
		
	    resp.send();
	}
	
	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,
			IOException {
	    System.out.println("MySipServlet INVITE: Got request:\n" + req.getMethod());

	    sfullSecondInvite = req;

		//100 trying valasz csak akkor megy, ha doInvite "lass�"
		/*try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	    
	    SipServletResponse resp180 = req.createResponse(SipServletResponse.SC_RINGING);
	    
	    resp180.addHeader("SfullTest", "true");
	    resp180.send();
	    
	    
	}
	
	@Override
	protected void doNotify(SipServletRequest req) throws ServletException,IOException {
	    System.out.println("MySipServlet NOTIFY: Got request:\n" + req.getMethod());
	    
	    String content = new String((byte[])req.getContent());
	    System.out.println("MySipServlet NOTIFY: request content:\n" + content);

	    SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
	    resp.addHeader("Expires", "0");
	    resp.send();

	}
	
	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {

		SipServletRequest ssreq = null;
		SipServletResponse ssresp = null;


		if (req instanceof SipServletRequest) {
			ssreq = (SipServletRequest) req;
		} else if (resp instanceof SipServletResponse) {
			ssresp = (SipServletResponse) resp;
		} else {
			System.out.println("Neither SipServletRequest, nor SipServletResponse arrived to RootServlet");
			if (req != null) {
				System.out.println("Request:\n{}" + req);
			} else if (resp != null) {
				System.out.println("Response:\n{}" + resp);
			} else {
				System.out.println("Both request and response are null!");
			}
			return;
		}
		if (ssresp != null && "OPTIONS".equals(ssresp.getMethod())) {
			System.out.println("OPTIONS ping response arrived.");
			if (ssresp.getStatus() == 200) {
				System.out.println("OPTIONS OK.");
			} else {
				System.out.println("OPTIONS failed, status:"+ ssresp.getStatus());
			}
		}else if (ssresp != null){
			System.out.println("Response arrived. StatusCode:"+ssresp.getStatus());
			if(ssresp.getStatus() == SipServletResponse.SC_SESSION_PROGRESS){
				SipServletRequest prack = ssresp.createPrack();

				if(ssresp.getHeader("SfullTest")!=null){
					prack.addHeader("SfullTest", ssresp.getHeader("SfullTest"));
					sfullFirstInvite = ssresp.getRequest();
		 		}else if(ssresp.getHeader("CancelTest")!=null){
		 			SipServletRequest cancelReq = ssresp.getRequest().createCancel();
		 			cancelReq.send();
		 			return;
		 		}

				
				prack.send();
			}/*A STACK automatikusan kik�ldi:
			else if (ssresp.getStatus() == SipServletResponse.SC_MOVED_TEMPORARILY){
				SipServletRequest ack =ssresp.createAck();
				ack.send();
			}*/else if(ssresp.getStatus() == SipServletResponse.SC_RINGING && "true".equals(ssresp.getHeader("SfullTest")))
			{
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				SipServletResponse busyResp = sfullSecondInvite.createResponse(SipServletResponse.SC_BUSY_HERE);
				busyResp.addHeader("SfullTest", "true");
				busyResp.send();
				
			}else if(ssresp.getStatus() == SipServletResponse.SC_MOVED_TEMPORARILY && "true".equals(ssresp.getHeader("ConcurrencyTest"))){
				for(int i=0;i<100;i++){
					ssresp.getApplicationSession().setAttribute("i", i);
					System.out.println(ssresp.getApplicationSession().getId()+" appsession 'i' attribute is:"+ssresp.getApplicationSession().getAttribute("i"));
				}
			}/*else if(ssresp.getStatus() == SipServletResponse.SC_MOVED_TEMPORARILY && "true".equals(ssresp.getHeader("ConcurrencyTestMDB"))){
				TODO disabled temporary
				 * JMSMessageSender sender = new JMSMessageSender();
				try {
					sender.setupPTP();
					sender.sendAsync(ssresp.getApplicationSession().getId());
					
				} catch (JMSException e) {
					e.printStackTrace();
				} catch (NamingException e) {
					e.printStackTrace();
				}finally{
					try {
						sender.stop();
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}
			}*/
			else {
				super.service(req, resp);
			}
		}
		else{
			super.service(req, resp);
		}
	}
}
