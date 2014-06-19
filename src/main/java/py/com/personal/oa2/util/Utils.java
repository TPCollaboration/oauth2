package py.com.personal.oa2.util;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

import py.com.personal.oa2.util.MsisdnValidator.TipoMsisdn;

public class Utils {
			
	/**
	 * Retorna {@code true} si la cadena es nula o de longitud 0.
	 */
	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Obtiene la fecha actual
	 */
	public static Timestamp obtenerFechaActual() {
		Date fechaActual = new Date();
		Timestamp fechaBloqueo = new Timestamp(fechaActual.getTime());
		return fechaBloqueo;
	}

	
	public static Date addSeccondsToCurrentDate(int secconds){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, secconds);
		Date d = calendar.getTime();
		return d;
	}
	
	public static String validateUsername(String username){
		if(isNumeric(username)){
			TipoMsisdn result = MsisdnValidator.validarMsisdn(username);
			if(!result.equals(MsisdnValidator.TipoMsisdn.MSISDN_9X)){
				username = MsisdnValidator.convertirMsisdn(username, result, 
						MsisdnValidator.TipoMsisdn.MSISDN_9X);
				
				return username;
			}
		}
		return username;
	}
	
	public static boolean isNumeric(String str){
	  NumberFormat formatter = NumberFormat.getInstance();
	  ParsePosition pos = new ParsePosition(0);
	  formatter.parse(str, pos);
	  return str.length() == pos.getIndex();
	}
	
}
