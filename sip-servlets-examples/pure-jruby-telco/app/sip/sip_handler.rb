# Note that the class extend a JBoss provided sip controller called JBoss::Sip::SipBaseHandler
# that mimic the Java Sip Servlet class the ruby way
require 'torquebox/sip/base'

class SipHandler < TorqueBox::Sip::Base
  # Handle INVITE request to setup a call by answering 200 OK
  def do_invite(request) 	
    request.create_response(200).send
  end
  # Handle BYE request to tear down a call by answering 200 OK
  def do_bye(request) 	
    request.create_response(200).send
  end
  # Handle REGISTER request so that a SIP Phone can register with the application by answering 200 OK
  def do_register(request) 	
    request.create_response(200).send
  end
  # Handle a successful response to an application initiated INVITE to set up a call (when a new complaint is filed throught the web part) by send an acknowledgment
  def do_success_response(response)
    response.create_ack.send
  end
end
