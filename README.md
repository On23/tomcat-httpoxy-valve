# Tomcat Httpoxy Vulnerability Patch

**Compatible with Tomcat 6, 7 and 8**

####Installation:

1. Put [httproxy-valve-t6.jar](https://github.com/On23/tomcat-httpoxy-valve/releases/download/1.0/httproxy-valve-t6.jar) into /lib of your Tomcat installation

2. add the line `<Valve className="ru.on23.tomcat.valve.httproxy.PoxyValve" />` in conf/server.xml (like after the AccessLogValve)

3. restart Tomcat

4. test it (should print OK if patch works properly):

```sh
#change 8080 in url to actual value
curl -qv 'http://localhost:8080/'  -H 'Proxy: any-proxy-server' 2>&1 | grep -q 'HTTP/1.1 400' && echo OK

```


## Tomcat Httpoxy Vulnerability ([Source](https://www.apache.org/security/asf-httpoxy-response.txt))


Apache Tomcat provides a CGI Servlet that allows to execute a CGI
script. The CGI Servlet isn't active in the configuration delivered by
the ASF and activating it requires the user to modify the web.xml delivered.

To mitigate "httpoxy" issues in CGI Servlet there are 3 possible ways:

### 1
Add a filter in the webapp that uses CGI scripts simple code to
reject the  requests with PROXY headers via 400 "bad request" error.
Map the filter in web.xml of the webapp. Code like the following will
allow that:
```java

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;

/*
 * Simple filter
 */
public class PoxyFilter implements Filter {

    protected FilterConfig filterConfig;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }


    public void destroy() {
        this.filterConfig = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws java.io.IOException,
                                                   ServletException {


        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        String poxy = req.getHeader("proxy");
        if (poxy == null) {
          // call next filter in the chain.
          chain.doFilter(request, response);
        } else {
          res.sendError(400);
        }
    }
}

```    


### 2
Add a global valve to reject requests with PROXY header, create a
PoxyValve.java with below content, compile it and put it in a jar and
put the jar in the lib installation of your tomcat. Add the line

    <Valve className="PoxyValve" />

in conf/server.xml (like after the
AccessLogValve) and restart Tomcat:

```java
    
import java.io.IOException;
import javax.servlet.ServletException;

import org.apache.catalina.valves.ValveBase;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;

public class PoxyValve
    extends ValveBase {

    public void invoke(Request request, Response response)
        throws IOException, ServletException {

        String poxy = request.getHeader("Proxy");
        if (poxy != null) {
            response.sendError(400);
            return;
        }
        getNext().invoke(request, response);
    }
}
```

### 3
Fix the CGIServlet code with the following patch and recompile
Tomcat and replace the catalina.jar by the produced one in you
installation and restart Tomcat:
```diff
+++
--- java/org/apache/catalina/servlets/CGIServlet.java   (revision 1724080)
+++ java/org/apache/catalina/servlets/CGIServlet.java   (working copy)
@@ -1095,7 +1095,8 @@
                 //REMIND: change character set
                 //REMIND: I forgot what the previous REMIND means
                 if ("AUTHORIZATION".equalsIgnoreCase(header) ||
-                    "PROXY_AUTHORIZATION".equalsIgnoreCase(header)) {
+                    "PROXY_AUTHORIZATION".equalsIgnoreCase(header) ||
+                    "PROXY".equalsIgnoreCase(header)) {
                     //NOOP per CGI specification section 11.2
                 } else {
                     envp.put("HTTP_" + header.replace('-', '_'),
+++
```
A mitigation is planned for future releases of Tomcat, tracked as
CVE-2016-5388, which will allow the user to prevent values like
HTTP_PROXY from being propagated to the CGI Servlet environment.
