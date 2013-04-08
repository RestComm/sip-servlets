/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2013, TeleStax and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

function setMenu(element, module) {
	$('#content').load('modules/' + module + '.html', function() {
		$('.nav-list').children('.active').removeClass('active');
		$(element).parent().addClass('active');
	});
}

function showConnectionOptions() {
	$("#connection-options").toggle();
}

function changeServerState(method) {
	mbeanSearch="*:type=SipApplicationDispatcher";
	var mbean;
	$.ajax({
		dataType: "json",
		url: window.jolokiaProtocol + window.jolokiaAddress + ":" + window.jolokiaPort + "/jolokia/search/" + mbeanSearch
	})
	.done(function(html) {
		if (html.error) {
			logToConsole("ERROR", html.error);
		}
		else {			
			mbean = html.value[0];
			timeToWait = $("#time-to-wait").val();
			$.ajax({
				dataType: "json",
				url: window.jolokiaProtocol + window.jolokiaAddress + ":" + window.jolokiaPort + "/jolokia/exec/" + jolokia.escape(mbean) + "/"  + method + "/" + timeToWait
			})
			.done(function(html) {
				if (html.error) {
					logToConsole("ERROR", html.error);
				}
				else {
					logToConsole("INFO", "Server stopped");
				}
			})
			.fail(function() {
				logToConsole("ERROR", "Failure trying to communicate with the SIP Servlets Server. Please try again later.");
			})
		}
	})
	.fail(function() {
		logToConsole("ERROR", "Failure trying to communicate with the SIP Servlets Server. Please try again later.");
	})
	
	
}

function logToConsole(level, text) {
	var time = new Date();
	var h = time.getHours();
	var m = time.getMinutes();
	var s = time.getSeconds();
	var ms = time.getMilliseconds();
	var timeStr = (h < 10 ? ('0' + h) : h) + ':' + (m < 10 ? ('0' + m) : m) + ':' + (s < 10 ? ('0' + s) : s) + ':' + (ms < 10 ? ('00' + ms) : (ms < 100 ? '0' + ms : ms));
	if (level == "INFO") {
		$('#console-log-text').prepend(timeStr + ' [' + level +'] ' + text + '<br/>');		
	}
	else {
		$('#console-log-text').prepend("<span style='color: #CC0000;'>" + timeStr + " [" + level + "] " + text + "</span><br/>");
	}
}

function clearConsole() {
	$('#console-log-text').html('');
}

function minimizeConsole() {
	$('.console').animate({height: '20px'});
	$('#btn-min-console').hide();
	$('#btn-max-console').show();
}

function maximizeConsole() {
	$('.console').animate({height: '160px'});
	$('#btn-min-console').show();
	$('#btn-max-console').hide();
}

function filterDeployableUnitsList() {
	filter = $("#dus-filter-input").val();
	if(filter) {
		$(".du-row").each(function() {
			rowText = $(this).text();
			if (rowText.indexOf(filter) !== -1) {
				$(this).fadeIn();
			}
			else {
				$(this).fadeOut();
			}
		});
	}
	else {
		$(".du-row").fadeIn();
	}
}

// TODO: Optimize and Refactor this
function filterComponents() {
	filterName = $("#comp-filter-input-name").val();
	filterVendor = $("#comp-filter-input-vendor").val();
	filterVersion = $("#comp-filter-input-version").val();

	// reset to total count
	$(".component-row-count").each(function() {
		txt = $(this).text();
		$(this).text(txt.substring(txt.indexOf('/')+1));
	});

	if(filterName || filterVendor || filterVersion) {
		$(".component-id-row").each(function() {
			nameText = $($(this).children().get(1)).text();
			vendorText = $($(this).children().get(2)).text();
			versionText = $($(this).children().get(3)).text();

			typeCountEl = $(this).parent().parent().parent().parent().prev().children(".component-row-count");
			typeCount = typeCountEl.text();

			if (typeCount.indexOf('/') == -1) {
				resTotal = typeCount;
				resCount = parseInt(resTotal);
			}
			else {
				resTotal = typeCount.substring(typeCount.indexOf('/')+1);
				resCount = parseInt(typeCount.substring(0, typeCount.indexOf('/')));
			}

			if (filterName) {
				if (nameText.indexOf(filterName) !== -1) {
					$(this).fadeIn();
				}
				else {
					console.log("OUT BY NAM" + resCount + " > " + nameText + ";" + vendorText + ";" + versionText);
					typeCountEl.text(--resCount + "/" + resTotal);
					$(this).fadeOut();
					return true;
				}
			}

			if (filterVendor) {
				if (vendorText.indexOf(filterVendor) !== -1) {
					$(this).fadeIn();
				}
				else {
					console.log("OUT BY VND" + resCount + " > " + nameText + ";" + vendorText + ";" + versionText);
					typeCountEl.text(--resCount + "/" + resTotal);
					$(this).fadeOut();
					return true;
				}
			}

			if (filterVersion) {
				if (versionText.indexOf(filterVersion) !== -1) {
					$(this).fadeIn();
				}
				else {
					console.log("OUT BY VRS" + resCount + " > " + nameText + ";" + vendorText + ";" + versionText);
					typeCountEl.text(--resCount + "/" + resTotal);
					$(this).fadeOut();
					return true;
				}
			}
			typeCountEl.text(resCount + "/" + resTotal);
			console.log("" + resCount + " > " + nameText + ";" + vendorText + ";" + versionText);
		});
	}
	else {
		$(".component-id-row").fadeIn();
	}
}

