require 'java'

class SignalDetectorListener
  include javax.media.mscontrol.MediaEventListener

	DTMF_0 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf0.wav"
	DTMF_1 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf1.wav"
	DTMF_2 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf2.wav"
	DTMF_3 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf3.wav"
	DTMF_4 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf4.wav"
	DTMF_5 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf5.wav"
	DTMF_6 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf6.wav"
	DTMF_7 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf7.wav"
	DTMF_8 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf8.wav"
	DTMF_9 = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/dtmf9.wav"
	STAR = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/star.wav"
	POUND = "http://"	+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/pound.wav"
	A = "http://"		+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/A.wav"
	B = "http://"		+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/B.wav"
	C = "http://"		+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/C.wav"
	D = "http://"		+ java.lang.System.get_property("jboss.bind.address", "127.0.0.1")	+ ":8080/pure-jruby-telco/audio/D.wav"

  def initialize    
  end
  
  def onEvent(event) 
     begin
        media_group = event.get_source.get_container

        signal_detector = media_group.get_signal_detector
        signal_detector.remove_listener(self)

        if (event.is_successful && javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent.SIGNAL_DETECTED == event.get_event_type) 
          # get the DTMF and play the corresponding file
          dtmf = event.get_signal_string
          play_dtmf(media_group.get_player, dtmf)
        else
          puts "DTMF detection failed "
        end
      rescue javax.media.mscontrol.MsControlException => e
        puts e.backrace
      end
   end
   
   def play_dtmf(player, dtmf)	
		prompt = nil;

		if dtmf == "0" 
			prompt = java.net.URI.create(DTMF_0)
		elsif dtmf == "1"
			prompt = java.net.URI.create(DTMF_1)
		elsif dtmf == "2" 
			prompt = java.net.URI.create(DTMF_2)
		elsif dtmf == "3" 
			prompt = java.net.URI.create(DTMF_3)
		elsif dtmf == "4" 
			prompt = java.net.URI.create(DTMF_4)
		elsif dtmf == "5" 
			prompt = java.net.URI.create(DTMF_5)
		elsif dtmf == "6" 
			prompt = java.net.URI.create(DTMF_6)
		elsif dtmf == "7" 
			prompt = java.net.URI.create(DTMF_7)
		elsif dtmf == "8" 
			prompt = java.net.URI.create(DTMF_8)
		elsif dtmf == "9" 
			prompt = java.net.URI.create(DTMF_9)
		elsif dtmf == "#" 
			prompt = java.net.URI.create(POUND)
		elsif dtmf == "*" 
			prompt = java.net.URI.create(STAR)
		elsif dtmf == "A" 
			prompt = java.net.URI.create(A)
		elsif dtmf == "B" 
			prompt = java.net.URI.create(B)
		elsif dtmf == "C" 
			prompt = java.net.URI.create(C)
		elsif dtmf == "D" 
			prompt = java.net.URI.create(D)
		else 
			raise javax.media.mscontrol.MsControlException.new("This DigitMap is not recognized " + dtmf)
		end
		
		player.play(prompt, nil, nil);
	end
end