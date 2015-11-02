Java Settlers - A web-based client-server version of Settlers of Catan

Introduction
------------

JSettlers is a web-based version of the board game Settlers of Catan
written in Java. This client-server system supports multiple
simultaneous games between people and computer-controlled
opponents. Initially created as an AI research project.

The client may be run as a Java application, or as an applet when
accessed from a web site which also hosts a JSettlers server.

The server may be configured to use a database to store account
information and game stats (details below).  A client applet to
create user accounts is also provided.

If you're upgrading from an earlier version of JSettlers, check
VERSIONS.txt for new features, bug fixes, and config changes.

JSettlers is an open-source project licensed under the GPL. The
project is hosted at http://sourceforge.net/projects/jsettlers2
and at http://nand.net/jsettlers/devel/ .  Questions, bugs, and
patches can be posted at the sourceforge page; patches can also
be sent to https://github.com/jdmonin/JSettlers2 .

                          -- The JSettlers Development Team


Contents
--------

  Documentation
  Requirements
  Setting up and testing
  Shutting down the server
  Hosting a JSettlers Server
  Upgrading from an earlier version
  Database Setup
  Development and Compiling


Documentation
-------------

User documentation for game play is available as .html pages located
in "docs/users" directory. These can be put on a JSettlers server for
its users using the applet.

Currently, this README is the only technical documentation for running
the client or server, setup and other issues. Over time, more docs
will be written. If you are interested in helping write documentation
please contact the development team from the SourceForge site.


Requirements
------------

To play JSettlers by connecting to a remote server you will need the
Java Runtime Version 5 or above. To connect as an applet, use any
browser which is Java enabled (using the browser plug-in) or just
download the JAR from http://nand.net/jsettlers/ and run it.

To Play JSettlers locally you need the Java Runtime 5 or above.
JSettlers-full.jar can connect directly to any server over the Internet.

To host a JSettlers server that provides a web applet for clients, you will
need an http server such as Apache's httpd, available from http://httpd.apache.org.

The JSettlers-full.jar file can also run locally as a server, without
needing a web server.  The applet is considered more convenient,
because you know everyone will have the same version.

To build JSettlers from source, you will need Apache Ant, available from
http://ant.apache.org, or an IDE such as Eclipse which understands Ant's format.


Setting up and testing
----------------------

From the command line, make sure you are in the JSettlers distribution
directory which contains both JSettlers.jar, JsettlersServer.jar and the
"lib" directory.  (If you have downloaded jsettlers-2.x.xx-full.tar.gz,
look in the src/target directory for these files.)

If you have downloaded jsettlers-2.x.xx-full.jar or jsettlers-2.x.xx-server.jar
instead of the full tar.gz, use that filename on the command lines shown below.


SERVER STARTUP:

Start the server with the following command
(server requires Java 5 or higher, or JDK 1.5 or higher):

  java -jar JSettlersServer.jar

This will start the server on the default port of 8880 with 7 robots.

You can change those values and specify game option defaults; see details below.

If MySQL or another database is not installed and running (See "Database Setup"),
you will see a warning with the appropriate explanation:

  Warning: failed to initialize database: ....

The database is not required: Without it, the server will function normally
except that user accounts cannot be maintained.

If you do use the database, you can give users a nickname and password to use
when they log in and play.  People without accounts can still connect, by
leaving the password field blank, as long as they aren't using a nickname
which has a password in the database.  Optionally game results can also be
stored in the database, see next section; results are not stored by default.

Parameters and game option defaults:

JSettlers options, parameters, and game option defaults can be specified on the
command line, or in a jsserver.properties file in the current directory when
you start the server.

Command line example:
  java -jar JSettlersServer.jar -Djsettlers.startrobots=9 8880 30 socuser socpass

In this example the parameters are: start 9 bots; TCP port number 8880;
max clients 30; db user; db password.

The started robots count against your max simultaneous connections (30 in this
example).  If the robots leave less than 6 player connections available, or if
they take more than half the max connections, a warning message is printed at
startup. To start a server with no robots (human players only), use
-Djsettlers.startrobots=0 .

Any command-line switches and options go before the port number if specified
on the command line.  If the command includes -jar, switches and options go
after the jar filename.

To change a Game Option from its default, for example to activate the house rule
"Robber can't return to the desert", use the "-o" switch with the game option's
name and value, or equivalently -Djsettlers.gameopt. + the name and value:
   -o RD=t
   -Djsettlers.gameopt.RD=t

