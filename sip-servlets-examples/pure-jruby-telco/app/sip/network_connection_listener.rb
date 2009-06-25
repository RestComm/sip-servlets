import javax.media.mscontrol.networkconnection.NetworkConnection
import javax.media.mscontrol.resource.Error
import javax.servlet.sip.SipServletResponse

class NetworkConnectionListener
	include javax.media.mscontrol.resource.MediaEventListener
	
  def initialize    
  end
  
	def onEvent(event)
		puts "onEvent called" 
		puts event
		
		conn = event.get_source;
		media_session = conn.get_media_session

		sip_session = media_session.get_attribute("SIP_SESSION")
    if sip_session.get_attribute("UNANSWERED_INVITE") != nil
		  invite = sip_session.get_attribute("UNANSWERED_INVITE")
		  sip_session.remove_attribute("UNANSWERED_INVITE")
      
      if Error.e_OK.equals(event.get_error) && NetworkConnection.ev_Modify.equals(event.get_event_type) 
        response = invite.create_response(SipServletResponse.SC_OK)
        begin
          sdp = conn.get_local_session_description
  
          response.set_content(sdp, "application/sdp")
          response.send
            
          sip_session.set_attribute("NETWORK_CONNECTION", conn)
        rescue Exception => e
          puts e.get_message
          sip_session.get_application_session.invalidate
          media_session.release
        end
      else
        begin
          if (NetworkConnection.e_ModifyOffersRejected.equals(event.get_error)) 
            # Send 488 error response to INVITE
            invite.create_response(SipServletResponse.SC_NOT_ACCEPTABLE_HERE).send
          elseif (NetworkConnection.e_ResourceNotAvailable.equals(event.get_error)) 
            # Send 486 error response to INVITE
            invite.create_response(SipServletResponse.SC_BUSY_HERE).send
          else 
            # Some unknown error. Send 500 error response to INVITE
            invite.create_response(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send                    
          end
          # Clean up media session
          sip_session.remove_attribute("MEDIA_SESSION")
          media_session.release        
        rescue Exception => e
          puts e.get_message
          # Clean up
          sip_session.get_application_session.invalidate
          media_session.release
        end
      end
    elseif Error.e_OK.equals(event.get_error) && NetworkConnection.ev_LocalSessionDescriptionModified.equals(event.get_event_type)
        invite = sip_session.get_attribute("INVITE")
        sip_session.remove_attribute("INVITE")
        sdp = conn.get_local_session_description
        invite.set_content(sdp, "application/sdp")
        invite.send
          
        sip_session.set_attribute("NETWORK_CONNECTION", conn)
    end  	
	end	
end 