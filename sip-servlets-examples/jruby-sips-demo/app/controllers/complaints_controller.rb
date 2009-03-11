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

    respond_to do |format|
      if @complaint.save
	# get the sip factory from the servlet context
    	@sip_factory = $servlet_context.get_attribute('javax.servlet.sip.SipFactory')
     	# create a new sip application session
    	@app_session = request.env['java.servlet_request'].get_session().get_application_session();    
    	# create a new sip servlet request to start a call to the sip phone with from header equals to "sip:my_jruby_app_rocks@mobicents.org" and the to header equals to the sip_uri from the complaint
    	@sip_request = @sip_factory.create_request(@app_session, 'INVITE', 'sip:my_jruby_app_rocks@mobicents.org', @complaint.sip_uri);
    	# actually sending the request out to the sip phone
    	@sip_request.send();

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
