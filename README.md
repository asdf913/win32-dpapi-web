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

### Run Argument(s)
<table>
<thead>
<tr><th>Argument</th><th>Sample&nbsp;Value</th><th>Remarks</th></tr>
</thead>
<tbody>
<tr><td rowspan="2">primaryLanguage</td><td>9</td><td rowspan="4"><a href="https://learn.microsoft.com/en-us/windows/win32/api/winnt/nf-winnt-makelangid">MAKELANGID macro (winnt.h) - Win32 apps | Microsoft Learn</a></td></tr>
<tr><td>LANG_ENGLISH</td>
<tr><td rowspan="2">subLanguage</td><td>1</td></tr>
<tr><td>SUBLANG_ENGLISH_US</td></tr>
</tbody>
</table>

### Screen shot
http://127.0.0.1:8080/cryptProtectData
<img width="1896" height="385" alt="Untitled" src="https://github.com/user-attachments/assets/b341819e-869b-412c-b103-a6d5d370cdcb" />

## Parameter
<table>
<caption>cryptUnprotectData</caption>
<thead>
<tr><th>Parameter</th><th>Sample&nbsp;Value</th><th>Possible&nbsp;Value(s)</th></tr>
</thead>
<tbody>
<tr><td rowspan="7">encoding</td><td rowspan="7">base64</td><td>base16</td></tr>
<tr><td>base32</td></tr>
<tr><td>base45</td></tr>
<tr><td>base58</td></tr>
<tr><td>base64</td></tr>
<tr><td>binary</td></tr>
<tr><td>hex</td></tr>
<tr><td>flags</td><td></td><td></td></tr>
<tr><td>entropy</td><td></td><td></td></tr>
<tr><td>description</td><td></td><td></td></tr>
</tbody>
</table>

## Command

### cryptProtectData

#### Empty Input
<pre>curl -X POST http://127.0.0.1:8080/cryptProtectData -H "Content-Type:text/plain"</pre>
<pre>AQAAANCMnd8BFdERjHoAwE/Cl+sBAAAADGlfyr9wSU2PAbkwpUQwggAAAAACAAAAAAAQZgAAAAEAACAAAAAK6uiIpJ5+1XznL7n9fob8u65/pHuxSSYCe9FdZ8KZCgAAAAAOgAAAAAIAACAAAACYrNRIctQfWLHo6SNM9ME5EkhS2YO5ooRZlTP3uYFHJxAAAACKU3trkt60oaySd3cPPUwVQAAAABU+W/T/KZqfrUqoLFWtL0/iO8tqn9Q+Ykw8fMBveo4sWOWvFqJZMHuU0jc5aIh6UxSLTjkz9i+PrjY+LCZYhrk=</pre>

#### Input
<pre>curl -X POST http://127.0.0.1:8080/cryptProtectData -H "Content-Type:text/plain" -d "Hello World"</pre>
<pre>AQAAANCMnd8BFdERjHoAwE/Cl+sBAAAADGlfyr9wSU2PAbkwpUQwggAAAAACAAAAAAAQZgAAAAEAACAAAAAXK9EvvOwclhkGQp9eic6u0834qdGqUZgZrbEbrDzOYQAAAAAOgAAAAAIAACAAAAD2uXi2g89aKzRhq2WvwJXpt2MvFMV+RC3lS+0zmst9XxAAAAC4tCTdQfOf4WJjkQo5U19GQAAAAGhE2dB/A/p2rM5Lhjgz9Sqtp3xWA5dRnX4BfJI8lcWB1USYHDfLJTei94JXNBOayUzbp/GGpXaTkbCy/B2OB9g=</pre>

#### Input with entropy
<pre>curl -X POST http://127.0.0.1:8080/cryptProtectData?entropy=password -H "Content-Type:text/plain" -d "Hello World"</pre>
<pre>AQAAANCMnd8BFdERjHoAwE/Cl+sBAAAADGlfyr9wSU2PAbkwpUQwggAAAAACAAAAAAAQZgAAAAEAACAAAADC87mOB3PLzi+Erf2O8d2NutnbfT/A4df9uX8WjxeqxAAAAAAOgAAAAAIAACAAAAAaVSiEMb5UZN1z+KaWhivLDfui/lmLjs5+WeA6EZ32lBAAAACayHFQJJ/J9m0tjrQjxH7TQAAAADoW5uZ3yckEF9wiGTOSPvUXU2gLWcorTdC6bD6klWZFQsD/fnaDfChE9P86HAFJHirvM0Z630ZOhV4fpJWD7aI=</pre>

### cryptUnprotectData

#### Input with entropy
<pre>curl -X POST http://127.0.0.1:8080/cryptUnprotectData?entropy=password -H "Content-Type:text/plain" -d "AQAAANCMnd8BFdERjHoAwE/Cl+sBAAAADGlfyr9wSU2PAbkwpUQwggAAAAACAAAAAAAQZgAAAAEAACAAAADC87mOB3PLzi+Erf2O8d2NutnbfT/A4df9uX8WjxeqxAAAAAAOgAAAAAIAACAAAAAaVSiEMb5UZN1z+KaWhivLDfui/lmLjs5+WeA6EZ32lBAAAACayHFQJJ/J9m0tjrQjxH7TQAAAADoW5uZ3yckEF9wiGTOSPvUXU2gLWcorTdC6bD6klWZFQsD/fnaDfChE9P86HAFJHirvM0Z630ZOhV4fpJWD7aI="</pre>
<pre>Hello World</pre>


## Reference

<table>
<tbody>
<tr><td><a href="https://learn.microsoft.com/en-us/windows/win32/api/dpapi/nf-dpapi-cryptprotectdata">CryptProtectData function (dpapi.h) - Win32 apps | Microsoft Learn</a></td></tr>
<tr><td><a href="https://learn.microsoft.com/en-us/windows/win32/api/dpapi/nf-dpapi-cryptunprotectdata">CryptUnprotectData function (dpapi.h) - Win32 apps | Microsoft Learn</a></td></tr>
</tbody>
</table>
