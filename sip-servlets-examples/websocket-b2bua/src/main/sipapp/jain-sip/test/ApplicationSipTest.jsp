<%@ page import="org.mobicents.servlet.sip.SipConnector" %>
<html>
    <head>
        <title>ApplicationSipTest</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script type='text/javascript'>   
            var logger =  undefined; //console;
        </script>	
	<link rel="icon" type="image/png" href="../main/bootstrap/img/telestax-favicon.png">
	<link href="../main/bootstrap/css/bootstrap.min.css" rel="stylesheet"> 
	<link href="../main/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
	<link href="../main/bootstrap/css/docs.css" rel="stylesheet">
	<script src="../main/bootstrap/js/bootstrap.min.js"></script>
        <script src="../main/javascript/gov/nist/core/GenericObject.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/GenericObjectList.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/NameValue.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/NameValueList.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/DuplicateNameValueList.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/HostPort.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/Host.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/Token.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/StringTokenizer.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/LexerCore.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/ParserCore.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/HostNameParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/core/MessageDigestAlgorithm.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/address/GenericURI.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/address/UserInfo.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/address/Authority.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/address/TelephoneNumber.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/address/SipUri.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/address/AddressImpl.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/address/AddressFactoryImpl.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/header/SIPObject.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/SIPHeader.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/SIPHeaderList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ParametersHeader.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/RequestLine.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/UserAgent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ContentLength.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ExtensionHeaderImpl.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Server.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/AddressParametersHeader.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/From.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/To.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Reason.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ReasonList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Protocol.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Via.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Contact.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/MediaRange.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/AuthenticationHeader.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/WWWAuthenticate.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Route.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ProxyAuthenticate.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ProxyAuthorization.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/StatusLine.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Authorization.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Allow.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/RecordRoute.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/MaxForwards.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ContentType.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/TimeStamp.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ContentLength.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ContentDisposition.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/CallIdentifier.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/CallID.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/CSeq.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Supported.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/Expires.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ContactList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ViaList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/WWWAuthenticateList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/RouteList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ProxyAuthenticateList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/ProxyAuthorizationList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/AuthorizationList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/AllowList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/RecordRouteList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/SupportedList.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/header/HeaderFactoryImpl.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/parser/Parser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/Lexer.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/HeaderParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ParametersParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/TokenTypes.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/TokenNames.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/StringMsgParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/AddressParametersParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ChallengeParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/URLParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/AddressParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ToParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/FromParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/CSeqParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ViaParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ContactParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ContentTypeParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ContentLengthParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/AuthorizationParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/WWWAuthenticateParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/CallIDParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/RouteParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/RecordRouteParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ProxyAuthenticateParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ProxyAuthorizationParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/TimeStampParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/UserAgentParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/SupportedParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ServerParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/SubjectParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/MaxForwardsParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ReasonParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/RequestLineParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ExpiresParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/EventParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/StatusLineParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ContentDispositionParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/AllowParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/AllowEventsParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/ParserFactory.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/parser/WSMsgParser.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/message/MessageObject.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/message/ListMap.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/message/SIPMessage.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/message/MessageFactoryImpl.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/message/SIPRequest.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/message/SIPResponse.js" type="text/javascript"></script>
        <script src="../main/javascript/gov/nist/sip/stack/HopImpl.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPTransactionStack.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPTransactionErrorEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/DefaultRouter.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/WSMessageChannel.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/WSMessageProcessor.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPDialog.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPDialogErrorEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPDialogEventListener.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPTransaction.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPClientTransaction.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/stack/SIPServerTransaction.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/Utils.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/EventWrapper.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/EventScanner.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/DialogTerminatedEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/DialogTimeoutEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/TransactionTerminatedEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/RequestEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/TimeoutEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/TransactionTerminatedEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/DefaultAddressResolver.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/ResponseEvent.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/ResponseEventExt.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/DialogFilter.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/ListeningPointImpl.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/NistSipMessageFactoryImpl.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/SipProviderImpl.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/SipStackImpl.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/SipListener.js" type="text/javascript" ></script>
        <script src="../main/javascript/gov/nist/sip/SipFactory.js" type="text/javascript" ></script>
        <script src="./ApplicationSipTest.js" type="text/javascript" ></script>
        <script type='text/javascript'>	
            var applicationSip=null;
            //            var defaultSipWsUrl="ws://10.193.7.134:8080/WebRtcGateway/WS/WebSocketConnector";
            //            var defaultSipDomain="sipasf.fr";
            //            var defaultSipDisplayName="9999";
            //            var defaultSipUserName="9999";
            //            var defaultSipLogin="9999@sipasf.fr";
            //            var defaultSipPassword="1234";
            //            var defaultSipContactPhoneNumber="7777";

               <%
                SipConnector[] sipConnectors = (SipConnector[]) getServletContext().getAttribute(
                		"org.mobicents.servlet.sip.SIP_CONNECTORS");
            	for(int q=0; q<sipConnectors.length; q++) {
            		if(sipConnectors[q].getTransport().equalsIgnoreCase("WS")) {
            			out.println("var defaultSipWsUrl=\"ws://" + sipConnectors[q].getIpAddress() + ":" + sipConnectors[q].getPort() + "\";");
            		}
            	}
            	%>
            var defaultStunServer="";
            var defaultSipDomain="mobicents.com";
            var defaultSipDisplayName="9999";
            var defaultSipUserName="9999";
            var defaultSipLogin="9999@mobicents.com";
            var defaultSipPassword="mobicents";
            var defaultSipContactPhoneNumber="8888";
            
            var localAudioVideoMediaStream=null;
            
            function onLoad()
            {
                console.debug("onLoad");
                document.getElementById("stunServer").value=defaultStunServer;
                document.getElementById("sipWsUrl").value=defaultSipWsUrl;
                document.getElementById("sipDomain").value=defaultSipDomain;
                document.getElementById("sipDisplayName").value=defaultSipDisplayName;
                document.getElementById("sipUserName").value=defaultSipUserName;
                document.getElementById("sipLogin").value=defaultSipLogin;
                document.getElementById("sipPassword").value=defaultSipPassword;
                document.getElementById("sipContactPhoneNumber").value=defaultSipContactPhoneNumber;
                navigator.webkitGetUserMedia({audio:true, video:true},gotLocalAudioVideoStream, gotLocalAudioVideoFailed);
            }

            function onBeforeUnload()
            {
                unRegister();
                for(var i=0;i<5000;i++)
                {
                    console.log("OnBeforeUnLoad()");  
                }     
            }
            
            function gotLocalAudioVideoStream (localStream) {
                localAudioVideoMediaStream=localStream;
                var url = webkitURL.createObjectURL(localStream);
                document.getElementById("localVideoPreview").src=url;
                document.getElementById("localVideoPreview").play();
                showRegisterButton();
            }

            function  gotLocalAudioVideoFailed(error) 
            {
                alert("Failed to get access to local media. Error code was " + error.code + ".");
                hideRegisterButton();
            }	
    
            function register()
            {
                applicationSip=new ApplicationSipTest(document.getElementById("sipWsUrl").value);
                applicationSip.localAudioVideoMediaStream=localAudioVideoMediaStream;
            }

            function unRegister()
            {
                if(applicationSip!=null)
                {
                    applicationSip.unRegister();   
                }
            }

            function call(from,to)
            {   
                applicationSip.call(from,to);
            }


            function bye()
            {
                applicationSip.bye();
            }
                
                
            function hideCallButton()
            {
                var call=document.getElementById("Call");
                call.disabled=true;
            }
            
            function showCallButton()
            {
                var call=document.getElementById("Call");
                call.disabled=false;
            }
            
            function hideByeButton()
            {
                var bye=document.getElementById("Bye");
                bye.disabled=true;
            }
            
            function showByeButton()
            {
                var bye=document.getElementById("Bye");
                bye.disabled=false;
            }
            
            function showUnRegisterButton()
            {
                var unRegister=document.getElementById("UnRegister");
                unRegister.disabled=false;
            }
            
            function showRegisterButton()
            {
                var register=document.getElementById("Register");
                register.disabled=false;
            }
            function hideUnRegisterButton()
            {
                var unRegister=document.getElementById("UnRegister");
                unRegister.disabled=true;
            }
            
            function hideRegisterButton()
            {
                var register=document.getElementById("Register");
                register.disabled=true;
            }
        </script>
        <!--style type='text/css'>
            div {
                border: 0px solid black;
            }

            div#input {
                clear: both;
                width: 36em;
                padding: 4px;
                background-color: #e0e0e0;
                border: 1px solid black;
            }

            div.hidden {
                display: none;
            }

            span.alert {
                font-style: italic;
            }
        </style-->
    </head>
    <body onload="onLoad()" onbeforeunload="onBeforeUnload()">
    <div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner">        
	<div class="container">
          <a class="brand" href="#">Mobicents, Connect the Cans, WebRTC Client,  By </a><a href="http://www.telestax.com"><img style="display: block;" alt="TeleStax" src="../main/bootstrap/img/TeleStax_logo_small.png" /></a>
        </div>
	<!--div class="container">
          <a class="brand" href="#">By</a>    
	  <a href="http://www.telestax.com"><img style="display: block;" alt="TeleStax" src="../main/bootstrap/img/TeleStax_logo_small.png" /></a>
        </div-->
      </div>
    </div>
    <div class="container-fluid">
      	<div class="row-fluid">
        	<div class="span3">
          		<div class="form-horizontal well">
			   <div id="sipAccountSettings" >
	    		   	<div class="nav-header">Registration</div>
			        <div class="nav-header">&nbsp;</div>
			        <div class="control-group" id='stunServerDiv'>
			                <label class="control-label" for="stunServer">STUN server:</label>
			                <input id="stunServer"  type="text" size="40"> 
		                </div>
            			<div class="control-group" id='sipWsUrlDiv'>
			                <label class="control-label">SIP outbound Proxy :</label>
			                <input  id="sipWsUrl"  type="text" size="40"> 
		                </div>
			        <div class="control-group"id='sipDomainDiv'>
                			<label class="control-label">SIP Domain:</label>
			                <input id="sipDomain"  type="text" size="30" value="sipasf.fr"> 
		                </div>           
		                <div class="control-group"id='sipDisplayNameDiv'>
			                <label class="control-label">SIP Display Name:</label>
			                <input id="sipDisplayName"  type="text" size="30" value="laurent"> 
		                </div>
		                <div class="control-group"id='sipUserNameDiv'>
			                <label class="control-label">SIP User Name:</label>
			                <input id="sipUserName"  type="text" size="30" value="9999"> 
            			</div>
		                <div class="control-group"id='sipLoginDiv'>
			                <label class="control-label">SIP Login:</label>
			                <input id="sipLogin"   type="text" size="30" value="9999@sipasf.fr"> 
		                </div>
		                <div class="control-group"id='sipPasswordDiv'>
			                <label class="control-label">SIP Password:</label>
			                <input id="sipPassword"   type="password" size="30" value="1234"> 
		                </div>           
			        <div class="control-group"id ='input'>
			                <input id='Register' class="btn btn-primary" type='submit' name='Register' disabled="disabled" value='Register' onclick = "register();"/>
					&nbsp;&nbsp;&nbsp;&nbsp;
			                <input id='UnRegister' class="btn btn-primary" type='submit' name='UnRegister' value='UnRegister' disabled="disabled" onclick = "unRegister();"/>
				</div>
			</div>
	          </div><!--/.well -->
              </div><!--/span-->
	      <div class="span9">
			<div class="form-horizontal well">
			   <div>
				<div class="nav-header">Communicate</div>		    				      
	      			<p class="lead"> 
       				   <div id='sipContactPhoneNumberDiv'>
					<div id='input'>
			                	Contact To Call: <input id="sipContactPhoneNumber" type="text" class="input-xlarge focused" >					
						&nbsp;<input id='Call' class="btn btn-primary" type='submit' name='Call' value='Call' disabled="disabled" onclick = "call(document.getElementById('sipContactPhoneNumber').value);"/>
						&nbsp;<input id='Bye' class="btn btn-primary" type='submit' name='Bye' value='Bye' disabled="disabled" onclick = "bye();"/>
						<div id='media'>						
						    <div id='over'>
							<video id="localVideoPreview" autoplay="autoplay" style="background-color: #000000; height:150px; width:150px; margin-right: 600px; -webkit-transition-property: opacity;-webkit-transition-duration: 2s;"></video>  
						    </div>
		   				    <div>
							<video id="remoteVideo" width="640px" height="480px" autoplay="autoplay" style="background-color: #000000;margin-top: 10px;-webkit-transition-property: opacity; -webkit-transition-duration: 2s;"></video>  
						    </div>
						</div>											  
 			                 </div>						
       		                   </div>
	      			</p>      		        
			   </div>
		        </div>
     	       </div>			
	</div>
    </div>
    </body>
</html>