To have all completed games' results stored in the database, use this option:
  -Djsettlers.db.save.games=Y

To see a list of all jsettlers options (use them with -D), run:
  java -jar JSettlersServer.jar --help
This will print all server options, and all Game Option default values. Note the
format of those default values: Some options need both a true/false flag and a
numeric value. To change the default winning victory points to 12 for example:
  -o VP=t12

jsserver.properties:

Instead of a long command line, any option can be added to jsserver.properties
which is read at startup if it exists in the current directory.  Any option
given on the command line overrides the same option in the properties file.
Comment lines start with # .

This example command line
  java -jar JSettlersServer.jar -Djsettlers.startrobots=9 -o RD=t 8880 30 socuser socpass
is the same as jsserver.properties with these contents:
jsettlers.startrobots=9
jsettlers.gameopt.RD=t
jsettlers.port=8880
jsettlers.connections=30
# db user and pass are optional
jsettlers.db.user=socuser
jsettlers.db.pass=socpass

To determine if the server is reading the properties file, look for this text
near the start of the console output:
  Reading startup properties from jsserver.properties


CLIENT CONNECT:

Now, double-click JSettlers.jar to launch the client.

Or, if you want to see the message traffic and debug output, from another command line window
start the player client with the following command:

  java -jar JSettlers.jar localhost 8880

In the player client window, enter any name in the Nickname field and
create a new game.

Type *STATS* into the chat part of the game window.  You should see
something like the following in the chat display:

  * > Uptime: 0:0:26
  * > Total connections: 1
  * > Current connections: 1
  * > Total Users: 1
  * > Games started: 0
  * > Games finished: 0
  * > Total Memory: 2031616
  * > Free Memory: 1524112
  * > Version: 1100 (1.1.00) build JM20080808

Now click on the "Sit Here" button and press "Start Game".  The robot
players should automatically join the game and start playing.

To play again with the same game options and players, click "Quit", then "Reset Board".
If other people are in the game, they will be asked to vote on the reset; any player can reject it.
If bots are in your game, and you want to reset with fewer or no bots, click the bot's Lock button
before clicking Quit/Reset and it won't rejoin the reset game.

If you want other people to access your server, tell them your server
IP address and port number (in this case 8880).  They can run the
JSettlers.jar file by itself, and it will bring up a window to enter your IP
and port number.  Or, they can enter the following command:

  java -jar JSettlers.jar <host> <port_number>

Where host is the IP address and port_number is the port number.

If you would like to maintain accounts for your JSettlers server,
start the database prior to starting the JSettlers Server. See the
directions in "Database Setup".



Shutting down the server
------------------------

To shut down the server enter *STOP* in the chat area of a game
window.  This will stop the server and all connected clients will be
disconnected.  (Only debug users can shut down the server.
See README.developer if you want that.)


