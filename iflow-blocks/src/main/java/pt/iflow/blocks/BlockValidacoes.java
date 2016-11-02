package pt.iflow.blocks;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

/**
 * <p>Title: BlockValidacoes</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class BlockValidacoes extends Block {
  public Port portIn, portOutOk, portOutError;

  private static final String sCOND_PREFIX = "cond";
  private static final String sMESG_PREFIX = "msg";


  public BlockValidacoes(int anFlowId,int id, int subflowblockid, String filename) {
    super(anFlowId,id, subflowblockid, filename);
    isCodeGenerator = true;
    hasInteraction = false;
    saveFlowState = true;
  }

  public Port[] getInPorts (UserInfoInterface userInfo) {
      Port[] retObj = new Port[1];
      retObj[0] = portIn;
      return retObj;
  }
  
  public Port getEventPort() {
      return null;
  }
  
  public Port[] getOutPorts (UserInfoInterface userInfo) {
    Port[] retObj = new Port[2];
    retObj[0] = portOutOk;
    retObj[1] = portOutError;
    return retObj;
  }

  /**
   * No action in this block
   * @return always 'true'
   */
  public String before(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }

  /**
   * No action in this block
   *
   * @param dataSet a value of type 'DataSet'
   * @return always 'true'
   */
  public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
    return true;
  }

  /**
   * Executes the block main action
   *
   * @param dataSet a value of type 'DataSet'
   * @return the port to go to the next block
   */
  public Port after(UserInfoInterface userInfo, ProcessData procData) {
    Port outPort = portOutOk;

    String login = userInfo.getUtilizador();
    StringBuffer logMsg = new StringBuffer();

    try {
      ////////////////////////////////////
      // Execute the code generated by the block
      ////////////////////////////////////
      StringBuffer sbError = new StringBuffer();
      String stmp = null;
      String stmp2 = null;
      boolean btmp = false;

      for (int i=0; (stmp = this.getAttribute(sCOND_PREFIX + i)) != null; i++) {

    	  // 	btmp = ConditionParser.evaluate(stmp,dataSet);
    	  try {
    		  btmp = procData.query(userInfo, stmp);
    	  }
    	  catch (Exception ei) {
    		  btmp = true;
    		  Logger.error(login,this,"after",
    				  "caught exception evaluation beanshell (assuming true): "
    				  + ei.getMessage());
    	  }

    	  Logger.debug(login,this,"after", stmp + " evaluated " + btmp);
    	  logMsg.append("Evaluated '" + stmp + "' to '" + btmp + "';");

    	  if (btmp) {
    		  try {
    			  stmp2 = procData.transform(userInfo, this.getAttribute(sMESG_PREFIX + i));
    		  }
    		  catch (Exception e1) {
    			  stmp2 = null;
    		  }
    		  if (stmp2 == null) {
    			  stmp2 = "Condição " + stmp + " não verificada.";
    		  }
    		  if (sbError.length() > 0) {
    			  sbError.append("<br/>");
    		  }
    		  sbError.append(stmp2);
    	  }
      }

      procData.clearError();
      if (sbError.length() > 0) {
    	  procData.setError(sbError.toString());
    	  outPort = portOutError;
      }
    }
    catch (Exception e) {
      Logger.error(login,this,"after","caught exception: " + e.getMessage());
      outPort = portOutError;
    }

    logMsg.append("Using '" + outPort.getName() + "';");
    Logger.logFlowState(userInfo, procData, this, logMsg.toString());
    return outPort;
  }

  public String getDescription (UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, true, "Validações");
  }

  public String getResult (UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, false, "Validações efectuadas");
  }

  public String getUrl (UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }
}
