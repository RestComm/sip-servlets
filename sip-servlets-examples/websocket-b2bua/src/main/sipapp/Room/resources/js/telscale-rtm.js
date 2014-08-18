/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

// --- General Utility Functions ----------------------------------------------
/**
 * Shortens a string to a specific size.
 * 
 * @param str
 *            the string to be shortened
 * @param begin
 *            the amount of chars to keep in the start
 * @param end
 *            the amount of chars to keep in the end
 * @return the shortened string, in form <start>..<end>
 */
function modal_alert(message) {
	$("#modal_message").html(message);
	$('#messageModal').modal();
}

function show_desktop_notification(message) {
	if (window.webkitNotifications
			&& window.webkitNotifications.checkPermission() == 0) {
		var thumb = "resources/images/telestax-logo-2012-web-square.png";
		var title = "TelScale RTM WebRTC Client";
		var popup = window.webkitNotifications.createNotification(thumb, title,
				message);
		// Show the popup
		popup.show();
		setTimeout(function() {
			popup.cancel();
		}, '10000');
	}
}

function enableCommunicatePanel(localAudioVideoMediaStream) {
	var displayName = getSipDisplayNameTextInputValue();
	//$('#left-sidebar').load('modules/contacts.html');	
	$('#registration').hide();
	//$('#left-sidebar').show('slow');
	$('#unregister').show();
	$('#user').show('slow');
	$('#username').text(displayName);
	$('#room').show('slow');
}

function disableCommunicatePanel() {
	closeChatBoxes();
	//disableCallButton();
	//disableEndCallButton();
	enableConnectButton();
	stopLocalVideo();
	hideLocalVideo();	
	$('#unregister').hide();
	$('#user').hide();
	$('#room').hide()	
	//$('#left-sidebar').hide();		
	$('#registration').show('slow');
}

function enableConnectButton() {
	document.getElementById("Register").disabled = false;
}

function disableConnectButton() {
	document.getElementById("Register").disabled = true;
}

function enableDisconnectButton() {
	document.getElementById("UnRegister").disabled = false;
}

function disableDisconnectButton() {
	document.getElementById("UnRegister").disabled = true;
}

function enableCallButton() {
	//document.getElementById("Call").disabled = false;
}

function disableCallButton() {
	//document.getElementById("Call").disabled = true;
}

function enableEndCallButton() {
	//document.getElementById("Bye").disabled = false;
}

function disableEndCallButton() {
	//document.getElementById("Bye").disabled = true;
}

function enableCallButton(contact) {
	//document.getElementById("Call").disabled = false;
	$("#call"+contact).show();	
}

function disableCallButton(contact) {
	//document.getElementById("Call").disabled = true;
	$("#call"+contact).hide();
}

function enableEndCallButton(contact) {
	//document.getElementById("Bye").disabled = false;
	$("#endCall"+contact).show();
	$("#callStatus"+contact).show();
}

function disableEndCallButton(contact) {
	//document.getElementById("Bye").disabled = true;
	$("#endCall"+contact).hide();
	$("#callStatus"+contact).hide();
}

function showLocalVideo() {
	document.getElementById("localVideo").style.visibility = "visible"
}

function hideLocalVideo() {
	document.getElementById("localVideo").style.visibility = "hidden"
}

function toggleRemoteVideo(contact) {
	if(document.getElementById("remoteVideo"+contact) == undefined) 
		return;
	$("#remoteVideo"+contact).toggle("slide");
}

function showRemoteVideo(contact) {
	if(document.getElementById("remoteVideo"+contact) == undefined) 
		return;
	$("#chatBoxContent"+contact).removeAttr("style");
	$("#chatBoxContent"+contact).attr("style", "height:35%; min-height:35%");
	document.getElementById("remoteVideo"+contact).style.visibility = "visible";
}

function hideRemoteVideo(contact) {
	if(document.getElementById("remoteVideo"+contact) == undefined) 
		return;
	document.getElementById("remoteVideo"+contact).style.visibility = "hidden";
	$("#chatBoxContent"+contact).removeAttr("style");
	$("#chatBoxContent"+contact).attr("style", "height:90%; min-height:90%");
}

