/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package py.com.personal.oa2.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ribeirov
 */
public class MsisdnValidator {
    
    private static final Pattern regexPatternLinea5959x = Pattern.compile("5959\\d{8}");
    
    private static final Pattern regexPatternLinea09x = Pattern.compile("09\\d{8}");
    
    private static final Pattern regexPatternLinea9x = Pattern.compile("9\\d{8}");

    public static TipoMsisdn validarMsisdn(String msisdn) {
        if(validarMsisdn(regexPatternLinea5959x, msisdn))
        	return TipoMsisdn.MSISDN_5959X;
        else if(validarMsisdn(regexPatternLinea9x, msisdn))
        	return TipoMsisdn.MSISDN_9X;
        else if(validarMsisdn(regexPatternLinea09x, msisdn))
        	return TipoMsisdn.MSISDN_09X;
        else
        	return TipoMsisdn.NINGUNO;
    }
    
    private static boolean validarMsisdn(Pattern p, String msisdn) {
    	Matcher m = p.matcher(msisdn);
        return m.matches();
    }
    
    public static String convertirMsisdn(String msisdn, TipoMsisdn tipoEntrada, TipoMsisdn tipoSalida) {
        if(tipoEntrada == TipoMsisdn.MSISDN_5959X) {
        	if(tipoSalida == TipoMsisdn.MSISDN_5959X)
        		return msisdn;
        	else if(tipoSalida == TipoMsisdn.MSISDN_9X)
        		return msisdn.substring(3);
        	else if(tipoSalida == TipoMsisdn.MSISDN_09X)
        		return "0" + msisdn.substring(3);
        	else
        		return msisdn;
        }
        else if(tipoEntrada == TipoMsisdn.MSISDN_9X) {
        	if(tipoSalida == TipoMsisdn.MSISDN_5959X)
        		return "595" + msisdn;
        	else if(tipoSalida == TipoMsisdn.MSISDN_9X)
        		return msisdn;
        	else if(tipoSalida == TipoMsisdn.MSISDN_09X)
        		return "0" + msisdn;
        	else
        		return msisdn;
        } else if(tipoEntrada == TipoMsisdn.MSISDN_09X) {
        	if(tipoSalida == TipoMsisdn.MSISDN_5959X)
        		return "595" + msisdn.substring(1);
        	else if(tipoSalida == TipoMsisdn.MSISDN_9X)
        		return msisdn.substring(1);
        	else if(tipoSalida == TipoMsisdn.MSISDN_09X)
        		return msisdn;
        	else 
        		return msisdn;
        } else
        	return msisdn;
    }

    public static String convertirMsisdn09x(String msisdn5959x) {
        return "0" + msisdn5959x.substring(3);
    }
    
    public enum TipoMsisdn {
    	MSISDN_5959X,
    	MSISDN_9X,
    	MSISDN_09X,
    	NINGUNO
    };
}