Hosting a JSettlers server
--------------------------
  - Start MySQL or PostgreSQL server
    (this database is optional; if you want a db, file-based sqlite also works)
  - Start JSettlers Server
  - Start http server (optional)
  - Copy JSettlers.jar client JAR and web/*.html to an http-served directory (optional)

To host a JSettlers server, start the server as described in "Setting up
and Testing". To maintain user accounts, be sure to start the database
first. (If you use a database, you can give users an account; everyone else
can still log in and play, by leaving the password field blank.)

Remote users can simply start their clients as described there,
and specify your server as host.

To provide a web page from which users can run the applet, you will
need to set up an http server such as Apache.  We assume you have
installed it already, and will refer to "${docroot}" as a directory
to place files to be served by your web server.

Copy index.html from src/web/ to ${docroot}.  If you're going to use an
accounts database and anyone can register their own account (this is not
the default setting), also copy accounts.html.

Edit the html to make sure the PORT parameter in "index.html" and "account.html"
applet tags match the port of your JSettlers server, and the text starting
"this applet connects to" has the right server name and port for users who can't
run the applet in their browser.  If you're using account.html, also
un-comment index.html's link to account.html.

Next copy the JSettlers.jar client file to ${docroot}. This will allow users
to use the web browser plug-in or download it to connect from their computer.
If you've downloaded it as JSettlers-{version}-full.jar, rename it to JSettlers.jar.

Your web server directory structure should now contain:
  ${docroot}/index.html
  ${docroot}/account.html (optional)
  ${docroot}/JSettlers.jar

Users should now be able to visit your web site to run the JSettlers client.


Upgrading from an earlier version
---------------------------------

If you're doing a new installation, not upgrading a server that's
already been running JSettlers, skip this section.

It's a simple process to upgrade to the latest version of JSettlers:

- Read VERSIONS.txt for new features, bug fixes, and config changes
  made from your version to the latest version.  Occasionally defaults
  change and you'll need to add a server config option to keep the
  same behavior, so read carefully.

- Save a backup copy of your current JSettlers.jar and JSettlersServer.jar,
  in case you want to run the old version for any reason.

- If you're using the optional database for user accounts and game scores,
  make a backup or export its contents.  Note that no schema changes or
  SQL scripts are needed to upgrade to the latest version.

- Stop the old server

- Copy the new JSettlers.jar and JSettlersServer.jar into place

- Start the new server, including any new options you wanted from VERSIONS.txt

- Test that you can connect and start games as usual, with and without bots.
  When you connect make sure the version number shown in the left-center of
  the client window is the new JSettlers version.


Database Setup
--------------

If you want to maintain user accounts, you will need to set up a MySQL, SQLite,
or PostgreSQL database. This will eliminate the "Problem connecting to database"
errors from the server, and also gives you the option to save all game scores
for reports or community-building.

Note: If you're upgrading to JSettlers 1.1.19, for security reasons the default
has changed to disallow user account self-registration. If you still want to
use that option, search below for "open registration".

For these instructions we'll assume you already installed the PostgreSQL or
MySQL software. SQLite is an easy database choice because it's just a JAR
file, not a separate large install, if you're looking to avoid that.

You will need a JDBC driver JAR file in your classpath or the same directory as
the JSettlers JAR, see below for details. Besides PostgreSQL, MySQL, or SQLite
any JDBC database can be used, including Oracle or MS SQL Server; however only
those three db types are tested in depth with JSettlers.

The default name for the database is "socdata".  To use another name,
you'll need to specify it as a JDBC URL on the command line, such as:
	-Djsettlers.db.url=jdbc:mysql://localhost/socdata
or
	-Djsettlers.db.url=jdbc:postgresql://localhost/socdata
or
	-Djsettlers.db.url=jdbc:sqlite:jsettlers.sqlite
If needed you can also specify a database username and password as:
	-Djsettlers.db.user=socuser -Djsettlers.db.pass=socpass
or place them on the command line after the port number and max connections:
	$ java -jar JSettlersServer.jar -Djsettlers.db.url=jdbc:mysql://localhost/socdata -Djsettlers.db.jar=mysql-connector-java-5.1.34-bin.jar 8880 20 socuser socpass


Finding a JDBC driver JAR:

The default JDBC driver is com.mysql.jdbc.Driver.  PostgreSQL and SQLite are also
recognized.  To use PostgreSQL, use a postgresql URL like the one shown above,
or specify the driver on the SOCServer command line:
	-Djsettlers.db.driver=org.postgresql.Driver
To use SQLite, use a sqlite url like the one shown above, or specify a
sqlite driver such as:
	-Djsettlers.db.driver=org.sqlite.JDBC
If a database URL is given but SOCServer can't connect to the database, it won't continue startup.

Depending on your computer's setup, you may need to point JSettlers at the
appropriate JDBC drivers, by placing them in your java classpath.
Your database system's JDBC drivers can be downloaded at these locations:
	MySQL:   http://www.mysql.com/products/connector/
	PostgreSQL:  http://jdbc.postgresql.org/download.html
	SQLite:  https://bitbucket.org/xerial/sqlite-jdbc
	  If sqlite crashes jsettlers on launch, or gives java.lang.UnsatisfiedLinkError
	  at launch but doesn't crash, add -Dsqlite.purejava=true before -jar on the
	  java command line and retry.

In some cases, adding to the classpath won't work because of JVM restrictions
about JAR files.  If you find that's the case, place the JDBC jar in the same
location as JSettlersServer.jar, and specify on the jsettlers command line:
	-Djsettlers.db.jar=sqlite-jdbc-3.7.2.jar
(sqlite jar filename may vary, update the parameter to match it).


Database Creation:

To create the jsettlers database in mysql, execute the SQL db scripts
jsettlers-create.sql and jsettlers-tables.sql located in src/bin/sql/
(included in jsettlers-2.x.xx-full.tar.gz):

$ mysql -u root -p -e "SOURCE jsettlers-create-mysql.sql"
This will connect as root, prompt for the root password, create a 'socuser' user with the password
'socpass', and create the 'socdata' database.

To build the empty tables, run:
$ mysql -u root -D socdata -p -e "SOURCE jsettlers-tables.sql"

For Postgres, create the db and tables with:
$ psql --file jsettlers-create-postgres.sql
$ psql --file jsettlers-tables.sql socdata

For sqlite, copy jsettlers-tables.sql to the same directory as
JSettlersServer.jar and sqlite-jdbc-3.7.2.jar and run this command
(sqlite jar filename may vary, update the jsettlers.db.jar parameter
to match it):
$ java -jar JSettlersServer.jar -Djsettlers.db.jar=sqlite-jdbc-3.7.2.jar  -Djsettlers.db.url=jdbc:sqlite:jsettlers.sqlite  -Djsettlers.db.script.setup=jsettlers-tables.sql
After a few seconds you should see this message:
	DB setup script was successful. Exiting now.
The directory should also now contain a jsettlers.sqlite file.

This will create jsettlers.sqlite containing the empty tables.
This script will fail if the tables already exist.


Optional: Storing Game Scores in the DB:

To automatically save all completed game results in database, use this option when
starting the JSettlers server:
	-Djsettlers.db.save.games=Y


Optional: Creating JSettlers Player Accounts in the DB:

To create player accounts, run the simple account creation client with the
following command:

  java -jar JSettlers.jar soc.client.SOCAccountClient localhost 8880

Users with accounts must type their password to log into the server to play.
People without accounts can still connect by leaving the password field blank,
as long as they aren't using a nickname which has a password in the database.

In versions before 1.1.19, anyone could create their own user accounts
(open registration). In 1.1.19 this default was changed to improve security:
An existing user must log in before creating any new account.  If you still
want to allow open registration of user accounts, include this option when
you start your server:
	-Djsettlers.accounts.open=y

When you first set up the database, there won't be any user accounts, so the
server will allow anyone to create the first account.  Please be sure to
create that first user account soon after you set up the database.

If you want to require that all players have accounts and passwords, include
this option when you start your server:
	-Djsettlers.accounts.required=y

To permit only certain users to create new accounts, instead of all users,
list them when you start your server:
	-Djsettlers.accounts.admins=bob,joe,lily
The server doesn't require or check at startup that the named accounts all
already exist, this is just a comma-separated list of names.

In case an admin account password is lost, there's a rudimentary password-reset feature:
Run JSettlersServer with the usual DB parameters and --pw-reset username, and you will be
prompted for username's new password. This command can be run while the server is up.
It will reset the password and exit, won't start a JSettlersServer.


Development and Compiling
-------------------------

Source code for JSettlers is available via tarballs and github.
See the project website at http://nand.net/jsettlers/devel/
or http://sourceforge.net/projects/jsettlers2/
for details. Patches against the latest version may be submitted there
or at https://github.com/jdmonin/JSettlers2 .

For more information on compiling or developing JSettlers, see README.developer.
That readme also has information about translating jsettlers to other languages,
see the "I18N" section.

JSettlers is licensed under the GNU General Public License.  Each source file
lists contributors by year.  A copyright year range (for example, 2007-2011)
means the file was contributed to by that person in each year of that range.
See individual source files for the GPL version and other details.

BCrypt.java is licensed under the "new BSD" license, and is copyright
(C) 2006 Damien Miller; see BCrypt.java for details.  jBCrypt-0.3.tar.gz
retrieved 2012-08-14 from http://www.mindrot.org/projects/jBCrypt/

org.fedorahosted.tennera.antgettext.StringUtil is licensed under the
"Lesser GPL" (LGPL) license, and is from the JBoss Ant-Gettext utilities.

The hex and port images were created by Jeremy Monin, and are licensed
Creative Commons Attribution Share Alike (cc-by-sa 3.0 US) or Creative
Commons Attribution (CC-BY 3.0 US); see each image's gif comments for details.
goldHex.gif is based on a 2010-12-21 CC-BY 2.0 image by Xuan Che, available at
http://www.flickr.com/photos/rosemania/5431942688/ , of ancient Greek coins.
