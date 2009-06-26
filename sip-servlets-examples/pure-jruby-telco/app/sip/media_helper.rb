require 'java'
require 'singleton'

module MediaHelper

	# Property key for the Unique MGCP stack name for this application
	MGCP_STACK_NAME = "mgcp.stack.name"

	# Property key for the IP address where CA MGCP Stack (SIP Servlet Container) is bound
	MGCP_STACK_IP = "mgcp.stack.ip"

	# Property key for the port where CA MGCP Stack is bound
	MGCP_STACK_PORT = "mgcp.stack.port"

	# Property key for the IP address where MGW MGCP Stack (MMS) is bound
	MGCP_PEER_IP = "mgcp.stack.peer.ip"

	# Property key for the port where MGW MGCP Stack is bound
	MGCP_PEER_PORT = "mgcp.stack.peer.port"

	LOCAL_ADDRESS = java.lang.System.get_property("jboss.bind.address", "127.0.0.1")
	CA_PORT = "2727"

	PEER_ADDRESS = java.lang.System.get_property("jboss.bind.address", "127.0.0.1")
	MGW_PORT = "2427"

	STACK_NAME = "SipServlets"

	class MgcpStack
		include Singleton
		
		attr_accessor :msControlFactory
	
		def initialize			
			@properties = java.util.Properties.new
			@properties.set_property(MGCP_STACK_NAME, STACK_NAME)
			@properties.set_property(MGCP_PEER_IP, PEER_ADDRESS)
			@properties.set_property(MGCP_PEER_PORT, MGW_PORT)
	
			@properties.set_property(MGCP_STACK_IP, LOCAL_ADDRESS)
			@properties.set_property(MGCP_STACK_PORT, CA_PORT)
			
      drivers = javax.media.mscontrol.spi.DriverManager.get_drivers
      puts @properties
      if drivers.has_next
        @msControlFactory = drivers.next.get_factory(@properties)        
      end
		end
    
		def create_media_session
			# Create new media session
      if @msControlFactory != nil
		    @msControlFactory.create_media_session
		  else
        return nil
      end
		end
	end
end