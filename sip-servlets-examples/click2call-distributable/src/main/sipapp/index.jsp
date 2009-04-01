<%@ page import="org.mobicents.servlet.sip.example.*"%>
<%@ page import="javax.servlet.sip.*"%>
<html>
<head>
<title>Sample "Hello, World" Application</title>
</head>
<body>

<p><font size="6">Simple Click To Call Demo</font></p>
<form method="GET" action="call">

	<p>To:</p>
	<p><input type="text" name="to" size="20" value="sip:to@127.0.0.1:5050"></p>
	<p>From:</p>
	<p><input type="text" name="from" size="20" value="sip:from@127.0.0.1:5060"></p>
	<p><input type="submit" value="Call" name="B1"><input type="reset" value="Reset" name="B2"></p>
</form>
<p>&nbsp;</p>
<hr/>
<a>Debug</a>
<%
SipApplicationSession appSession = 
        	((ConvergedHttpSession)request.getSession()).getApplicationSession();
out.println("<br/>appSession.setFromSipServletUA1=" + appSession.getAttribute("setFromSipServletUA1"));
out.println("<br/>appSession.setFromSipServletUA2=" + appSession.getAttribute("setFromSipServletUA2"));
out.println("<br/>appSession.setFromHttpServlet=" + appSession.getAttribute("setFromHttpServlet"));
out.println("<br/>JSESSIONID=" + request.getSession().getId());
out.println("<br/>APPSESSIONID=" + appSession.getId());
%>

</body>
</html>
