require 'java'

# Listener getting the connection creation events
class NetworkConnectionListener
	include javax.media.mscontrol.resource.MediaEventListener
	include java.io.Serializable
	
  	def initialize    
  	end
  
	def onEvent(event)
		puts "NetworkConnectionListener event received " + event.to_string  
		
		# get the media connection
		conn = event.get_source;
		# get the corresponding media session
		media_session = conn.get_media_session
		# get the corresponding sip session
		sip_session = media_session.get_attribute("SIP_SESSION")
				
    	if sip_session.get_attribute("UNANSWERED_INVITE") != nil
    	  # handling the case where the app received an INVITE from a Phone to setup a call
		  invite = sip_session.get_attribute("UNANSWERED_INVITE")
		  sip_session.remove_attribute("UNANSWERED_INVITE")
            	  
	      if javax.media.mscontrol.resource.Error.e_OK.equals(event.get_error) && javax.media.mscontrol.networkconnection.NetworkConnection.ev_Modify.equals(event.get_event_type)
	      	# connection was successfully created, we create the response, set the answer SDP on it and send it out 
	        response = invite.create_response(javax.servlet.sip.SipServletResponse.SC_OK)
	        begin
	          sdp = conn.get_local_session_description
	  
	          response.set_content(sdp, "application/sdp")
	          response.send
	            
	          sip_session.set_attribute("NETWORK_CONNECTION", conn)
	        rescue Exception => e
	          puts e.get_message
	          # if something goes wrong make sure to release the media session
	          sip_session.get_application_session.invalidate
	          media_session.release
	        end
	      else
	        begin
	          if (javax.media.mscontrol.networkconnection.NetworkConnection.e_ModifyOffersRejected.equals(event.get_error)) 
	            # if the INVITE SDP was rejected, Send 488 error response to INVITE
	            invite.create_response(javax.servlet.sip.SipServletResponse.SC_NOT_ACCEPTABLE_HERE).send
	          elsif (javax.media.mscontrol.networkconnection.NetworkConnection.e_ResourceNotAvailable.equals(event.get_error)) 
	            # if no resource is available, Send 486 error response to INVITE
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
	        # handling the case where the app sent an INVITE to a Phone to setup a call
	        # SDP is attached to the INVITE and the INVITE is sent out
	        invite = sip_session.get_attribute("INVITE")
	        sip_session.remove_attribute("INVITE")
	        sdp = conn.get_local_session_description
	        invite.set_content(sdp, "application/sdp")
	        invite.send
	          
	        sip_session.set_attribute("NETWORK_CONNECTION", conn)
	    else 
	    	# handling the case where where the app received a 200 OK to a previously sent INVITE to a Phone to setup a call
	    	begin
	    		# we create the media session and initiate it
      			media_group = media_session.create_media_group(javax.media.mscontrol.mediagroup.MediaGroupConfig.c_PlayerSignalDetector)
		      	media_group.add_listener(MediaStatusListener.new)		
		      	media_group.join_initiate(javax.media.mscontrol.Joinable::Direction::DUPLEX, conn, self)
		      	
		      	sip_session.set_attribute("MediaGroup", media_group)
		    rescue javax.media.mscontrol.MsControlException => e
		      puts e.message
		      # TODO cleanup
    		end
	    end  	
	end	
end 