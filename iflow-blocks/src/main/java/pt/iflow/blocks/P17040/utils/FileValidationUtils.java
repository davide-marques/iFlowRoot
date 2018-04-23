package pt.iflow.blocks.P17040.utils;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.UserInfoInterface;

public class FileValidationUtils {
    
	
	/**
     * <p>Checks if two dates are on the same day ignoring time.</p>
     * @param date1  the first date, not altered, not null
     * @param date2  the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is <code>null</code>
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }
    
    /**
     * <p>Checks if two calendars represent the same day ignoring time.</p>
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
    
    public static boolean isValidNif(String nif) {
        
        if(StringUtils.isEmpty(nif) || !NumberUtils.isNumber(nif) || nif.length() != 9)
          return false;
        
        int check=0;
        int checkDigit = nif.charAt(8)-'0';
        for(int f=0; f < 8; f++)
          check += (nif.charAt(f)-'0') * (9-f);
        int div = (check / 11);
        div = div * 11;
        check -= div;
        if(check == 0 || check == 1)
          check = 0;
        else
          check = 11 - check;
        
        return check == checkDigit;
        
      }
    
    public static boolean isValidDomainValue(UserInfoInterface userInfo, DataSource datasource, String domain, String value) throws SQLException{
    	if(value==null)
    		return true;
    	
    	return retrieveSimpleField(datasource, userInfo,
				"select * from {0} where codigo = {1} ", new Object[] {domain, value }).size() == 1 ;
    }
    
    public static boolean isValidIdEntEN008(UserInfoInterface userInfo, DataSource datasource, HashMap<String, Object> idEntValues) throws SQLException{
    	if (StringUtils.equalsIgnoreCase("" + idEntValues.get("type"), "i1")
				&& StringUtils.isAlpha(idEntValues.get("nif_nipc").toString()))
			return false;
    	return true;
	}
    
    public static boolean isValidIdEntEN010(UserInfoInterface userInfo, DataSource datasource, HashMap<String, Object> idEntValues) throws SQLException{
		if (StringUtils.equalsIgnoreCase("" + idEntValues.get("type"), "i1")
				&& !FileValidationUtils.isValidNif(idEntValues.get("nif_nipc").toString()))
			return false;
		return true;
    }
}