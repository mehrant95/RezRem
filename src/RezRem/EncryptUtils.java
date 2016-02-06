package RezRem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class EncryptUtils {
	
   private static final String DEFAULT_ENCODING="UTF-16";
   
   private static final String key = "8U6Q)X4&lD-tU47xEe69sjGW@7)h@XgM_roZj%$s";
   
   private static final BASE64Encoder enc = new BASE64Encoder();
   
   private static final BASE64Decoder dec = new BASE64Decoder();

   public static String base64encode(String text) {
	   
	   try {
		   
		   String rez = enc.encode( text.getBytes( DEFAULT_ENCODING ) );
		   
		   return rez;
		   
	   } catch ( UnsupportedEncodingException e ) {
		   
		   return null;
		   
	   }
   
   }

   public static String base64decode(String text) {
	   
	   try {
		   
		   return new String(dec.decodeBuffer( text ), DEFAULT_ENCODING);
		   
	   } catch ( IOException e ) {
		   
		   return null;
		   
	   }
	   
   }
   
   public static String encode(String input) {
	   
	   return base64encode(xorMessage(input, key));
	   
   }
   
   public static String decode(String input) {
	  
	   return xorMessage(base64decode(input), key);
	   
   }
   
   public static String xorMessage(String message, String key) {
	   
	   try {
		   
		   if (message==null || key==null ) return null;
		   
		   char[] keys=key.toCharArray();
		   
		   char[] mesg=message.toCharArray();
		   
		   int ml=mesg.length;
		   
		   int kl=keys.length;
		   
		   char[] newmsg=new char[ml];
		   
		   for (int i=0; i<ml; i++)
			   newmsg[i]=(char)(mesg[i]^keys[i%kl]);
			   
		   mesg=null; keys=null;
		   
		   return new String(newmsg);
		   
	   } catch ( Exception e ) {
		   
		   return null;
		   
	   }
	   
   }
   
}