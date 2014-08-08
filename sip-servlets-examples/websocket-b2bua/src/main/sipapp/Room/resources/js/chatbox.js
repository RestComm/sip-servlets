(function($) {
    $.widget("ui.chatbox", {
        options: {
            id: null, //id for the DOM element
            title: null, // title of the chatbox
            user: null, // can be anything associated with this chatbox
            hidden: false,
            offset: 20, // relative to right edge of the browser window
	    lastMessageSentFromPeer: null,
	    lastMessageDate: null,
            width: 300, // width of the chatbox
            messageSent: function(id, user, msg) {
                // override this
                this.boxManager.addMsg(user.first_name, msg, true);
            },            
            boxClosed: function(id) {
            }, // called when the close icon is clicked
            boxManager: {
                // thanks to the widget factory facility
                // similar to http://alexsexton.com/?p=51
                init: function(elem) {
                    this.elem = elem;
                },
                linkify: function(msg) {
                    // http://, https://, ftp://
                    var urlPattern = /\b(?:https?|ftp):\/\/[a-z0-9-+&@#\/%?=~_|!:,.;]*[a-z0-9-+&@#\/%=~_|]/gim;

                    // www. sans http:// or https://
                    var pseudoUrlPattern = /(^|[^\/])(www\.[\S]+(\b|$))/gim;

                    // Email addresses *** here I've changed the expression ***
                    var emailAddressPattern = /(([a-zA-Z0-9_\-\.]+)@[a-zA-Z_]+?(?:\.[a-zA-Z]{2,6}))+/gim;

                    return msg
                        .replace(urlPattern, '<a target="_blank" href="$&">$&</a>')
                        .replace(pseudoUrlPattern, '$1<a target="_blank" href="http://$2">$2</a>')
                        .replace(emailAddressPattern, '<a target="_blank" href="mailto:$1">$1</a>');
                },
                emotify: function(text) {
                	  var emoticons = {                	    
                	    ':D'  : 'icon_biggrin.gif',
                	    '%)'  : 'icon_confused.gif', '%-)' : 'icon_confused.gif',
                	    '|-O' : 'icon_cool.gif', '|;-)': 'icon_cool.gif',
                	    ':\'-(': 'icon_cry.gif', ':\'(': 'icon_cry.gif',
                	    ':P': 'icon_eek.gif', ':-P': 'icon_eek.gif',
                	    '>:)': 'icon_evil.gif', '>;)': 'icon_evil.gif', '>:-)': 'icon_evil.gif',
                	    ':-###..': 'icon_mrgreen.gif', ':###..': 'icon_mrgreen.gif',
                	    ':-||': 'icon_redface.gif', ' :@': 'icon_redface.gif',
                	    ':-|' : 'icon_neutral.gif',
                	    ':-(' : 'icon_sad.gif', ':('  : 'icon_sad.gif',
                	    ':-)' : 'icon_smile.gif', ':)'  : 'icon_smile.gif',
                	    ':-O' : 'icon_surprised.gif', ':O'  : 'icon_surprised.gif',
                	    ';)' : 'icon_wink.gif', ';-)'  : 'icon_wink.gif',
                	    ':*' : 'icon_kiss.gif', ':-*'  : 'icon_kiss.gif',
                	    '<3' : 'icon_heart.gif'
                	    
                	  }, url = "./resources/images/emoticons/", patterns = [],
                	     metachars = /[[\]{}()*+?.\\|^$\-,&#\s]/g;

                	  // build a regex pattern for each defined property
                	  for (var i in emoticons) {
                	    if (emoticons.hasOwnProperty(i)){ // escape metacharacters
                	      patterns.push('('+i.replace(metachars, "\\$&")+')');
                	    }
                	  }

                	  // build the regular expression and replace
                	  return text.replace(new RegExp(patterns.join('|'),'g'), function (match) {
                	    return typeof emoticons[match] != 'undefined' ?
                	           '<img src="'+url+emoticons[match]+'"/>' :
                	           match;
                	  });
                },
                addMsg: function(peer, msg, messageFromPeer) {
                    var self = this;
                    var box = self.elem.uiChatboxLog;
                    var e = document.createElement('div');                    

                    var systemMessage = false;                    
		    var appendMessage = false;

                    msg = self.linkify(msg);
                    msg = self.emotify(msg);                    
		   
   		    var contactImg = document.createElement("div");
		    contactImg.innerHTML = '<img class="img-circle" src="resources/images/userPic_generic.png"/> ';

		   if (peer) {    
			if(peer == self.elem.options.title) {
				if(self.elem.options.lastMessageSentFromPeer == false) {
					appendMessage = true;
				} else {
					self.elem.options.lastMessageDate = Date.now();
				}
				self.elem.options.lastMessageSentFromPeer = false;			
			} else {				
				if(self.elem.options.lastMessageSentFromPeer == true) {
					appendMessage = true;
				} else {
					self.elem.options.lastMessageDate = Date.now();
				}
				self.elem.options.lastMessageSentFromPeer = true;
			}
			var msgElement = document.createElement("div"); 
			msgElement.innerHTML = msg ; 
			if(appendMessage == false) {
				e.appendChild(contactImg);
			}
			//e.appendChild(msgElement);
                    } else {
                        systemMessage = true;
			var msgElement = document.createElement("i");
			msgElement.innerHTML = msg ;
			//e.appendChild(msgElement);
                    }    		    
		    if(appendMessage == false) {
			    var messageDiv = document.createElement('div');
			    var timestampDiv = document.createElement('div');
			    var contactSpan = document.createElement("span");                
			    var timestampSpan = document.createElement("span");
			    if(self.elem.options.lastMessageSentFromPeer) {
			        $(msgElement).attr("id", "message-"+peer+"-"+self.elem.options.lastMessageDate);
			    } else {
			        $(msgElement).attr("id", "message-"+self.elem.options.title+"-"+self.elem.options.lastMessageDate);
			    }			   
			    box.append(e);
	                    $(e).hide();
			    contactSpan.innerHTML = peer + " - ";
			    $(timestampSpan).attr("data-livestamp", Date.now()/1000);
		 	    $(timestampDiv).addClass("ui-chatbox-date");
			    timestampDiv.appendChild(contactSpan);
			    timestampDiv.appendChild(timestampSpan);
			    messageDiv.appendChild(msgElement);
			    messageDiv.appendChild(timestampDiv);
			    e.appendChild(messageDiv);
			    if(messageFromPeer) {
				$(contactImg).addClass("ui-chatbox-avatar-peer");                 
			        $(messageDiv).addClass("ui-chatbox-msg-peer other");				
				$(e).addClass("ui-chatbox-discussion-peer");
			    } else {
				$(contactImg).addClass("ui-chatbox-avatar");                 
				$(messageDiv).addClass("ui-chatbox-msg self");
				$(e).addClass("ui-chatbox-discussion");
			    }
		            //$(e).css("maxWidth", $(box).width());
		            $(e).fadeIn();
		            self._scrollToBottom();
		    } else {
			if(self.elem.options.lastMessageSentFromPeer) {
			   $("#message-"+peer+"-"+self.elem.options.lastMessageDate).html($("#message-"+peer+"-"+self.elem.options.lastMessageDate).html() + "<br/>" + msg);
			} else {
			   $("#message-"+self.elem.options.title+"-"+self.elem.options.lastMessageDate).html($("#message-"+self.elem.options.title+"-"+self.elem.options.lastMessageDate).html() + "<br/>" +  msg);
			}
		    }	
		    

                    if (!self.elem.uiChatboxTitlebar.hasClass("ui-state-focus")
                        && !self.highlightLock) {
                        self.highlightLock = true;
                        self.highlightBox();
                    }
                },
                close: function() {
                    var self = this;
                    self.elem.uiChatbox.hide();
//                    self.options.boxClosed(self.options.id);
                },
                highlightBox: function() {
                    /*var self = this;                                       
                    self.highlightLock = false;
		    $("#contact-" + this.activeChatBox).removeAttr("style");
   		    $("#contact-" + this.activeChatBox).attr("style", "color: grey; cursor: pointer;");*/
                },
                toggleBox: function() {
                    this.elem.uiChatbox.toggle();
                },
                _scrollToBottom: function() {
                    var box = this.elem.uiChatboxLog;
                    box.scrollTop(box.get(0).scrollHeight);
                }
            }
        },
        toggleContent: function(event) {
            this.uiChatboxContent.toggle();
            if (this.uiChatboxContent.is(":visible")) {
                this.uiChatboxInputBox.focus();
            }
        },
        close: function(event) {        	
        	this.uiChatbox.hide();
//            this.options.boxClosed(this.options.id);
        },
        widget: function() {
            return this.uiChatbox
        },
        _create: function() {
            var self = this,
            options = self.options,
            title = options.title || "No Title",
            // chatbox
            uiChatbox = (self.uiChatbox = $('<div id=\"chat' + self.options.id + '\"></div>'))
                .appendTo(document.getElementById('chat'))
                .addClass('ui-widget ' +
                          'ui-corner-top ' +
                          'ui-chatbox'
                         )
                .attr('outline', 0)
                .focusin(function() {
                    // ui-state-highlight is not really helpful here
                    //self.uiChatbox.removeClass('ui-state-highlight');
                    self.uiChatboxTitlebar.addClass('ui-state-focus');
                })
                .focusout(function() {
                    self.uiChatboxTitlebar.removeClass('ui-state-focus');
                }),
	    // Media
	    uiChatboxConf = (self.uiChatboxConf = $(
		'<div id=\'media\'>' +				            
			'<div id=\'resizable\'>' +
				'<video id="remoteVideo' + title + '" onDblClick="toggleFullScreen(' + title + ');" width="640" height="400" autoplay="autoplay" style="margin-top: 10px;-webkit-transition-property: opacity; -webkit-transition-duration: 2s; display:block; margin: 0 auto;" controls></video>' +
			'</div>' +
		'</div>'))
                    .addClass('ui-widget-content ' +
                              'ui-chatbox-content '
                             )
                    .appendTo(uiChatbox),
            // titlebar
            uiChatboxTitlebar = (self.uiChatboxTitlebar = $('<div></div>'))                
//                .click(function(event) {
//                    self.toggleContent(event);
//                })
                .appendTo(uiChatbox),
                
            /*uiChatboxTitlebarClose = (self.uiChatboxTitlebarClose = $('<a href="#"></a>'))
                .addClass('ui-corner-all ' +
                          'ui-chatbox-icon '
                         )
                .attr('role', 'button')
                .hover(function() { uiChatboxTitlebarClose.addClass('ui-state-hover'); },
                       function() { uiChatboxTitlebarClose.removeClass('ui-state-hover'); })
                .click(function(event) {
                    self.close(event);
                    return false;
                })
                .appendTo(uiChatboxTitlebar),
            uiChatboxTitlebarCloseText = $('<span></span>')
                .addClass('ui-icon ' +
                          'icon-remove')
                .text('  ')
//                .text('close')
                .appendTo(uiChatboxTitlebarClose),
            uiChatboxTitlebarMinimize = (self.uiChatboxTitlebarMinimize = $('<a href="#"></a>'))
                .addClass('ui-corner-all ' +
                          'ui-chatbox-icon'
                         )
                .attr('role', 'button')
                .hover(function() { uiChatboxTitlebarMinimize.addClass('ui-state-hover'); },
                       function() { uiChatboxTitlebarMinimize.removeClass('ui-state-hover'); })
                .click(function(event) {
                    self.toggleContent(event);
                    return false;
                })
                .appendTo(uiChatboxTitlebar),
            uiChatboxTitlebarMinimizeText = $('<span></span>')
                .addClass('ui-icon ' +
                          'icon-minus')
                 .text('  ')
//                .text('minimize')
                .appendTo(uiChatboxTitlebarMinimize),*/             
            // content
            uiChatboxContent = (self.uiChatboxContent = $('<div id="chatBoxContent'+ title + '" style="height:90%; min-height:90%"></div>'))
                .addClass('ui-widget-content ' +
                          'ui-chatbox-content '
                         )
                .appendTo(uiChatbox),
            uiChatboxLog = (self.uiChatboxLog = self.element)
                .addClass('ui-widget-content ' +
                          'ui-chatbox-log'
                         )
                .appendTo(uiChatboxContent),
	    //
// incoming call
             /*uiChatboxIncomingCall = (self.uiChatboxIncomingCall = $('<div id="incomingCall' + title + '" style="border-top-color:black"></div>'))
                    .addClass('ui-widget-incoming-call ' +
                              'ui-chatbox-incoming-call '
                             )
                    .appendTo(uiChatboxContent),
             uiChatboxAnswerReject = (self.uiChatboxIncomingCall = $('<table style=";width:100%" border="0"></table>'))
                    .html('<tr><td style="text-align:center;width:50%;background-color:green" onclick="mobicentsWebRTCPhoneController.onClickAcceptCallButtonViewEventHandler()"><a id="acceptCall" href="#"><img src="resources/images/phone_pickup.png" width="25" height="25" style="font-size: 1.2em"/></a></td><td style="text-align:center;width:50%;background-color:red" onclick="mobicentsWebRTCPhoneController.onClickRejectCallButtonViewEventHandler()"><a id="rejectCall" href="#"><img src="resources/images/phone_hangup.png" width="25" height="25" style="font-size: 1.2em"/></a></td></tr>')
                    .appendTo(uiChatboxIncomingCall),*/
	      uiChatboxIncomingCall = (self.uiChatboxIncomingCall = $('<span id="incomingCall' + title + '"></span>'))
                .html('&nbsp;<a id="acceptCall" onclick="mobicentsWebRTCPhoneController.onClickAcceptCallButtonViewEventHandler()" href="#"><img src="resources/images/video-camera-icon-green.png" width="25" height="25" style="font-size: 1.2em"/></a>&nbsp;<a id="rejectCall" onclick="mobicentsWebRTCPhoneController.onClickRejectCallButtonViewEventHandler()" href="#"><img src="resources/images/video-camera-icon-red.png" width="25" height="25" style="font-size: 1.2em"/></a>&nbsp;')
                .appendTo(uiChatboxContent),	   

	    uiChatboxTitle = (self.uiChatboxTitle = $('<span id="commActions' + title + '"></span>'))
                .html('&nbsp;<a id="call'+title+'" href="#" onclick="mobicentsWebRTCPhoneController.onClickCallButtonViewEventHandler(\'' + title + '\')"><i class="icon-facetime-video" style="font-size: 1.2em;"/></a><a id="endCall' + title + '" href="#" onclick="mobicentsWebRTCPhoneController.onClickEndCallButtonViewEventHandler()"><span class="icon-stack"><i class="icon-facetime-video"></i><i class="icon-ban-circle icon-stack-base text-error"></i></span></a>&nbsp;<a id="shareFile' + title + '" href="#" onclick="$(\'#fileShareModal\').modal()"><i class="icon-paper-clip"></i></a>&nbsp;')
                .appendTo(uiChatboxContent),
	   uiChatboxInput = (self.uiChatboxInput = $('<div></div>'))
                .addClass('ui-widget-content ' +
                          'ui-chatbox-input'
                         )
                .click(function(event) {
                    // anything?
                })
                .appendTo(uiChatboxContent),
//
            uiChatboxInputBox = (self.uiChatboxInputBox = $('<textarea rows="1" placeholder="Send a Message..."></textarea>'))
                .addClass('ui-widget-content ' +
                          'ui-chatbox-input-box ' +
                          'ui-corner-all'
                         )
                .appendTo(uiChatboxInput)
                .keydown(function(event) {
                    if (event.keyCode && event.keyCode == $.ui.keyCode.ENTER) {
                        msg = $.trim($(this).val());
                        if (msg.length > 0) {
//                            self.options.messageSent(self.options.id, self.options.user, msg);
                        	self.options.boxManager.addMsg(mobicentsWebRTCPhoneController.webRTCommClientConfiguration.sip.sipUserName, msg, false);
                        	mobicentsWebRTCPhoneController.onClickSendMessageButtonViewEventHandler(self.options.title, msg);
                        }
                        $(this).val('');
                        return false;
                    }
                })
                .focusin(function() {
                    uiChatboxInputBox.addClass('ui-chatbox-input-focus');
                    var box = $(this).parent().prev();
                    box.scrollTop(box.get(0).scrollHeight);
                })
                .focusout(function() {
                    uiChatboxInputBox.removeClass('ui-chatbox-input-focus');
                });

            // disable selection
            uiChatboxTitlebar.find('*').add(uiChatboxTitlebar).disableSelection();

            // switch focus to input box when whatever clicked
            uiChatboxContent.children().click(function() {
                // click on any children, set focus on input box
                self.uiChatboxInputBox.focus();
            });

            //self._setWidth(self.options.width);
            //self._position(self.options.offset);

            self.options.boxManager.init(self);

            if (!self.options.hidden) {
                uiChatbox.show();
            }
        },
        _setOption: function(option, value) {
            if (value != null) {
                switch (option) {
                case "hidden":
                    if (value)
                        this.uiChatbox.hide();
                    else
                        this.uiChatbox.show();
                    break;
                /*case "offset":
                    this._position(value);
                    break;
                case "width":
                    this._setWidth(value);
                    break;*/
                }
            }
            $.Widget.prototype._setOption.apply(this, arguments);
        },
       /* _setWidth: function(width) {
            this.uiChatboxTitlebar.width(width + "px");
            this.uiChatboxLog.width(width + "px");
            this.uiChatboxInput.css("maxWidth", width + "px");
            // padding:2, boarder:2, margin:5
            this.uiChatboxInputBox.css("width", (width - 18) + "px");
        },
        _position: function(offset) {
            this.uiChatbox.css("right", offset);
        }*/
    });
}(window.jQuery));


//Need this to make IE happy
//see http://soledadpenades.com/2007/05/17/arrayindexof-in-internet-explorer/
if(!Array.indexOf){
 Array.prototype.indexOf = function(obj){
	for(var i=0; i<this.length; i++){
	    if(this[i]==obj){
	        return i;
	    }
	}
	return -1;
 }
}


var chatboxManager = function() {

 // list of all opened boxes
 var boxList = new Array();
 // list of boxes shown on the page
 var showList = new Array();
 // list of first names, for in-page demo
 var nameList = new Array();
 // list of notifications per name
 var numberOfNotifications = new Object();

 var activeChatBox;

 var config = {
	width : 200, //px
	gap : 20,
	maxBoxes : 5,
	messageSent : function(dest, msg) {
	    // override this
	    $("#" + dest).chatbox("option", "boxManager").addMsg(dest, msg, true);
	}
 };

 var init = function(options) {
	$.extend(config, options)
 };

 var reset = function() {
	 boxList = new Array();
	 // list of boxes shown on the page
	 showList = new Array();
	 // list of first names, for in-page demo
	 nameList = new Array();
	 numberOfNotifications = new Object();		
	 activeChatBox = undefined;
 }

 var delBox = function(id) {
	// TODO
 };

 var getNextOffset = function() {
	var offset = (config.width + config.gap) * showList.length;
	// So that the first box isn't stuck to the right side of the screen
	offset = offset + 20;
	return offset;
 };

 var boxClosedCallback = function(id) {
	// close button in the titlebar is clicked
	var idx = showList.indexOf(id);
	if(idx != -1) {
	    showList.splice(idx, 1);
	    diff = config.width + config.gap;
	    for(var i = idx; i < showList.length; i++) {
		offset = $("#" + showList[i]).chatbox("option", "offset");
		$("#" + showList[i]).chatbox("option", "offset", offset - diff);
	    }
	}
	else {
	    alert("should not happen: " + id);
	}
 };

 // caller should guarantee the uniqueness of id
 var addBox = function(id, user, name) {
	var idx1 = showList.indexOf(id);
	var idx2 = boxList.indexOf(id);
	if(idx1 != -1) {
	    // found one in show box, do nothing
	}
	else if(idx2 != -1) {
	    // exists, but hidden
	    // show it and put it back to showList
	    $("#"+id).chatbox("option", "offset", getNextOffset());	    
	    showList.push(id);	    
	}
	else{
	    var el = document.createElement('div');
	    el.setAttribute('id', id);
	    $(el).chatbox({id : id,
			   user : user,
			   title : user.first_name,
			   hidden : false,
			   width : config.width,
			   offset : getNextOffset(),
			   messageSent : messageSentCallback,
			   boxClosed : boxClosedCallback
			  });
	    boxList.push(id);
	    showList.push(id);
	    nameList.push(user.first_name);
	    numberOfNotifications[user.first_name] = 0;
	    $("#incomingCall"+user.first_name).hide();
	    $("#remoteVideo"+user.first_name).hide();
	}
	this.toggleBox(user.first_name, false);
 };

 var messageSentCallback = function(id, user, msg) {
	var idx = boxList.indexOf(id);
	config.messageSent(nameList[idx], msg);
 };
 
 var dispatch = function(id, user, msg) {
	// dispatch only if chatbox exists	
	if($("#box" + id).length > 0) {		
		$("#box" + id).chatbox("option", "boxManager").addMsg(user, msg, true);
	}
	if(this.activeChatBox == undefined) {
		$("#chatbox"+id).show();
	}
	if(!isActive && user) {
		playChatNotification();
	}
	if(id != this.activeChatBox) {
		var nbNotif = numberOfNotifications[id] + 1;
    	    	$("#lastMessage"+id).html("<i>" + user + ": " + msg + "</i>");	
		$("#notificationsStatus"+id).html('<a href="#" style="font-size: small">' + nbNotif + '</a>');			
		numberOfNotifications[id] = nbNotif;
	}
 }

 var toggleBox = function(id, toggle) {
    if(this.activeChatBox != undefined) {
	$("#box" + this.activeChatBox).chatbox("option", "boxManager").close();
	$("#contact-" + this.activeChatBox).removeAttr("style");
	$("#contact-" + this.activeChatBox).attr("style", "line-height: 25px; cursor: pointer;");
	$("#participant-" + this.activeChatBox).remove();
    }
    this.activeChatBox = id;
    $("#contact-" + id).removeAttr("style");
    $("#contact-" + id).attr("style", "line-height: 25px; font-weight:bold;");
    $('#participant_list > tbody:last').append('<tr id="participant-' + id + '"><td style="border-top:0px;overflow:hidden;"><img class="img-circle" style="height:140px; width:140px;" src="resources/images/userPic_generic.png">    ' + id + '</td></tr>');
    if(toggle) {
	    numberOfNotifications[id] = 0;
	    $("#notificationsStatus"+id).html('');
	    $("#lastMessage"+id).html('');	    
	    $("#box"+id).chatbox("option", "boxManager").toggleBox();
    }
 }

 return {
	init : init,
	reset : reset,
	addBox : addBox,
	delBox : delBox,
	dispatch : dispatch,
	toggleBox : toggleBox
 };
}();


var contactList = [];
var idList = new Array();

var broadcastMessageCallback = function(from, msg) {
    for(var i = 0; i < idList.length; i ++) {
        //chatboxManager.addBox(idList[i]);
        $("#" + idList[i]).chatbox("option", "boxManager").addMsg(from, msg, true);
    }
}


// chatboxManager is excerpt from the original project
// the code is not very clean, I just want to reuse it to manage multiple chatboxes
chatboxManager.init({messageSent : broadcastMessageCallback});

function chat(contact, toggle) {                
    var id = "box" + contact;
    var found = false;
    for(var i = 0; i < idList.length && !found; i ++) {
        if(idList[i] == id) {
    		found = true;    	
        }	
    }
    
    if(found == false) {
        idList.push(id);        
        chatboxManager.addBox(id, 
                                {dest:"dest" + contact, // not used in demo
                                 title:"box" + contact,
                                 first_name:contact,
                                 last_name:""
                                 //you can add your own options too
                                });
        disableEndCallButton(contact);
	/*$('#participant_list > tbody:last').innerHtml = "";
	$('#participant_list > tbody:last').append(
			'<tr><td>' + 
				'<video id="localVideo" autoplay="autoplay" class="participant"></video>' +
			'</td></tr>');*/
    } else if(toggle) {
    	chatboxManager.toggleBox(contact, toggle);
    }
}

function closeChatBoxes() {
	for(var i = 0; i < idList.length; i ++) {
		$("#" + idList[i]).chatbox("option", "boxManager").close();				                   
        	$("#chat" + idList[i]).remove();
    	}				            	
	idList = new Array();
	chatboxManager.reset();
	contacts = [];
	contactList = [];
}

function addContact(contact) {	
	contact=$.trim(contact);	
	$('#sipContact').val('');
	$('#sipContact').keyup();
	// prevents 2 contacts with same name to be added
	if($("#contact-" + contact).length <= 0) {
		$('#contact_list > tbody:last').append(
			'<tr onclick="chat(\'' + contact + '\', \'true\')" id="contact-' + contact + '" style="line-height: 25px; cursor: pointer;">' +
				'<td style="border-top:0px;overflow:hidden;"><img class="img-circle" style="height:25px; width:25px;" src="resources/images/icon-users.jpg">  ' + contact + '</td>' +
				'<td id="notificationsStatus' + contact + '" style="border-top:0px;overflow:hidden;">&nbsp;&nbsp;' +					
				'</td>' +
				'<td style="border-top:0px;overflow:hidden;">&nbsp;&nbsp;' +
					'<a id="callStatus' + contact + '" href="#">' +
						'<i class="icon-facetime-video" style="font-size: 1.2em;"/>' +
					'</a>'+
				'</td>' + 
			'</tr>'+
			'<tr style="line-height: 10px;color: grey;font-size: small">' +
				'<td id="lastMessage' + contact + '" style="overflow:hidden;width: 200px;max-width:200px;height:20px; border-top:0px;font-size: small"></td>' +
			'</tr>');
		disableEndCallButton(contact);
		contactList.push(contact);
	}	
//	$('#contact_list > tbody:last').append('<tr onclick="chat(\'' + contact + '\')" id="contact-' + contact + '"><td>' + contact + '</td></tr>');
}

var isActive;

window.onfocus = function () { 
  isActive = true; 
}; 

window.onblur = function () { 
  isActive = false; 
}; 
