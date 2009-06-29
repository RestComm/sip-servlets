require 'java'

class NetworkConnectionListener
	include javax.media.mscontrol.resource.MediaEventListener
	include java.io.Serializable
	
  def initialize    
  end
  
	def onEvent(event)
		puts "NetworkConnectionListener event received " + event.to_string  
		
		conn = event.get_source;
		media_session = conn.get_media_session

		sip_session = media_session.get_attribute("SIP_SESSION")
    	if sip_session.get_attribute("UNANSWERED_INVITE") != nil
		  invite = sip_session.get_attribute("UNANSWERED_INVITE")
		  sip_session.remove_attribute("UNANSWERED_INVITE")
      
	      if javax.media.mscontrol.resource.Error.e_OK.equals(event.get_error) && javax.media.mscontrol.networkconnection.NetworkConnection.ev_Modify.equals(event.get_event_type) 
	        response = invite.create_response(javax.servlet.sip.SipServletResponse.SC_OK)
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
	          if (javax.media.mscontrol.networkconnection.NetworkConnection.e_ModifyOffersRejected.equals(event.get_error)) 
	            # Send 488 error response to INVITE
	            invite.create_response(javax.servlet.sip.SipServletResponse.SC_NOT_ACCEPTABLE_HERE).send
	          elsif (javax.media.mscontrol.networkconnection.NetworkConnection.e_ResourceNotAvailable.equals(event.get_error)) 
	            # Send 486 error response to INVITE
	            invite.create_response(javax.servlet.sip.SipServletResponse.SC_BUSY_HERE).send
	          else 
	            # Some unknown error. Send 500 error response to INVITE
	            invite.create_response(javax.servlet.sip.SipServletResponse.SC_SERVER_INTERNAL_ERROR).send                    
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
	    elsif sip_session.get_attribute("INVITE") != nil && javax.media.mscontrol.resource.Error.e_OK.equals(event.get_error) && javax.media.mscontrol.networkconnection.NetworkConnection.ev_Modify.equals(event.get_event_type)
	        invite = sip_session.get_attribute("INVITE")
	        sip_session.remove_attribute("INVITE")
	        sdp = conn.get_local_session_description
	        invite.set_content(sdp, "application/sdp")
	        invite.send
	          
	        sip_session.set_attribute("NETWORK_CONNECTION", conn)
	    else 
	    	begin
      			media_group = media_session.create_media_group(javax.media.mscontrol.mediagroup.MediaGroupConfig.c_PlayerSignalDetector)
		      	media_group.add_listener(MediaStatusListener.new)		
		      	media_group.join_initiate(javax.media.mscontrol.Joinable::Direction::DUPLEX, conn, self)
		      	
		      	sip_session = media_session.get_attribute("SIP_SESSION")
		      	sip_session.set_attribute("MediaGroup", media_group)
		    rescue javax.media.mscontrol.MsControlException => e
		      puts e.message
		      terminate(sipSession, ms);
    		end
	    end  	
	end	
end 