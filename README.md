# win32-dpapi-web

## Build
<pre>mvn clean package</pre>

## Run
<pre>java -jar win32-dpapi-web-0.0.1-SNAPSHOT.jar</pre>
<pre>INFO::main: Logging initialized @180ms to org.eclipse.jetty.util.log.StdErrLog
INFO:oejs.Server:main: jetty-9.4.58.v20250814; built: 2025-08-14T02:28:49.637Z; git: 8f1440587e9e4ae7db3d74cf205643f3d707148d; jvm 17.0.19+10
INFO:oejs.session:main: DefaultSessionIdManager workerName=node0
INFO:oejs.session:main: No SessionScavenger set, using defaults
INFO:oejs.session:main: node0 Scavenging every 600000ms
INFO:oejsh.ContextHandler:main: Started o.e.j.s.ServletContextHandler@475e586c{/,null,AVAILABLE}
INFO:oejs.AbstractConnector:main: Started ServerConnector@90f6bfd{HTTP/1.1, (http/1.1)}{0.0.0.0:8080}
INFO:oejs.Server:main: Started @704ms</pre>

### Parameter
<table>
<thead>
<tr><th>Parameter</th><th>Sample&nbsp;Value</th><th>Remarks</th></tr>
</thead>
<tbody>
<tr><td rowspan="2">primaryLanguage</td><td>9</td><td rowspan="4"><a href="https://learn.microsoft.com/en-us/windows/win32/api/winnt/nf-winnt-makelangid">MAKELANGID macro (winnt.h) - Win32 apps | Microsoft Learn</a></td></tr>
<tr><td>LANG_ENGLISH</td>
<tr><td rowspan="2">subLanguage</td><td>1</td></tr>
<tr><td>SUBLANG_ENGLISH_US</td></tr>
</tbody>
</table>
