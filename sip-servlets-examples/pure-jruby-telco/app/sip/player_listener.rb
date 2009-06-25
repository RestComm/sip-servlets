#require 'java'
require 'player_listener'

import javax.media.mscontrol.resource.Error
import javax.media.mscontrol.mediagroup.Player

class PlayerListener
  include javax.media.mscontrol.resource.MediaEventListener

  def initialize
  end
 
  def onEvent(event) 
      puts "PlayerListener Received event"
      puts event
      
      player = event.get_source
      media_group = player.get_container
      if (Error.e_OK.equals(event.get_error) && Player.ev_PlayComplete.equals(event.get_event_type))
        puts "Received PlayComplete event"
        begin
          signal_detector = media_group.get_signal_detector
          signal_detector.add_listener(SignalDetectorListener.new)
          signal_detector.receive_signals(1, null, null, null)
        rescue MsControlException => e
          e.backTrace
        end
      else
        puts "Player didn't complete successfully "
      end
   end
end