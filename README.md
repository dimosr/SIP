Iprovements over SIP<br/> (Session Inititation Protocol)<br/> Communication Platform
=========================================================================

Introduction
--------------------------------------------------------------------------
The purpose of this project was to design and implement 3 new functionalities in the initial application containing a client component (SIP Commmunicator) and a  server component (Jain SIP Proxy). Those 2 programs implement the protocol SIP, which is defined under the RFC 3261[1], which was published by the organization National Institute of Standards and Technology (NIST). The SIP Protocol allows the evolution of communication applications through Internet, amongst them the service of Voice over IP (VoIP), which is used for Skype.

* NIST Site : http://snad.ncsl.nist.gov/
* RFC Link : http://www.ietf.org/rfc/rfc3261.txt
* Initial Package : https://jsip.ci.cloudbees.com/job/jsip/ws/

Initial Package
----------------------------------------------------
The initial package was an implementetion of Sip Communicator and Sip Proxy Server. Logging in without authentication was allowed from Sip Communicator to Proxy Server. After the registration, each Sip Communicator instance could make call (including voice and image), to any other Sip Communicator instance that was registered to the same Proxy Server.

Improvements
--------------------------------------------------------
The improvements that were implemented over the pre-existing package included
the following 4 features :
- Authentication (single registration - multiple logins)
- Blocking 
- Forwarding 
- Billing
- Friending | Unfriending

___

[Authentication] :
User is required to have made at least a registration to the Proxy Server prior to logging. The info given during the registration are the username, the password, the email and the address of the user. If the user has not made any registration prior to the attempt to login, he is not allowed to login. Similarly, if the user gives a wrong password, he is not allowed to login to the application.

___

[Blocking] :
User can block other users, so that they cannot make a call to him. If a blocked user attempts a call to his blockee, he will see that the other user is blocked, so that he will not be able to understand that the other user blocked him. With the corresponding button in the menu the user can see all the users that he has blocked.

___

[Forwarding] :
A user can forward all the calls to another user. In this way, whoever calls this user, will make a call to the user this call was forwarded to. It is also implemented an additional feature, so that the circular forwardings are not allowed and the procedure of forwarding is stopped in the last user before a circle is completed.

___

[Billing] :
Each user is charged for each call made to another user. The calls are charged linearly dependent on the duration of the call and there is also a level, under which the users are charged with constant cost. Also, different charges occur between friends (see next). With the corresponding button in the menu, the user can see the total cost of all the calls he has made.

___

[Friending] :
Each user can friend another user. This functionality is mutual, so if user A friends user B, the B will not need to acknoledge the friendship. Each of the 2 users can unfriend the other in order to stop this relationship between them. 2 users that are friended benefit from lower charges during their calls. With the corresponding button in the menu, the user can see all his current friends.

Installation & Deployment
-------------------------------------------------------
In order to execute this package, the instructions that have to be followed are
the following :

- Installation of Java SE 7u7 JDK

Install the corresponding Java for your Operating System from 
[Oracle java repository](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

- Installation of the environment Java Media Framework

Download JMF 2.1.1e Software for our specific environment from
[Oracle jmf repository](http://www.oracle.com/technetwork/java/javase/download-142937.html)

- Update environment variable PATH to contain JAVA_CLASSPATH

- Installation of Eclipse

- Download of the package form Github (folder SipProject)

- Start Eclipse 

Choose as workspace the path (...\SipProject\workspace) (or .../SipProject/workspace in Unix)

- Import Sip Communicator and Proxy Server

Choose form upper menu of eclipse
File -> Import -> General -> Existing Projects into Workspace -> Next -> (Browse) -> ...\SipProject and select 'Sip Communicator' and 'Proxy Server'

- Installation of mySQL - Configuration

Install the mySQL in your PC, and edit the class gov.nist.sip.proxy.Database.java to update your own database credentials. Also, you may need to re-import the mysql-connector jar from the folder SipProject\sipproxy\lib. This is done by right-clicking in sipproxy in eclipse and then
Configure Build Path -> Add External Jar -> browse to the file

- Configuration files of Communicator

Set the following attributes in the file sip-communicator.xml:
```xml
<AUDIO_PORT value="22224"/>
<VIDEO_PORT value="22222"/>
<REGISTRAR_ADDRESS value="<ip of Proxy Server>:4000"/>
<REGISTRAR_PORT value="5060"/>
<DEFAULT_DOMAIN_NAME value="<ip of Proxy Server>:4000"/>
<DEFAULT_AUTHENTICATION_REALM value="<ip of Proxy Server>:4000"/>
<IP_ADDRESS value="<ip of Communicator>"/>
<OUTBOUND_PROXY value="<ip of Proxy Server>:4000/udp"/>
```

Also set the following attributes in the file configuration.xml of Proxy Server :
```xml
stack_IP_address="<ip of Proxy Server>"
```

- Run Proxy Server 

We choose from Eclipse the class gov.nist.sip.proxy.gui.ProxyLauncher.java and with right-click execute
Run As -> Java Application -> New and add the following as Argument :
```sh
–cf gov/nist/sip/proxy/configuration/configuration.xml
```

- Run Sip Communicator 

We choose from Eclipse the class net.java.sip.communicator.SipCommunicator.java and with right-ckick execute
Run As -> Java Application -> New and add the following as VM Argument :
```sh
-Djava.library.path=./lib
```

-  Start the proxy

We press the button Start the Proxy in Proxy Server
We also press Register from Sip Communicator 

- Here we are ready !!!


