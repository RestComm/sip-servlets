<%@ page import="java.util.*"%>
<%@ page import="org.mobicents.servlet.sip.example.*"%>
<%@ page import="com.facebook.api.*"%>
<%@ page import="org.w3c.dom.*"%>
<%@ page import="javax.servlet.*"%>
<%@ page import="java.net.*"%>
<%

String apikey = getServletContext().getInitParameter("apikey");//"1ef4389e2341209d5f42e6f39f319fe2";
String secret = getServletContext().getInitParameter("secret");//"49c4b71e3df42f6c1d2c661c5a319aa3";

String authToken = request.getParameter("auth_token");
String updateNumber = request.getParameter("updateNumber");
String pageHtml = "";
String debugString = "";

// If we dont have auth key in the request parameters or the session, then go to the facebook auth page
// to get auth key.
if(authToken == null && request.getSession().getAttribute("fbSession") == null) {
	String inCanvas = request.getParameter("fb_sig_in_iframe");
	if("1".equals(inCanvas)) {
		debugString += "Redirect to fb login\n";
		%><script>top.location="http://www.facebook.com/login.php?api_key=<%out.print(apikey);%>&v=1.0&cavnas=true";</script>
			<%
				} else {
					response.sendRedirect("http://www.facebook.com/login.php?api_key=" + apikey + "&v=1.0;");
				}
				return;
			}

			// Initialize the facebook client
			FacebookXmlRestClient client = null;
			String fbSession = (String) request.getSession().getAttribute("fbSession");
			try {
				client = new FacebookXmlRestClient(apikey, secret, fbSession);
				client.setIsDesktop(false);
			} catch (Exception e) {
				client = new FacebookXmlRestClient(apikey, secret);
				client.setIsDesktop(false);
			}

			// Get a facebook authenticated session
			fbSession = client.auth_getSession(authToken);

			// Store it in the web session
			request.getSession().setAttribute("fbSession", fbSession);

			// Get the current user ID
			String cid = Integer.toString(client.users_getLoggedInUser());

			// Get the number from the profile. Update the profile number if the number in the request parameter is new.
			String myPhone = null;
			if(updateNumber != null) {
				myPhone = updateNumber;
				FacebookUtils.setCurrentUserPhone(updateNumber, client);
			}
			else myPhone = FacebookUtils.getCurrentUserPhone(client);
			%>
<html>
<title>Facebook click2call</title>
<head>
<style>
body { font-family: Tahoma, Geneva, 'Trebuchet MS', Arial, Helvetica, sans-serif; }

.calltable, a {
	border: 1px solid #D4E0EE;
	border-collapse: collapse;
	font-family: "Trebuchet MS", Arial, sans-serif;
	color: #555;
}

.headtable {
	border: none;
	width: 100%;
}

td.calltable, tr {
	padding: 4px;
border: 1px dotted black;
}
</style>
</head>
<body>
<table class="headtable"><tr>
<td style="vertical-align:middle"><img alt="Mobicents" src="images/mobicents.logo.jpeg"/>
<a style="font-size: 30px;margin-left:100px"><b>Click2Call Facebook</b></a></td>
</tr></table>
<br/>
<% if(!client.users_isAppAdded()) {%>
<a href="http://www.facebook.com/add.php?api_key=<%out.print(apikey);%>" target="_top">Install</a>
<% }%>
<hr/>
<p style="font-size: 11px;"> To use this application you need an access code. If you think you should have access code send email to vladimir.ralev at gmail.com.
You should start by entering you phone number in the "Your phone number" text box. The click "Update" and it will be stored in you facebook 
account for later use, it can be updated at any time by repeating the steps. Then enter the second phone number in the other text box. When 
you click "Dial" a call will be established between the two phones, It may take 20-30 seconds before you receive the the first call and another 20-30
for the other phone to start ringing. </p><br/><br/>
<form>
Access code <input id="accessCode" value="" type="password"/><br/><br/><br/>
Your phone number is <input id="currentUserPhone" value="<%out.print(myPhone);%>"/>
<a href="#" onClick="javascript:updatePhone();">Update</a>
</form>
<%
try {
	%>
	<script>
	String.prototype.replaceAll = function(pcFrom, pcTo){
      	var i = this.indexOf(pcFrom);
      	var c = this;
       
      	while (i > -1){
      		c = c.replace(pcFrom, pcTo);
      		i = c.indexOf(pcFrom);
      	}
      	return c;
    }
    
    /* This function makes an http request to the servlet which initiates the calls */
	function dial() {
		var remote_phone = document.getElementById('phoneNumber').value;
		var my_phone = document.getElementById('currentUserPhone').value;
		var access = document.getElementById('accessCode').value; 
		self.location = "http://localhost:8080/click-to-call-servlet-1.0/call?from="
			+ fixNumber(remote_phone)+"&to="
			+ fixNumber(my_phone)+"&access="
			+ access;
	}
	
	/* This function stores the phone number the the user's application profile */
	function updatePhone() {
		var my_phone = document.getElementById('currentUserPhone').value;
		var index = self.location.href.indexOf("updateNumber");
		if(index<0)
		    if(self.location.href.indexOf("?")<0)
		    	self.location += "?updateNumber="+my_phone;
		    else
			self.location += "&updateNumber="+my_phone;
		else {
		    var newLoc = self.location.href;
		    newLoc = newLoc.substring(0,index)+"updateNumber="+my_phone;
		    self.location = newLoc;
		}
	}
	
	/* This function removes all unneeded characters from the phone string and only leaves the numbers */
	function fixNumber(numberString) {
		var fixedNumber = "";
		var number = numberString.replaceAll('.',' ').replaceAll('+',' ').replaceAll('(',' ').replaceAll(')',' ').replaceAll('-',' ');
		for(var q=0; q<number.length; q++) if(number.charAt(q)!=' ') fixedNumber += number.charAt(q);
		return fixedNumber;
	}
	
 

	</script>
	<form>
		Enter phone number to dial:
		<input id="phoneNumber" type="text" size="33"><br/>
	</form>
		<a href="#" id="dialog_body"
	onclick="javascript:dial();">Dial</a>
	<%
	
} catch (Exception e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
%>
</body>
</html>