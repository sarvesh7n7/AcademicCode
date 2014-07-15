package encrymsgpassing;


import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServerNode 
{
	static Map<String,String> hm=new HashMap<>();
	
	public static void main(String[] args) throws Exception
	{
		
		String clientSentence;
        String capitalizedSentence;
        ServerSocket welcomeSocket = new ServerSocket(6799);

        Socket connectionSocket;
        
        DataOutputStream outToClient;
        DataInputStream incl;
        EncryptFunctions E = new EncryptFunctions();
        hm = E.createHashMap("D:/srkn/userpwd.txt");
        
       /* byte[]        keycs = new byte[] {
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
                };  */
        byte[] keycs = new byte[16];
        
       // byte[] keycs = new byte[16];
        
        byte[]        keyas = new byte[] {
       		 0x00, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04,
       		 0x05, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
       		 0x05, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
                };
        
        byte[] keyBytes = new byte[] {
	             0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
	             0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
	             0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };

     
			connectionSocket = welcomeSocket.accept();
			
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			incl = new DataInputStream(connectionSocket.getInputStream());
			
			byte [] fromaccess = new byte[40000];
			int noofby = 0;                                   //server receives encrypted R from access node and decrypts it and displays
	        noofby = incl.read(fromaccess);
	        byte[] fromsaccess1 = new byte[noofby];
	 
	        System.arraycopy(fromaccess, 0, fromsaccess1, 0, noofby);
	          
	        System.out.println(E.asHex(fromsaccess1));
	       
	        String decrypt_ran_num = E.decryptfuncstr(keyBytes, fromsaccess1);
	        
	        System.out.println(decrypt_ran_num);
	      
	
			  System.out.println("Decrypted random number is:"+decrypt_ran_num);
			  int ran1=Integer.parseInt(decrypt_ran_num); // Converting R from string to int
			  Integer upated_ran = (ran1 + 1);  // R=R+1
			  String updated_ran1 = upated_ran.toString();
			  byte[] encrypt_updated_ran1=E.encryptfunc(keyBytes, updated_ran1); // encrypting R+1
			  outToClient.write(encrypt_updated_ran1);
			  
			  
		 while(true)
	      {	  
				  connectionSocket = welcomeSocket.accept();
					
					outToClient = new DataOutputStream(connectionSocket.getOutputStream());
					incl = new DataInputStream(connectionSocket.getInputStream());
			  
			  fromaccess = new byte[40000];
		      noofby = 0;                                   
		      noofby = incl.read(fromaccess);
		      byte[] fromaccess1 = new byte[noofby];  
		      System.arraycopy(fromaccess, 0, fromaccess1, 0, noofby);
		      String fromac = E.decryptfuncstr(keyas, fromaccess1);
			  System.out.println(fromac);
			  String fromacs[] = fromac.split("#");
			  keycs = E.givemdHash(fromacs[1]);
			  
		      if(fromacs[0].equals("validation"))
		      { 
				  fromaccess = new byte[40000];
			      noofby = 0;                                   
			      noofby = incl.read(fromaccess);
			      fromaccess1 = new byte[noofby];  
			      System.arraycopy(fromaccess, 0, fromaccess1, 0, noofby);
			      byte[] fromClientdata = E.decryptfuncbyte(keyas, fromaccess1);
			      
			      clientSentence = E.decryptfuncstr(keycs, fromClientdata);
			      
			      System.out.println(clientSentence);
			      
			      String userpwd[] = clientSentence.split("#");
			      keycs = E.givemdHash(userpwd[0]);   
			
			 
			      if((hm.containsKey(userpwd[0]))&&(hm.get(userpwd[0]).equals(userpwd[1])))		
			      	{
				  		System.out.println("correct usename and pwd");
				  		byte [] outtoac = E.encryptfunc(keyas, "authentic");
				  		outToClient.write(outtoac);
			  		
			      	}
			      	   
			     else
			     	{
				    	System.out.println("username and pwd missmatch");
				    	byte [] outtoac = E.encryptfunc(keyas, "not authentic");
				  		outToClient.write(outtoac);
				  		continue;
			    	
			     	}
			 
			      
		      }
		     
			  
		      int rantoser=E.getRandomNumbersmall();
		        String rannum = ""+rantoser;
		        System.out.println("the randm number:"+rannum);
		      System.out.println(rannum.length());
		      while(true)
		         {
		    	  	connectionSocket = welcomeSocket.accept();
		            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		    		                     
		            incl = new DataInputStream(connectionSocket.getInputStream());
		     
		        
		            byte[] fromclient = new byte[40000];
		            noofby = 0;
		            noofby = incl.read(fromclient);
		            
		            byte[] fromclient1 = new byte[noofby];
		            
		            System.arraycopy(fromclient, 0, fromclient1, 0, noofby);
		            
		            System.out.println("From Access: "+E.asHex(fromclient1));
		            
		            byte[] fromClientdata = E.decryptfuncbyte(keyas, fromclient1);
		            int cldataby = fromClientdata.length;
		            System.out.println(cldataby);
		            byte[] hashval = new byte[32];
		            byte[] seqnum = new byte[4];
		            byte[] fromcldata = new byte[cldataby-32-4];
		            
		            System.arraycopy(fromClientdata, 0, fromcldata, 0, cldataby-32);
		            System.arraycopy(fromClientdata, cldataby-32, hashval, 0, 32);
		            clientSentence = E.decryptfuncstr(keycs, fromcldata);
		           
		           System.out.println("Received from client: "+clientSentence);
		            byte[] clienthash = E.giveHash(clientSentence);
		            System.out.println(E.asHex(clienthash));
		            System.out.println(E.asHex(hashval));
		            
		            if(E.asHex(clienthash).equals(E.asHex(hashval)))
		            {
		     
			            if(clientSentence.equalsIgnoreCase("exit"))	
			            {
			            	String ex = "EXIT";
			            	byte[] enex = E.encryptfunc(keyas, ex);
			            	outToClient.write(enex);
			            	byte[] encrypt = E.encryptfunc(keycs,"GOOD BYE");
			            	byte[] outToaccess = E.encryptfunc(keyas,encrypt);
			            	outToClient.write(outToaccess);
			            	break;
			            }
			            else{
			            	String ex = "NOTEXIT";
			            	byte[] enex = E.encryptfunc(keyas, ex);
			            	outToClient.write(enex);
			            	capitalizedSentence = clientSentence.toUpperCase();
			            	byte[] encrypt = E.encryptfunc(keycs,capitalizedSentence);
			            	byte[] outToaccess = E.encryptfunc(keyas,encrypt);
			            	outToClient.write(outToaccess);
			            	outToClient.flush();
			            }
		            }
		            
			        connectionSocket.close();
		         }
			  
      } 
        
		
	}
}
