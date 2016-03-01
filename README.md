<a href="http://projectdanube.org/" target="_blank"><img src="http://projectdanube.github.com/xdi2/images/projectdanube_logo.png" align="right"></a>
<img src="http://projectdanube.github.com/xdi2/images/logo64.png"><br>

This is an XDI2 plugin to integrate [XDI2](http://github.com/projectdanube/xdi2) components with the
[Tor](https://www.torproject.org/) network.

### Information

* [Code Example](https://github.com/projectdanube/xdi2-tor/wiki/Code%20Example)
* [Server Configuration Example](https://github.com/projectdanube/xdi2-tor/wiki/Server%20Configuration%20Example)
* [Graph Factory Flags](https://github.com/projectdanube/xdi2-tor/wiki/Graph%20Factory%20Flags)

### How to build

First, you need to build the main [XDI2](http://github.com/projectdanube/xdi2) project.

After that, just run

    mvn clean install

To build the XDI2 plugin.

### How to run as standalone web application

    mvn clean install jetty:run -P war

Then access the web interface at

	http://localhost:9995/

Or access the server's status page at

	http://localhost:9995/xdi

Or use an XDI client to send XDI messages to

    http://localhost:9995/xdi/graph

### Maven Dependency

	<dependency>
	    <groupId>xdi2</groupId>
	    <artifactId>xdi2-tor</artifactId>
	    <version>${xdi2-tor-version}</version>
	    <scope>compile</scope>
	</dependency>

### Community

Google Group: http://groups.google.com/group/xdi2

IRC: irc://irc.freenode.net:6667/xdi
