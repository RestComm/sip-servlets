#require 'java'

import javax.media.mscontrol.resource.Error
import javax.media.mscontrol.mediagroup.signals.SignalDetector

class SignalDetectorListener
  include javax.media.mscontrol.resource.MediaEventListener

  def initialize    
  end
  
  def onEvent(event) 
     begin
        media_group = event.get_source.get_container

        signal_detector = media_group.get_signal_detector
        signal_detector.remove_listener(self)

        if (Error.e_OK.equals(event.get_error) && SignalDetector.ev_SignalDetected.equals(event.get_event_type))
          sequence = event.get_signal_string
          playDTMF(mg.getPlayer, sequence)
        else
          puts "DTMF detection failed "
        end
      rescue MsControlException => e
        puts e.backrace
      end
   end
end