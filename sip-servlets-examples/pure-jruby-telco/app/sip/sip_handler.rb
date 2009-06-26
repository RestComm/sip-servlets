# Note that the class extend a JBoss provided sip controller called JBoss::Sip::SipBaseHandler
# that mimic the Java Sip Servlet class the ruby way
require 'java'
require 'torquebox/sip/base'
require 'media_helper'
require 'network_connection_listener'
require 'media_status_listener'

class SipHandler < TorqueBox::Sip::Base
  # Handle INVITE request to setup a call by answering 200 OK
  def do_invite(request) 	
    request.create_response(180).send
    
    begin  	   		
  		sip_session = request.get_session;
  		# getting the MgcpStack instance and creating a new media session
  		mgcp_stack = MediaHelper::MgcpStack.instance
      media_session = mgcp_stack.create_media_session
      if media_session == nil
        request.create_response(500, "Impossible to create the media session to initialize the RTP connection").send 
        return
      end
  		
  		# Store stuff so it can be responded to later
  		sip_session.set_attribute("MEDIA_SESSION", media_session)
  		media_session.set_attribute("SIP_SESSION", sip_session)
  		sip_session.set_attribute("UNANSWERED_INVITE", request)
  	
  		# Create a new NetworkConnection and attaching a new listener to it to know when the connection will have been created
  		connection = media_session.create_network_connection(javax.media.mscontrol.networkconnection.NetworkConnectionConfig.c_Basic)
  		network_connection_listener = NetworkConnectionListener.new
  		connection.add_listener(network_connection_listener)
  		# asking to modify the connection with the received sdp : the listener will get 
  		# the event notifying it that the connection is ready or not with the corresponding SDP
  		# that will be used to send the 200 response
  		connection.modify(javax.media.mscontrol.networkconnection.NetworkConnection.CHOOSE, request.get_raw_content)
  	rescue Exception => e
  		puts e.backtrace
  		puts e.message
  		request.create_response(500).send	
  	end
  end
  
  # Handle ACK request to play announcement
  def do_ack(request)   
    sip_session = request.get_session

    media_session = sip_session.get_attribute("MEDIA_SESSION")
    begin
      media_group = media_session.create_media_group(javax.media.mscontrol.mediagroup.MediaGroupConfig.c_PlayerSignalDetector)
      media_group.add_listener(MediaStatusListener.new)

      connection = sip_session.get_attribute("NETWORK_CONNECTION")
      media_group.join_initiate(javax.media.mscontrol.Joinable::Direction::DUPLEX, connection, self)
      sip_session.set_attribute("MediaGroup", media_group)
    rescue javax.media.mscontrol.MsControlException => e
      puts e.message
      terminate(sipSession, ms);
    end
  end
  
  # Handle BYE request to tear down a call by answering 200 OK
  def do_bye(request) 	
    # kill the media session if present
  	media_session = request.get_session.get_attribute("MEDIA_SESSION")
  	if media_session != nil
		  media_session.release
    end
    request.create_response(200).send    
  end
  
  # Handle REGISTER request so that a SIP Phone can register with the application by answering 200 OK
  def do_register(request) 	
    request.create_response(200).send
  end
  
  # Handle a successful response to an application initiated INVITE to set up a call (when a new complaint is filed throught the web part) by send an acknowledgment
  def do_success_response(response)
    response.create_ack.send
    sdp = response.get_content    
    connection = response.get_session.getAttribute("NETWORK_CONNECTION")
    connection.modify(javax.media.mscontrol.networkconnection.NetworkConnection.CHOOSE, sdp)
  end
  
  def terminate(sip_session, media_session)
    bye = sip_session.create_request("BYE")
    begin
      bye.send();
      # Clean up media session
      media_session.release
      sip_mession.removeAttribute("MEDIA_SESSION")
    rescue Exception => e
      puts "Terminating: Cannot send BYE"
      puts e.message
      puts e.backtrace
    end
  end
end
