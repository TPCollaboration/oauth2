package py.com.personal.oauth2.rest.client.dao;


import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

/**
 *  Implements http status codes not implemented by resteasy.
 *   
 * @author demian
 *
 */
public enum OA2Status implements StatusType{

	      /**
	       * 302 Found see {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3">HTTP/1.1 documentation</a>}.
	       */
	      FOUND(302, "Found");
	    		  
	      private final int code;
	      private final String reason;
	      private Family family;

	      OA2Status(final int statusCode, final String reasonPhrase)
	      {
	         this.code = statusCode;
	         this.reason = reasonPhrase;
	         switch (code / 100)
	         {
	            case 1:
	               this.family = Family.INFORMATIONAL;
	               break;
	            case 2:
	               this.family = Family.SUCCESSFUL;
	               break;
	            case 3:
	               this.family = Family.REDIRECTION;
	               break;
	            case 4:
	               this.family = Family.CLIENT_ERROR;
	               break;
	            case 5:
	               this.family = Family.SERVER_ERROR;
	               break;
	            default:
	               this.family = Family.OTHER;
	               break;
	         }
	      }

	      /**
	       * Get the class of status code
	       *
	       * @return the class of status code
	       */
	      public Status.Family getFamily()
	      {
	         return family;
	      }

	      /**
	       * Get the associated status code
	       *
	       * @return the status code
	       */
	      public int getStatusCode()
	      {
	         return code;
	      }

	      /**
	       * Get the reason phrase
	       *
	       * @return the reason phrase
	       */
	      public String getReasonPhrase()
	      {
	         return toString();
	      }

	      /**
	       * Get the reason phrase
	       *
	       * @return the reason phrase
	       */
	      @Override
	      public String toString()
	      {
	         return reason;
	      }

	      /**
	       * Convert a numerical status code into the corresponding Status
	       *
	       * @param statusCode the numerical status code
	       * @return the matching Status or null is no matching Status is defined
	       */
	      public static OA2Status fromStatusCode(final int statusCode)
	      {
	         for (OA2Status s : OA2Status.values())
	         {
	            if (s.code == statusCode)
	            {
	               return s;
	            }
	         }
	         return null;
	      }
}