function playLocalVideo(localMediaStream) {
	if (typeof navigator.mozGetUserMedia != 'undefined') {
		document.getElementById("localVideo").mozSrcObject = localMediaStream;
	} else {
		var vendorURL = window.URL || window.webkitURL;
		document.getElementById("localVideo").src = vendorURL
				.createObjectURL(localMediaStream);
	} 
	document.getElementById("localVideo").muted = true;
	document.getElementById("localVideo").play();
}

function stopLocalVideo() {	
	document.getElementById("localVideo").pause();
	if (typeof navigator.mozGetUserMedia  != 'undefined') {
		document.getElementById("localVideo").mozSrcObject = undefined;
	} else {
		document.getElementById("localVideo").src = undefined;
	}	
}

function playRemoteVideo(contact,remoteMediaStream) {
	if(document.getElementById("remoteVideo"+contact) == undefined) 
		return;
	
	if (typeof navigator.mozGetUserMedia != 'undefined') {
		document.getElementById("remoteVideo"+contact).mozSrcObject = remoteMediaStream;
	} else {
		var vendorURL = window.URL || window.webkitURL;
		document.getElementById("remoteVideo"+contact).src = vendorURL
				.createObjectURL(remoteMediaStream);
	} 

	document.getElementById("remoteVideo"+contact).play();
	$('#remoteVideo'+contact).show();
}

function stopRemoteVideo(contact) {
	if(document.getElementById("remoteVideo"+contact) == undefined) 
		return;
	$('#remoteVideo'+contact).hide();
	document.getElementById("remoteVideo"+contact).pause();
	if (typeof navigator.mozGetUserMedia != 'undefined') {
		document.getElementById("remoteVideo"+contact).mozSrcObject = undefined;
	} else {
		document.getElementById("remoteVideo"+contact).src = undefined;
	} 
}

function toggleFullScreen(contact) {
	var videoElement = document.getElementById("remoteVideo"+contact);
	if (!document.mozFullScreen && !videoElement.webkitDisplayingFullscreen) {
    		/*
    		 * if (videoElement.mozRequestFullScreen) {
    		 * videoElement.mozRequestFullScreen(); } else {
    		 */
    		videoElement.webkitEnterFullScreen(Element.ALLOW_KEYBOARD_INPUT);
    		/* } */
	} else {
    		/*
    		 * if (document.mozCancelFullScreen) { document.mozCancelFullScreen(); }
    		 * else {
    		 */
    		videoElement.webkitExitFullscreen();
    		/* } */
	}
}    

/*
 * function playRemoteAudio(remoteMediaStream) { if(typeof
 * navigator.webkitGetUserMedia != 'undefined') {
 * document.getElementById("remoteAudio").src=webkitURL.createObjectURL(remoteMediaStream); }
 * else if(typeof navigator.mozGetUserMedia != 'undefined') {
 * document.getElementById("remoteAudio").mozSrcObject=remoteMediaStream; }
 * document.getElementById("remoteAudio").play(); }
 * 
 * function stopRemoteAudio() { document.getElementById("remoteAudio").pause();
 * if(typeof navigator.webkitGetUserMedia != 'undefined') {
 * document.getElementById("remoteAudio").src=undefined; } else if(typeof
 * navigator.mozGetUserMedia != 'undefined') {
 * document.getElementById("remoteAudio").mozSrcObject=undefined; } }
 */

