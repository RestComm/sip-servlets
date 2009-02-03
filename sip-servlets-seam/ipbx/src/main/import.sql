insert into USERS(USERID,VERSION,name,realName,password) values(1,1,'vralev','Vladimir Ralev','vralev');
insert into USERS(USERID,VERSION,name,realName,password) values(2,1,'contact','Contact','contact');
insert into USERS(USERID,VERSION,name,realName,password) values(3,1,'admin','Admin','admin');

insert into CONTACTS(CONTACTID,VERSION,USERID,uri) values(1,1,1,'sip:contact@localhost:5060');
insert into CONTACTS(CONTACTID,VERSION,USERID,uri) values(2,1,1,'sip:contact@192.168.1.187:5060');
insert into CONTACTS(CONTACTID,VERSION,USERID,uri) values(3,1,1,'sip:contact@localhost:5064');
insert into CONTACTS(CONTACTID,VERSION,USERID,uri) values(4,1,2,'sip:contact@192.168.1.187:5064');
insert into CONTACTS(CONTACTID,VERSION,USERID,uri) values(5,1,1,'18004664411');


insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(1,1,1,false,'sip:vralev@localhost');
insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(2,1,1,false,'sip:vralev@192.168.1.187');
insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(3,1,1,false,'sip:vralev@localhost:5064');
insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(7,1,1,true,'359888579097');
insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(8,1,3,true,'sip:contact@localhost:5064');
--insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(9,1,3,false,'sip:contact@localhost');
--insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(10,1,1,false,'sip:vralev@nist.gov');
insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(11,1,1,false,'sip:vralev-ekiga@192.168.1.187:5061');
insert into REGISTRATIONS(REGISTRATIONID,VERSION,USERID,selected,uri) values(12,1,1,false,'sip:receiver@sip-servlets.com');

--insert into BINDINGS(BINDINGID,VERSION,CONTACTADDRESS,CALLID,CSEQ,EXPIRES,REGISTRATIONID) values(1,1,'sip:vralev@localhost:5060','cid1',1,5000,1);
--insert into BINDINGS(BINDINGID,VERSION,CONTACTADDRESS,CALLID,CSEQ,EXPIRES,REGISTRATIONID) values(2,1,'sip:vralev@192.168.1.187:5060','sdfd2s',2,5200,1);
--insert into BINDINGS(BINDINGID,VERSION,CONTACTADDRESS,CALLID,CSEQ,EXPIRES,REGISTRATIONID) values(3,1,'sip:vralev@192.168.1.187:5064','sdfd2s',2,5200,2);
--insert into BINDINGS(BINDINGID,VERSION,CONTACTADDRESS,CALLID,CSEQ,EXPIRES,REGISTRATIONID) values(4,1,'sip:vralev@localhost:5064','sdfd2s',2,5200,2);

insert into HISTORYLOG(HISTORYID,VERSION,USERID,MESSAGE, TIMESTAMP) values(1,1,1,'Missed call from sip:c1@swdsd.com','2009-01-14 11:39:00');
insert into HISTORYLOG(HISTORYID,VERSION,USERID,MESSAGE, TIMESTAMP) values(2,1,1,'Answered call from sip:c1@swdsd.com','2009-01-14 16:39:00');

insert into ROLES(ROLEID,VERSION,USERID,role) values(1,1,1,'caller');
insert into ROLES(ROLEID,VERSION,USERID,role) values(2,1,1,'admin');
insert into ROLES(ROLEID,VERSION,USERID,role) values(3,1,2,'caller');
insert into ROLES(ROLEID,VERSION,USERID,role) values(4,1,2,'admin');
insert into ROLES(ROLEID,VERSION,USERID,role) values(5,1,3,'caller');
insert into ROLES(ROLEID,VERSION,USERID,role) values(6,1,3,'admin');

insert into GLOBALPROPERTIES(GLOBALPROPERTYID, VERSION, NAME, VALUE) values (1,1,'pbx.hostname','localhost')
insert into GLOBALPROPERTIES(GLOBALPROPERTYID, VERSION, NAME, VALUE) values (2,1,'pbx.call.timeout','10000')
insert into GLOBALPROPERTIES(GLOBALPROPERTYID, VERSION, NAME, VALUE) values (3,1,'pbx.default.ringback.tone','file:///home/vralev/welcome.wav')
insert into GLOBALPROPERTIES(GLOBALPROPERTYID, VERSION, NAME, VALUE) values (4,1,'pbx.default.rejected.announcement','rejected.wav')

insert into PSTNGATEWAYACCOUNTS(PSTNACCOUNTID, VERSION, NAME, USERNAME, HOSTNAME, PASSWORD) values(1,1,'Callwithus account','189964505','uk.callwithus.com','412944')
