require 'java' 
require 'media_helper'
require 'network_connection_listener'

class ComplaintsController < ApplicationController
  # GET /complaints
  # GET /complaints.xml
  def index
    @complaints = Complaint.find(:all)

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @complaints }
    end
  end

  # GET /complaints/1
  # GET /complaints/1.xml
  def show
    @complaint = Complaint.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.xml  { render :xml => @complaint }
    end
  end

  # GET /complaints/new
  # GET /complaints/new.xml
  def new
    @complaint = Complaint.new

    respond_to do |format|
      format.html # new.html.erb
      format.xml  { render :xml => @complaint }
    end
  end

  # GET /complaints/1/edit
  def edit
    @complaint = Complaint.find(params[:id])
  end

  # POST /complaints
  # POST /complaints.xml
  def create
    @complaint = Complaint.new(params[:complaint])

	# get the sip factory from the servlet context
	sip_factory = $servlet_context.get_attribute('javax.servlet.sip.SipFactory')
	# create a new sip application session
	app_session = request.env['java.servlet_request'].get_session().get_application_session();
	# create a new sip servlet request to start a call to the sip phone with from header equals to "sip:my_jruby_app_rocks@mobicents.org" and the to header equals to the 		sip_uri from the complaint
	sip_request = sip_factory.create_request(app_session, 'INVITE', 'sip:my_jruby_app_rocks@mobicents.org', @complaint.sip_uri);
	
	#MediaHelper
	mgcp_stack = MediaHelper::MgcpStack.instance
	  media_session = mgcp_stack.create_media_session
	  if media_session == nil
	    puts "Impossible to get create the media session to initialize the RTP connection"    
	  else
	    puts media_session
	    
	    sip_session = sip_request.get_session
	    sip_session.set_attribute("MEDIA_SESSION", media_session)
	    media_session.set_attribute("SIP_SESSION", sip_session)
	    sip_session.set_attribute("INVITE", request)
	    
	    # Create a new NetworkConnection to handle RTP and attaching a new listener to it to know when it is created to update
	    # the sip request SDP and send it
	    connection = media_session.create_network_connection(javax.media.mscontrol.networkconnection.NetworkConnectionConfig.c_Basic)
	    network_connection_listener = NetworkConnectionListener.new
	    connection.add_listener(network_connection_listener)
	    # asking to modify the connection with the received sdp : the listener will get 
	    # the event notifying it that the connection is ready or not with the corresponding SDP
	    # that will be used to send the 200 response
	    connection.modify(javax.media.mscontrol.networkconnection.NetworkConnection.UNKNOWN_YET, nil)
	  end
  
	# actually sending the request out to the sip phone
	#@sip_request.send();

    respond_to do |format|
      if @complaint.save
        flash[:notice] = 'Complaint was successfully created.'
        format.html { redirect_to(@complaint) }
        format.xml  { render :xml => @complaint, :status => :created, :location => @complaint }
      else
        format.html { render :action => "new" }
        format.xml  { render :xml => @complaint.errors, :status => :unprocessable_entity }
      end
    end
  end

  # PUT /complaints/1
  # PUT /complaints/1.xml
  def update
    @complaint = Complaint.find(params[:id])

    respond_to do |format|
      if @complaint.update_attributes(params[:complaint])
        flash[:notice] = 'Complaint was successfully updated.'
        format.html { redirect_to(@complaint) }
        format.xml  { head :ok }
      else
        format.html { render :action => "edit" }
        format.xml  { render :xml => @complaint.errors, :status => :unprocessable_entity }
      end
    end
  end

  # DELETE /complaints/1
  # DELETE /complaints/1.xml
  def destroy
    @complaint = Complaint.find(params[:id])
    @complaint.destroy

    respond_to do |format|
      format.html { redirect_to(complaints_url) }
      format.xml  { head :ok }
    end
  end
end
