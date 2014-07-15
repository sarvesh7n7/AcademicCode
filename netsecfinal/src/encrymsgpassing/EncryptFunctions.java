package encrymsgpassing;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

	
	public class EncryptFunctions
	{
		static Map<String,String> hm=new HashMap<>();
		private static String stampbytes;
   
		public EncryptFunctions()
		{
			
		}
		
	    public byte[] encryptfunc(byte[] key, String message) 
	    {
	    	byte[] encrypted = null;
	    	
	    	try 
	    	{
		            
	            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
	           
	            // initialize the cipher for encrypt mode
	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

	           
	            // encrypt the message
	            encrypted = cipher.doFinal(message.getBytes());
	          

	        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException ex) 
	        {
	            ex.printStackTrace();   
	        }
	    	return encrypted;
	    
	    }
	    
	    public static byte[] encryptfunc(byte[] key, byte[] message) 
	    {
	    	byte[] encrypted = null;
	    	
	    	try 
	    	{
	                   
	            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
	           
	            // initialize the cipher for encrypt mode
	            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

	           
	            // encrypt the message
	            encrypted = cipher.doFinal(message);
	  

	        } catch (GeneralSecurityException ex) 
	        {
	            ex.printStackTrace();   
	        }
	    	return encrypted;
	    
	    } 
	    	
	    
		public String decryptfuncstr(byte[] key, byte[] encrypted)
		{
			String plainText = null;
			try
			{
			
			SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");  
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	        // initialize the cipher for decryption
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	
	        // decrypt the message
	        byte[] decrypted = cipher.doFinal(encrypted);
	        plainText = new String(decrypted);
			}catch(GeneralSecurityException ex)
			{
				ex.printStackTrace();   
			}
	     return plainText;
	    }
	
		 public static byte[] decryptfuncbyte(byte[] key, byte[] encrypted)
			{
				String plainText = null;
				byte[] decrypted = null;
				try
				{
				
				SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");  
		    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		    // initialize the cipher for decryption
		    cipher.init(Cipher.DECRYPT_MODE, skeySpec);

		    // decrypt the message
		     decrypted= cipher.doFinal(encrypted);
				}catch(GeneralSecurityException ex)
				{
					ex.printStackTrace();   
				}
		 return decrypted;
		}
	    
	    // Turns array of bytes into string
	     
	   
	    public static String asHex(byte buf[]) {
	        StringBuilder strbuf = new StringBuilder(buf.length * 2);
	        int i;
	        for (i = 0; i < buf.length; i++) 
	        {
	            if (((int) buf[i] & 0xff) < 0x10)
	            {
	                strbuf.append("0");
	            }
	            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
	        }
	        return strbuf.toString();
	    }
	    
	    public static byte[] giveHash(byte[] hashof)
		 {
			 byte[] digest = null; 
			 try
				{
					
					MessageDigest md = MessageDigest.getInstance("SHA256");
					digest = md.digest(hashof);
				}catch(Exception e){ e.printStackTrace();}
				
				return digest;
		 }
	    
	    public static byte[] giveHash(String hashof)
	    {
	   	 
	   	 byte[] digest = null; 
	   	 try
	   		{
	   			
	   			MessageDigest md = MessageDigest.getInstance("SHA256");
	   			digest = md.digest(hashof.getBytes());
	   		}catch(Exception e){ e.printStackTrace();}
	   		
	   		return digest;
	    }
	    
	    public static byte[] givemdHash(String hashof)
	    {
	   	 
	   	 byte[] digest = null; 
	   	 try
	   		{
	   			
	   			MessageDigest md = MessageDigest.getInstance("MD5");
	   			digest = md.digest(hashof.getBytes());
	   		}catch(Exception e){ e.printStackTrace();}
	   		
	   		return digest;
	    }
	    
	    public static int getRandomNumber() {

			int min=987654321;
			int max=1234567890;
			Random ran = new Random();
			int randomNumber = ran.nextInt((max + 1) - min) + min;
			return randomNumber;
			//System.out.println("Random number generated is:"+randomNumber);
		}
	    
	    public static int getRandomNumbersmall() {

			int min=1111;
			int max=4444;
			Random ran = new Random();
			int randomNumber = ran.nextInt((max + 1) - min) + min;
			return randomNumber;
			//System.out.println("Random number generated is:"+randomNumber);
		}
	    
	    public static Map<String,String> createHashMap(String url)
	    {
	    	
	    	
	    	String str1[] = null;
	    	  
	    	try
	    	{
	    	  // Open the file that is the first
	    	  // command line parameter
	    	  FileInputStream fstream = new FileInputStream(url);
	    	 
	    	 
	    	  // Get the object of DataInputStream
	    	  DataInputStream in = new DataInputStream(fstream);
	    	  BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    	 
	    	 
	    	  String strLine;
	    	  //String str1[]=new String[100];
	    	  //Read File Line By Line
	    	  while ((strLine = br.readLine()) != null) 
	    	  {
	    	  // Print the content on the console
	    	     
	    	      str1 = strLine.split(" ");
	    	     
	    	     
	    	      hm.put(str1[0],str1[1]);
	    	           
	    	      //System.out.println("hash map entries are:"+hm);
	    	  }
	    	 
	    	 
	         Iterator it = hm.entrySet().iterator();
	    	  while (it.hasNext())
	    	  {
	    	           
	   	          Map.Entry pairs = (Map.Entry)it.next();
	              System.out.println(pairs.getKey() + " = " + pairs.getValue());
	    	               	           
	    	           
	    	   }
	    	       
	    	  //Close the input stream
	    	  in.close();
	    	}
	    	  catch (Exception e)
	        {//Catch exception if any
	    	      System.err.println("Error: " + e.getMessage());
	    	      }
	    	  return hm;
	    }
	    
	    
	    public static String stamp()
		{
			EncryptFunctions E = new EncryptFunctions();
		    
		    Calendar ca=Calendar.getInstance();
		    
		    String y=((Integer)ca.get(Calendar.YEAR)).toString();
		    String m=((Integer)ca.get(Calendar.MONTH)).toString();
		    String d=((Integer)ca.get(Calendar.DATE)).toString();
		    String h=((Integer)ca.get(Calendar.HOUR_OF_DAY)).toString();
		    String mi=((Integer)ca.get(Calendar.MINUTE)).toString();
		    String sec=((Integer)ca.get(Calendar.SECOND)).toString();
		 
		    
		    
		    String stamp=y+m+d+h+mi+sec;
		 
		   return stamp;
		 }
	    
	    
	    public static void writeToFile(String text, String urlstr) 
	    {
            try 
            {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                                    urlstr), true));
                    bw.write(text);
                    bw.newLine();
                    bw.close();
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            }
            
            
	    }
	    
	    
	    public static String generateTicket(String username)
	    {
	    	String stamp = stamp();
	    	byte[] hashofname = giveHash(username);
	    	byte[] stampbytes = stamp.getBytes();
	    	byte[] namebytes = username.getBytes();
	    	int hashlen = hashofname.length;
	    	int stamplen = stampbytes.length;
	    	int namelen = namebytes.length;
	    	
	    	
	    	String namehash = new String(hashofname);
	    	String sym = "#";
	    	
	    	String concat = username+sym+namehash+sym+stamp;
	    	
	    //	System.out.println(concat);
	    //	System.out.println(concat.length());
	    
	    	
	    	return concat;
	    	        	
	    }
	    
	    public static boolean isticketAvailable(String username){
	    	if(username==null || username=="")return false;


	    	File f = new File("d:/srkn/"+username+".txt");

	    	if(f.exists()){
	    	System.out.println("File existed");
	    	return true;
	    	}else{
	    	System.out.println("File not found!");
	    	return false;
	    	}
	    	}
	    
	}


	