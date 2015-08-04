/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package qa.utils.mxml;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;

import flex.messaging.log.Log;
import flex.ant.HtmlWrapperTask;
import flex.ant.MxmlcTask;
import flex.ant.config.NestedAttributeElement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class MXMLCServlet extends HttpServlet
{
    
    private static final long serialVersionUID = 2788674952911587822L;
    private static final String DEBUG_LOG_CATEGORY = "QA.MXMLC.SERVLET";
    
    private String servicesFilePath;
    private String flexConfigPath;
    private String contextRealPath;
    private String sdkHomePath;
    private boolean mxmlcExists = false;
    private boolean antDebug = false;
    
    @Override
    public void init() throws ServletException
    {
        super.init();
        contextRealPath = getServletContext().getRealPath("/");
        
        String _adebug = getInitParameter("ant-debug");
        if(_adebug != null)
            antDebug = new Boolean(_adebug).booleanValue();
                
        flexConfigPath = contextRealPath + "WEB-INF/flex/flex-config.xml";
        
        servicesFilePath = getInitParameter("services-config");
        if(servicesFilePath == null)
            servicesFilePath = contextRealPath + "WEB-INF/flex/services-config.xml";
        
        String _flexHome = getInitParameter("FLEX_HOME");
        if(_flexHome != null)
        {
            // absolute path to FLEX_HOME
            if(findMxmlc(_flexHome))
            {
                sdkHomePath = _flexHome;
                mxmlcExists = true;
            }
            // relative path to FLEX_HOME
            else if(findMxmlc(contextRealPath + _flexHome))
            {
                sdkHomePath = contextRealPath + _flexHome;
                mxmlcExists = true;
            }
            else
                logDebug("FLEX_HOME " + _flexHome + " couldn't be found.  Webtier compiler won't be available");
        }
        else
            logDebug("FLEX_HOME not set, Webtier compiler won't be available");
        // if FLEX_HOME not found, delay the error in case nobody is using the webtier
    }
    
    private boolean findMxmlc(String path)
    {
        return new File(path + File.separator + "bin" + File.separator + "mxmlc").exists();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String requestFile = req.getServletPath();
        if (requestFile.endsWith(".swf"))
        {
            File swfFile = new File(getServletContext().getRealPath(req.getServletPath()));
            
            writeSwfFile(resp, swfFile);
            return;
        }
        PrintWriter out = new PrintWriter(resp.getOutputStream());

        if (mxmlcExists)
        {
            String fileLocationPath = getServletContext().getRealPath(req.getServletPath().substring(0, req.getServletPath().lastIndexOf('/')+1));
            boolean noEndingSlash = fileLocationPath.charAt(fileLocationPath.length() - 1) != File.separatorChar;
            String fileName = req.getServletPath().substring(req.getServletPath().lastIndexOf('/')+1);
            String fileNameWithoutExt = fileName.substring(0,fileName.indexOf(".mxml"));
            String fileLocationPathSlashEnding = fileLocationPath + (noEndingSlash ? File.separator : "");
            
            Boolean debug = "true".equals(req.getParameter("debug")) ? true : false;
           
            File mxmlFile = new File(fileLocationPathSlashEnding + fileName);
            File swfFile;
            File htmlFile;
            
            if(mxmlFile.exists())
            {
                swfFile = new File(fileLocationPathSlashEnding + fileNameWithoutExt + ".swf");
                htmlFile = new File(fileLocationPathSlashEnding + fileNameWithoutExt + ".html");

                if (swfFile.exists())
                {
                    swfFile.delete();
                }
                if (htmlFile.exists())
                {
                    htmlFile.delete();
                }
            }
            else
            {
                // mxml file not found.  shouldn't be here
                throw new IOException(fileName + " not found");
            }
            Project project = new Project();
            try
            {
                project.setName("WebTier Compiler");
                project.setBaseDir(new File(contextRealPath));
                project.setProperty("FLEX_HOME", sdkHomePath);
                project.setProperty("serviceConfig", servicesFilePath);
                project.setProperty("flexConfig", flexConfigPath);   
                project.setProperty("mxmlFileLocation", fileLocationPathSlashEnding);
                project.setProperty("mxmlFile", fileNameWithoutExt);                
                project.setProperty("debug", debug.toString());
                project.setProperty("contextRoot", req.getContextPath());
                project.addBuildListener(getLogListener());
                
                project.init();
                // setup the project
                addMXMLCTarget(project);
                // compile the mxml
                project.executeTarget("compile");
                if (swfFile.exists() && htmlFile.exists())                    
                    writeWrapperHtml(resp, out, htmlFile);
                else
                {
                    out.print("<h1>Internal error.</h1><br/>Generated swf and wrapper couldn't be found.</html>");
                    out.flush();
                }
            }
            catch (Exception e)
            {
                out.print("<h1>Failed to compile.</h1><br/>Please see server console log for detailed error message.</html>");
                out.flush();
                project.fireBuildFinished(e);
                //e.printStackTrace();
            }
        }
        else
        {
            out.print("<h1>FLEX SDK not found</h1><br></html>");
            out.flush();
        }
    }
    
    // send the html content to the output
    private void writeWrapperHtml(HttpServletResponse resp, PrintWriter out, File htmlFile) throws IOException
    {
        resp.addHeader("Cache-Control","no-cache");
        resp.setContentType("text/html");
        FileInputStream fis = null;
        StringBuffer buffer = new StringBuffer();
        try
        {
            fis = new FileInputStream(htmlFile);
            int c;
            while ((c = fis.read()) > -1)
            {
                buffer.append((char) c);
            }
        }
        finally
        {
            try { fis.close();} catch(Exception e) {}
        }
        out.print(buffer.toString());
        resp.setContentLength(buffer.length());
        out.flush();        
    }

    private void writeSwfFile(HttpServletResponse resp,  File swfFile) throws IOException
    {
        if (swfFile.exists())
        {
            OutputStream out = resp.getOutputStream();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            FileInputStream fis = null;
            resp.addHeader("Cache-Control","no-cache");
            resp.setContentType("application/x-shockwave-flash");
            try
            {
                fis = new FileInputStream(swfFile);
                int b;
                while ((b=fis.read()) > -1)
                {
                    bytes.write(b);
                }
            }
            finally
            {
                try { fis.close();} catch(Exception e) {}
            }
            resp.setContentLength(bytes.size());
            out.write(bytes.toByteArray());
            out.flush();
        }
        else
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    // add mxmlc and html wrapper tasks to the project
    private void addMXMLCTarget(Project project)
    {
        String mxmlFile = project.getProperty("mxmlFile");
        String mxmlFileLocation = project.getProperty("mxmlFileLocation");
        
        Target target = new Target();
        target.setName("compile");
        project.addTarget("compile", target);
        
        MxmlcTask mxmlcTask = new MxmlcTask();
        mxmlcTask.setTaskName("mxmlc");
        mxmlcTask.setFile(mxmlFileLocation + mxmlFile + ".mxml");
        mxmlcTask.setFork(true);
        
        mxmlcTask.setDynamicAttribute("services", servicesFilePath);
        mxmlcTask.setDynamicAttribute("keep-generated-actionscript", "false");
        mxmlcTask.setDynamicAttribute("debug", project.getProperty("debug"));
        mxmlcTask.setDynamicAttribute("context-root", project.getProperty("contextRoot"));
        ((NestedAttributeElement)mxmlcTask.createDynamicElement("load-config")).setDynamicAttribute("filename", flexConfigPath);
        
        mxmlcTask.setProject(project);
        target.addTask(mxmlcTask);  
        
        HtmlWrapperTask htmlTask = new HtmlWrapperTask();
        htmlTask.setTaskName("html-wrapper");
        htmlTask.setTitle( mxmlFile+ " Flex Application");
        htmlTask.setFile(mxmlFile +".html");
        htmlTask.setHeight("100%");
        htmlTask.setWidth("100%");
        htmlTask.setApplication(mxmlFile + " app");
        htmlTask.setSwf(mxmlFile);
        htmlTask.setOutput(mxmlFileLocation);
        
        htmlTask.setProject(project);
        target.addTask(htmlTask);
    }
        
    // create a build listener for log output and use System out and err.
    // since the actual compile info is sent to the output stream,
    // message from error stream isn't helpful at all.  Also you
    // don't want to send the output stream to the browser either.
    // Ideally write your Listener to redirect the output.
    // TODO: enhance the logger and output failure to the browser 
    private BuildListener getLogListener()
    {
        DefaultLogger consoleLogger = new DefaultLogger();
        
        consoleLogger.setErrorPrintStream(System.err);
        consoleLogger.setOutputPrintStream(System.out);
        if(antDebug)
            consoleLogger.setMessageOutputLevel(Project.MSG_DEBUG);
        else 
            consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
        return consoleLogger;
    }
    
    // debug log
    private void logDebug(String s)
    {
        Log.getLogger(DEBUG_LOG_CATEGORY).debug(s);
    }
}