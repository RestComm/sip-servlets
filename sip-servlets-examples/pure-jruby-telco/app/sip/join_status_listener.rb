require 'java'
load 'player_listener.rb'

# listener on Media Session creation when the phone and the application can start to exchange media
class JoinStatusListener
  include javax.media.mscontrol.join.JoinEventListener

  WELCOME_MSG = "http://" + java.lang.System.get_property("jboss.bind.address", "127.0.0.1") + ":8080/pure-jruby-telco/audio/dtmf_welcome.wav"

  def initialize
  end
 
  
  def onEvent(event) 

      media_group = event.get_this_joinable
      if (event.is_successful)
        if (javax.media.mscontrol.join.JoinEvent.JOINED == event.get_event_type) 
        	puts "both parties joined. Start Player"
       		begin
       	  
          	player = media_group.get_player
          	player.add_listener(PlayerListener.new)          
          	prompt = java.net.URI.create(WELCOME_MSG)
          
          	# we play the welcome message and listen for DTMF
	       	player.play(prompt, nil, nil)

        	rescue MsControlException => e
          		e.backtrace;
        	end
      	elsif (javax.media.mscontrol.join.JoinEvent.UNJOINED == event.get_event_type)                                
        	puts "both parties unjoined"
        end
      else 
        puts "Joining of both parties failed"
      end
   end
end