<%@ page import="java.util.HashMap"%>
<%@ page import="org.mobicents.servlet.sip.example.*"%>
<%@ page import="javax.servlet.sip.*"%> 
<html>
<title>Click To Call Demo</title>
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
<a style="font-size: 30px;margin-left:100px"><b>Click-to-Call Sample Application</b></a></td>
</tr></table>
<hr/>
<%
SipSessionsUtil sessionsUtil = (SipSessionsUtil) getServletContext().getAttribute("javax.servlet.sip.SipSessionsUtil");
SipApplicationSession sipApplicationSession = sessionsUtil.getApplicationSessionByKey("registeredUsersMapAppSession", false);
HashMap<String, String> users = null;
if(sipApplicationSession != null) {
	users = (HashMap<String, String>) sipApplicationSession.getAttribute("registeredUsersMap");
}
if(users == null) {
	out.println("<a>No registered users. Please register at least two SIP User Agents.</a><br/>");
} else {
	String[] userArray = new String[0];
	userArray = users.keySet().toArray(userArray);

	 // Create app session and request
    SipApplicationSession appSession = 
    	((ConvergedHttpSession)request.getSession()).getApplicationSession();
    
    CallStatusContainer calls = (CallStatusContainer) appSession.getAttribute("activeCalls");
	
	out.println("<table class=\"calltable\">");
	out.println("<tr><td bgcolor=\"#DDDDDD\">From \\ To</td>");
	for(String col:userArray) out.println("<td class=\"calltable\" bgcolor=\"#EEEEEE\"><b>" + col + "</b></td>");
	out.println("</tr>");
	for(String fromAddr:userArray) {
		out.println("<tr><td bgcolor=\"#EEEEEE\"><b>"+fromAddr +"</b></td>");
		String fromAddrV = users.get(fromAddr);
		for(String toAddr:userArray) {
			if(!toAddr.equals(fromAddr)) {
				String toAddrV = users.get(toAddr);
				String status = calls==null? null:calls.getStatus(fromAddrV, toAddrV);
				if(status == null) status = "FFFFFF"; // This is hex RGB color
				if(status.equals("FFFFFF"))
					out.println("<td class=\"calltable\" bgcolor=\"#" + status 
						+ "\" align=\"center\"><a href=\"call?to="
						+ users.get(toAddr) + "&from="
						+ users.get(fromAddr) +"\"\">call</a></td>");
				else
					out.println("<td class=\"calltable\" bgcolor=\"#" + status 
							+ "\" align=\"center\"><a>call in progress </a><a href=\"call?to="
							+ users.get(toAddr) + "&bye=true&from="
							+ users.get(fromAddr) +"\"\">end</a></td>");
				
			} else {
				out.println("<td class=\"calltable\" ></td>");
			}
		}
		out.println("<tr>");
	}
	out.println("</table>");
	out.println("<br/><a href=\"call?bye=all\">Close all calls</a>");
}

%>
</body>
</html>