/*
 * function playLocalVideo(localMediaStream) { if(typeof
 * navigator.webkitGetUserMedia != undefined) {
 * document.getElementById("localVideoPreview").src=webkitURL.createObjectURL(localMediaStream); }
 * else if(typeof navigator.mozGetUserMedia != undefined) {
 * document.getElementById("localVideoPreview").mozSrcObject=localMediaStream; }
 * document.getElementById("localVideoPreview").play(); }
 * 
 * 
 * function stopLocalVideo() {
 * document.getElementById("localVideoPreview").pause(); if(typeof
 * navigator.webkitGetUserMedia != undefined) {
 * document.getElementById("localVideoPreview").src=null; } else if(typeof
 * navigator.mozGetUserMedia != undefined) {
 * document.getElementById("localVideoPreview").mozSrcObject=null; } }
 * 
 * function playRemoteVideo(remoteMediaStream) { if(typeof
 * navigator.webkitGetUserMedia != undefined) {
 * document.getElementById("remoteVideo").src=webkitURL.createObjectURL(remoteMediaStream); }
 * else if(typeof navigator.mozGetUserMedia != undefined) {
 * document.getElementById("remoteVideo").mozSrcObject=remoteMediaStream; }
 * document.getElementById("remoteVideo").play(); }
 * 
 * 
 * function stopRemoteVideo() { document.getElementById("remoteVideo").pause();
 * if(typeof navigator.webkitGetUserMedia != undefined) {
 * document.getElementById("remoteVideo").src=undefined; } else if(typeof
 * navigator.mozGetUserMedia != undefined) {
 * document.getElementById("remoteVideo").mozSrcObject=undefined; } }
 */

function playChatNotification() {
	document.getElementById("chat_notification").play();
}

function playRinging(remoteMediaStream) {
	document.getElementById("ringing").play();
}

function stopRinging() {
	document.getElementById("ringing").pause();
}

function setStunServerTextInputValue(value) {
	document.getElementById("stunServer").value = value;
}

function setTurnServerTextInputValue(value) {
	document.getElementById("turnServer").value = value;
}

function setTurnLoginTextInputValue(value) {
	document.getElementById("turnLogin").value = value;
}

function setTurnPasswordTextInputValue(value) {
	document.getElementById("turnPassword").value = value;
}

function setSipOutboundProxyTextInputValue(value) {
	document.getElementById("sipOutboundProxy").value = value;
}

function setSipDomainTextInputValue(value) {
	document.getElementById("sipDomain").value = value;
}

function setSipDisplayNameTextInputValue(value) {
	document.getElementById("sipDisplayName").value = value;
}

function setSipUserNameTextInputValue(value) {
	document.getElementById("sipUserName").value = value;
}

function setSipLoginTextInputValue(value) {
	document.getElementById("sipLogin").value = value;
}

function setSipPasswordTextInputValue(value) {
	document.getElementById("sipPassword").value = value;
}

function setSipContactTextInputValue(value) {
	document.getElementById("sipContact").value = value;
}

function getTextInputValue(elementId) {
	var value = document.getElementById(elementId).value;
	if (value == "undefined")
		return undefined;
	else if (value == "")
		return undefined;
	else
		return value
}

function getStunServerTextInputValue() {
	return getTextInputValue("stunServer")
}

function getTurnServerTextInputValue() {
	return getTextInputValue("turnServer")
}

function getTurnLoginTextInputValue() {
	return getTextInputValue("turnLogin")
}

function getTurnPasswordTextInputValue() {
	return getTextInputValue("turnPassword")
}

function getSipOutboundProxyTextInputValue() {
	return getTextInputValue("sipOutboundProxy");
}

function getSipDomainTextInputValue() {
	return getTextInputValue("sipDomain");
}

function getSipDisplayNameTextInputValue() {
	return getTextInputValue("sipDisplayName");
}

function getSipUserNameTextInputValue() {
	return getTextInputValue("sipUserName");
}

function getSipLoginTextInputValue() {
	return getTextInputValue("sipLogin");
}

function getSipPasswordTextInputValue() {
	return getTextInputValue("sipPassword");
}

function getSipContactTextInputValue() {
	return getTextInputValue("sipContact");
}

function getAudioCodecsFilterTextInputValue() {
	return getTextInputValue("audioCodecsFilter")
}

function getVideoCodecsFilterTextInputValue() {
	return getTextInputValue("videoCodecsFilter")
}

function getLocalVideoFormatTextInputValue() {
	return getTextInputValue("localVideoFormat")
}

function setAudioCodecsFilterTextInputValue(value) {
	document.getElementById("audioCodecsFilter").value = value;
}

function setVideoCodecsFilterTextInputValue(value) {
	document.getElementById("videoCodecsFilter").value = value;
}

function setLocalVideoFormatTextInputValue(value) {
	document.getElementById("localVideoFormat").value = value;
}
