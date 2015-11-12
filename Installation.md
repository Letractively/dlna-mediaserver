# Download the webapp #

First of all you need the webapplication itself. Stable and tested releases can be found [here](http://code.google.com/p/dlna-mediaserver/downloads/list).

For ease of deployment place the war file somewhere on youre servers filesystem.

# Tomcat 6 #

TODO : Install Tomcat 6 and Manager

# Create context.xml #

There are but a few settings you have to configure before deploying the war into tomcat. This is all done in a context.xml for your mediaserver.

Please create a file named mediaserver-context.xml next to your downloaded war and add following xml content :

```
<Context path="/${webappname}" docBase="${path-to-war-file}">
     
    <Environment
        name="mediaserver.webapp.name"
        override="false"
        type="java.lang.String"
        value="${webappname}" />

    <Environment
        name="mediaserver.server.hostname"
        override="false"
        type="java.lang.String"
        value="${hostname}" />
        
    <Environment
        name="mediaserver.server.port"
        override="false"
        type="java.lang.Integer"
        value="${serverport}" /> 
       
    <Environment 
        name="mediaserver.database.dialect"
        override="false"
        type="java.lang.String"
        value="${hibernateDialect}" />

    <Resource
        name="jdbc/mediaserver"
        auth="Container"
        driverClassName="${jdbcDriver}"
        maxActive="100"
        maxIdle="30"
        maxWait="10000"
        password="${jdbcPassword}"
        type="javax.sql.DataSource"
        url="${jdbcUrl}"
        username="${jdbcUsername}" />
    
</Context>
```

So what do these settings mean?

|**Setting**|**Value**|**Example**|
|:----------|:--------|:----------|
|${path-to-war-file}|This would be the full path to where you downloaded the war file to, including the files name itself|/opt/downloads/dlna-mediaserver-1.1.1.war|
|${hostname}|This should be the servers hostname or ip adress. If you are not certain that all devices can reach the server by its name, put its ip adress here|192.168.1.20|
|${serverport}|This is the port on which tomcat is running. Tomcats default port is 8080|8080       |
|${webappname}|Which name should tomcat use to publish the webapp? This also defines the url on which you will be able to access the webgui|dlna-mediaserver|
|${hibernateDialect}|This value depends on the database you are using. Valid entries are listed [here](http://docs.jboss.org/hibernate/orm/3.5/api/org/hibernate/dialect/package-summary.html).|org.hibernate.dialect.MySQL5InnoDBDialect|
|${jdbcDriver}|The database driver for youre database. Remember the jar containing this driver has to be placed within tomcats lib/common-lib folder|com.mysql.jdbc.Driver|
|${jdbcUrl} |The url to youre database.|jdbc:mysql://192.168.1.20:3306/dlna-mediaserver?useUnicode=yes&amp;characterEncoding=UTF-8|
|${jdbcUsername}|The username for your database connection.|mediaserver|
|${jdbcPassword}|The password for your database user|12345678   |

## Choose your favorite database ##

The mediaserver strongly relies on some kind of database backend. Therefor you need to configure one.

To keep this Guide simple only MySql, H2 and Derby as filesystem based solutions will be explained. If you rather want to use a different database i assume you know what to configure by reading these examples anyway.

In any case you have to put the appropriate jdbc driver jar in tomcats lib or shared lib folder.

### MySql ###

Assuming you already have installed and configured your MySql server all you have to do is create a new user and database. The user needs to have the privilege to create tables on the database. Also its a good idea to set the databases default encoding to utf-8.

Lets assume you created database "dlna-mediaserver" and user "mediaserver" with password "12345678".

|**Setting**|**Value**|
|:----------|:--------|
|${hibernateDialect}|org.hibernate.dialect.MySQL5InnoDBDialect|
|${jdbcDriver}|com.mysql.jdbc.Driver|
|${jdbcUrl} |jdbc:mysql://localhost:3306/dlna-mediaserver?useUnicode=yes&amp;characterEncoding=UTF-8|
|${jdbcUsername}|mediaserver|
|${jdbcPassword}|12345678 |

Now add the [mysql-connector.jar](http://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/5.1.20/mysql-connector-java-5.1.20.jar) to tomcats lib or shared lib folder.


### Filesystem ###

Create a folder which is accessible by the tomcat user. Lets assume you chose /opt/mediaserver/database.

#### Derby ####

|**Setting**|**Value**|
|:----------|:--------|
|${hibernateDialect}|org.hibernate.dialect.DerbyDialect|
|${jdbcDriver}|org.apache.derby.jdbc.EmbeddedDriver|
|${jdbcUrl} |jdbc:derby:/opt/mediaserver/database;create=true|
|${jdbcUsername}|mediaserver|
|${jdbcPassword}|12345678 |

Please add the [derby.jar](http://search.maven.org/remotecontent?filepath=org/apache/derby/derby/10.8.2.2/derby-10.8.2.2.jar) and [derby-tools.jar](http://search.maven.org/remotecontent?filepath=org/apache/derby/derbytools/10.8.2.2/derbytools-10.8.2.2.jar) to tomcats lib or shared lib folder.

#### H2 ####

|**Setting**|**Value**|
|:----------|:--------|
|${hibernateDialect}|org.hibernate.dialect.H2Dialect|
|${jdbcDriver}|org.h2.Driver|
|${jdbcUrl} |jdbc:h2:/opt/mediaserver/database;create=true|
|${jdbcUsername}|mediaserver|
|${jdbcPassword}|12345678 |

Please add the [h2.jar](http://search.maven.org/remotecontent?filepath=com/h2database/h2/1.3.167/h2-1.3.167.jar) to tomcats lib or shared lib folder.

# Deploy #

If you have come so far, deployment is fairly easy now. Start your tomcat and direct your browser to its manager (http://192.168.1.20:8080/manager/html). Scroll down to "Deploy" -> "Deploy directory or WAR file located on server" and deploy with the follwoing settings:

|Context Path (required):|/${webappname}|this is the same as you wrote in your context.xml but is required here|
|:-----------------------|:-------------|:---------------------------------------------------------------------|
|XML Configuration file URL:|the full path to the mediaserver-context.xml you just created|                                                                      |


Thats it. If for any reason the webapp won't start, please recheck your context.xml and make sure the required jdbc-jar is placed in tomcats lib or shared lib folder.



