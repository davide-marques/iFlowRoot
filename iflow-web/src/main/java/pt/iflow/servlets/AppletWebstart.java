package pt.iflow.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import pt.iflow.api.presentation.PresentationManager;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.applet.AbstractAppletServletHelper;
import pt.iflow.documents.DocumentServiceServlet;

public class AppletWebstart
  extends HttpServlet
{
  private static final long serialVersionUID = 1L;
  public static final String REQUEST_FOR_APPLET = "REQUEST_FOR_APPLET";
  private Hashtable<String, RequestForApplet> requestPile;
  
  protected void service(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    ServletContext sc = req.getSession().getServletContext();
    if (sc.getAttribute("REQUEST_FOR_APPLET") == null) {
      sc.setAttribute("REQUEST_FOR_APPLET", new Hashtable());
    }
    super.service(req, resp);
  }
  
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    try
    {
      HttpSession session = DocumentServiceServlet.getSessionFixedForJNLP(req);
      UserInfoInterface userInfo = (UserInfoInterface)session.getAttribute("UserInfo");
      Gson gson = new Gson();
      String requestURL = req.getRequestURL().toString();
      String documentBaseURL = requestURL.replace("AppletWebstart", "DocumentService");
      String codebaseURL = requestURL.replace("/AppletWebstart", "");
      String checkRequestForAppletURL = requestURL.replace("AppletWebstart", "CheckRequestForApplet");
      
      AbstractAppletServletHelper helper = AbstractAppletServletHelper.getInstance();
      ArrayList<String> resources = new ArrayList();
      Iterator<String> iter = helper.dependenecies();
      while (iter.hasNext())
      {
        String name = (String)iter.next();
        resources.add(name);
      }
      Enumeration enp = req.getParameterNames();
      Hashtable htp = new Hashtable();
      htp.put("DOCUMENTBASEURL", documentBaseURL);
      htp.put("CHECKREQUESTFORAPPLET", checkRequestForAppletURL);
      htp.put("JSESSIONID", session.getId());
      while (enp.hasMoreElements())
      {
        String name = enp.nextElement().toString();
        String value = req.getParameter(name);
        htp.put(name, value);
      }
      if (htp.size() == 3)
      {
        String appletArgument = gson.toJson(htp);
        Hashtable hsSubstLocal = new Hashtable();
        hsSubstLocal.put("appletArgument", appletArgument);
        hsSubstLocal.put("codebaseURL", codebaseURL);
        hsSubstLocal.put("resources", resources);
        String appletWebStartJnlp = PresentationManager.buildPage(resp, userInfo, hsSubstLocal, "appletWebStart");
        
        byte[] ba = appletWebStartJnlp.getBytes();
        resp.setHeader("Content-Disposition", "attachment;filename=\"appletWebStart.jnlp\";");
        resp.setContentLength(ba.length);
        OutputStream out = resp.getOutputStream();
        out.write(ba);
        out.flush();
        out.close();
      }
      else
      {
        session.setAttribute("REQUEST_FOR_APPLET", new RequestForApplet(htp));
        return;
      }
    }
    catch (Exception e)
    {
      Logger.error("<unknown>", this, "doGet", "Could not generate applet jnlp file", e);
      resp.sendError(500, "Internal server error");
      return;
    }
  }
}
