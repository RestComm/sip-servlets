require 'java'
load 'player_listener.rb'

class MediaStatusListener
  include javax.media.mscontrol.StatusEventListener

  WELCOME_MSG = "http://" + java.lang.System.get_property("jboss.bind.address", "127.0.0.1") + ":8080/pure-jruby-telco/audio/dtmf_welcome.wav"

  def initialize
  end
 
  
  def onEvent(event) 

      media_group = event.get_source
      if (event.get_error.equals(javax.media.mscontrol.resource.Error.e_OK) && javax.media.mscontrol.JoinEvent.ev_Joined.equals(event.get_event_type)) 
        # NC Joined to MG
        puts "NC joined to MG. Start Player"
       begin
          player = media_group.get_player
          player.add_listener(PlayerListener.new)

          prompt = java.net.URI.create(WELCOME_MSG)

          player.play(prompt, nil, nil)

        rescue MsControlException => e
          e.backtrace;
        end
      elsif (event.get_error.equals(javax.media.mscontrol.resource.Error.e_OK) && javax.media.mscontrol.JoinEvent.ev_Unjoined.equals(event.get_event_type)) 
        puts "Un-Joined MG and NC"
      else 
        puts "Joining of MG and NC failed"
      end
   end
end