function filterComponentsClear() {
	$(".filter-component").val("");
	filterComponents();
}

/* Doesn't work... no path :(
function onFileChange() {
	//get the file path
	var file = $('#realfile').val();
	//pull out the filename
	file = file.replace(/^.*\\/i, "");
	//show to user
	$('#du-filename').text(file);
	$("#install-du-btn").fadeOut(300, function(){
		$("#du-filename-box").fadeIn(300);
	});
}

function cancelDeployableUnitInstall() {
	$("#du-filename-box").fadeOut(300, function(){
		$("#install-du-btn").fadeIn(300);
	});
}

function confirmDeployableUnitInstall() {
	if ($("#du-persistent").attr("checked")) {
		mbean = "org.mobicents.slee:name=DeployerMBean";
		method = "persistentInstall";
	}
	else {
		mbean = "javax.slee.management:name=Deployment";
		method = "install";
	}
	jolokia.execute(mbean, method, $("#realfile").val(),
	{
		success: function(value) {
			//console.log(JSON.stringify(value));
			logToConsole('INFO', 'Server ' + method + ' completed successfuly.');
		}
	});
}
*/

function showDeployableUnitPathInput() {
	$("#show-install-btn").hide();
	$("#du-install-box").css("display", "block");
	$("#du-filename-path").focus();
}

function cancelDeployableUnitInstall() {
	$("#du-filename-path").val("");
	$("#du-install-box").css("display", "none");
	$("#show-install-btn").show();
}

//function doDeployableUnitInstall(persistent) {
//	if (persistent) {
//		mbean = "org.mobicents.slee:name=DeployerMBean";
//		method = "persistentInstall";
//	}
//	else {
//		mbean = "javax.slee.management:name=Deployment";
//		method = "install";
//	}
//	$.ajax({
//		dataType: "json",
//		url: window.jolokiaProtocol + window.jolokiaAddress + ":" + window.jolokiaPort + "/jolokia/exec/" + jolokia.escape(mbean) + "/"  + method + "/" +  $("#du-filename-path").val().replace(/\//g,'!/')
//	})
//	.done(function(html) {
//		if (html.error) {
//			logToConsole("ERROR", html.error);
//		}
//		else {
//			logToConsole("INFO", "Deployable Unit " + $("#du-filename-path").val() + " successfuly deployed.");
//		}
//	})
//	.fail(function() {
//		logToConsole("ERROR", "Failure trying to communicate with the JAIN SLEE Server. Please try again later.");
//	})
//	.always(function() {
//		cancelDeployableUnitInstall();
//	});
//	// jolokia.execute(mbean, method, $("#du-filename-path").val(),
//	// {
//	// 	success: function(value) {
//	// 		//console.log(JSON.stringify(value));
//	// 		logToConsole('INFO', 'Server ' + method + ' completed successfuly.');
//	// 	}
//	// });
//}
//
//function uninstallDeployableUnit(url) {
//	mbean = "javax.slee.management:name=Deployment";
//	method = "uninstall";
//	// jolokia.execute(mbean, method, url,
//	// {
//	// 	success: function(value) {
//	// 		//console.log(JSON.stringify(value));
//	// 		logToConsole('INFO', 'Server ' + method + ' completed successfuly.');
//	// 	}
//	// });
//	$.ajax({
//		dataType: "json",
//		url: window.jolokiaProtocol + window.jolokiaAddress + ":" + window.jolokiaPort + "/jolokia/exec/" + jolokia.escape(mbean) + "/"  + method + "/" +  url.replace(/\//g,'!/')
//	})
//	.done(function(html) {
//		if (html.error) {
//			logToConsole("ERROR", html.error);
//		}
//		else {
//			logToConsole("INFO", "Deployable Unit " + $("#du-filename-path").val() + " successfuly deployed.");
//		}
//	})
//	.fail(function() {
//		logToConsole("ERROR", "Failure trying to communicate with the JAIN SLEE Server. Please try again later.");
//	})
//	.always(function() {
//		cancelDeployableUnitInstall();
//	});
//}