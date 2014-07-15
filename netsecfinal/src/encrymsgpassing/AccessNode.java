package encrymsgpassing;

import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class AccessNode {

	static Map<String,String> hm=new HashMap<>();
	public static void main(String[] args) throws Exception
	{
		String clientSentence;
        String capitalizedSentence;
        ServerSocket fromclSocket = new ServerSocket(6797);
        Socket accessSocket;
        Socket connectionSocket;
        DataOutputStream outToClient;
        DataInputStream incl = null;
        DataInputStream inserver;
        DataOutputStream outToServer;
        
        EncryptFunctions E = new EncryptFunctions();
        
        hm = E.createHashMap("D:/srkn/access.txt");
        
        byte[]        keyac = new byte[] {
        		0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07               
                };
        
        byte[] keyBytes = new byte[] {
	             0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
	             0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
	             0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };

        byte[] keym = new byte[] {
        		0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
	             0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
	             0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
        
        byte[]        keyas = new byte[] {
       		 0x00, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04,
       		 0x05, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
       		0x05, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
        
        
       
        accessSocket = new Socket("localhost", 6799);
   	 		
        inserver = new DataInputStream(accessSocket.getInputStream());
        outToServer = new DataOutputStream(accessSocket.getOutputStream());
 
        Integer rantoser=0;
        System.out.println("Generating random R for authentication"); // R is being sent to server
        rantoser=E.getRandomNumber();
        String rantoser_num = rantoser.toString();
        System.out.println("Random number generated is:"+rantoser_num);
        
        System.out.println("string random sent to server:"+rantoser_num);
        
        byte[] encrypt_rantoser_num = E.encryptfunc(keyBytes,rantoser_num);
        System.out.println("The encrypted ran_num1"+E.asHex(encrypt_rantoser_num));
        outToServer.write(encrypt_rantoser_num);
        outToServer.flush();
	        
	     
	        
        byte []fromserver= new byte[40000];
        int noofby = 0;                                   //access receives content from server and decrypts it and displays
        noofby = inserver.read(fromserver);
        byte[] fromserver1 = new byte[noofby];
        
        System.arraycopy(fromserver, 0, fromserver1, 0, noofby);
        
        String ran_new_fromser=E.decryptfuncstr(keyBytes, fromserver1);
        System.out.println("R+1 sent by server to access node is"+ran_new_fromser);

        // authentcation done of server and access... 
	    while(true)
		{   
		        
		        
        	accessSocket = new Socket("localhost", 6799); 	 	
	        inserver = new DataInputStream(accessSocket.getInputStream());
	        outToServer = new DataOutputStream(accessSocket.getOutputStream());   
	        //for client authentication
	   	 	 
	         connectionSocket = fromclSocket.accept();
	 	 	 outToClient = new DataOutputStream(connectionSocket.getOutputStream());
	 	 	 incl = new DataInputStream(connectionSocket.getInputStream());
	        
		   	 noofby = 0;
	
	         byte[] fromclient = new byte[40000];
		     noofby = incl.read(fromclient);
		     byte[] fromclient1 = new byte[noofby];
		     System.arraycopy(fromclient, 0, fromclient1, 0, noofby);
		    
		     System.out.println("From Client: "+E.asHex(fromclient1));
		     
		     String clientDecrypt = E.decryptfuncstr(keyac, fromclient1);
		     System.out.println("form client: " +clientDecrypt);
	   	 	
		     String userreq[] = clientDecrypt.split("#");
	         
	    //   if(hm.containsKey(userreq[0]))
	    	//   System.out.println("user is present in the system no need for ticket");
		     
		     
		     /// procedure for ticket generation...
	         // will be done with sirram
	         if(userreq[1].equals("requestticket"))
	         {
	        	 if(hm.containsKey(userreq[0]))
	        	 {
	        		 String tocl = "you have the ticket, use that";
	        		 byte[] clientEncrypt = E.encryptfunc(keyac, tocl);
		        	 outToClient.write(clientEncrypt);
		        	 continue;
	        	 }
	        	 
	        	 String tocl = "server authentication";
	        	 byte[] clientEncrypt = E.encryptfunc(keyac, tocl);
	        	 outToClient.write(clientEncrypt);
	        	 
	        	 String val = "validation";
	        	 String toserver = val+"#"+userreq[0];
	        	 byte[] enctoserver = E.encryptfunc(keyas,toserver);
	        	 outToServer.write(enctoserver);
	        	 outToServer.flush();
	        	 
	
	            fromclient = new byte[40000];
	            noofby = incl.read(fromclient);
	            fromclient1 = new byte[noofby];
	   	     	System.arraycopy(fromclient, 0, fromclient1, 0, noofby);
	   	     	
	   	     	byte[] clientDecryptbyte = E.decryptfuncbyte(keyac, fromclient1);
	             
	   	     	byte[] clientEncrypt1 = E.encryptfunc(keyas, clientDecryptbyte);
	   	     
	   	     	outToServer.write(clientEncrypt1);
	         
	   	     	int noofby1 = 0;
	         
	         	noofby1 = inserver.read(fromserver);
	         	fromserver1 = new byte[noofby1];
	         
	         	System.arraycopy(fromserver, 0, fromserver1, 0, noofby1);
	        
	         	String formServerString = E.decryptfuncstr(keyas, fromserver1);
	         	System.out.println(formServerString);
	         	
	         	
	         	if(formServerString.equals("authentic"))
	         	{
	         		//call procedure for generating the ticket and store the ticket for later use
	         		
	         		String ticket  = E.generateTicket(userreq[0]);
	         		String timestamp = E.stamp();
	         		     		
	         		E.writeToFile(userreq[0]+ " "+timestamp, "D:/srkn/access.txt");
	         		hm = E.createHashMap("D:/srkn/access.txt");
	         		clientEncrypt1 = E.encryptfunc(keym, ticket);
	         		clientEncrypt = E.encryptfunc(keyac, clientEncrypt1);
	         		outToClient.write(clientEncrypt);
	         		
	         		
	         		
	         	}
	         	else
	         	{
	         		String notaut = "not authentic";
	         		clientEncrypt = E.encryptfunc(keyac, notaut);
	         		outToClient.write(clientEncrypt);
	         		continue;
	         	}
	         	
	         }
	         
	         else if(userreq[1].equals("ticket"))
	         {
	        	 // check for the ticket validation after recieving the ticket
	        	 System.out.println("its coming here..");
	        	 String msgpass = "msgpassing";
	    
	        	 String toserver = msgpass+"#"+userreq[0];
	        	 byte[] enctoserver = E.encryptfunc(keyas,toserver);
	        	 outToServer.write(enctoserver);
	        	 outToServer.flush();
	        	 
	        	 fromclient = new byte[40000];
	             noofby = incl.read(fromclient);
	             fromclient1 = new byte[noofby];
	    	     System.arraycopy(fromclient, 0, fromclient1, 0, noofby);
	    	     byte[] clientdec = E.decryptfuncbyte(keyac, fromclient1);
	    	     String dec = E.decryptfuncstr(keym, clientdec);
				 System.out.println(dec);
	    	     
				 String[] septic = dec.split("#");
				 String namehash =  E.asHex(E.giveHash(septic[0]));
				 String outhash = E.asHex(septic[1].getBytes());
				 
				 System.out.println(namehash.length());
				 System.out.println(outhash.length());
				 System.out.println(namehash);
				 System.out.println(outhash);
				 
				 if((hm.containsKey(septic[0]))&&(septic[2].equals(hm.get(septic[0]))))
				 {
								 
					 if(namehash.equals(outhash))
					 {
						 System.out.println("i dont believe this");
					 }
					 String tocl = "ticket validated";
		        	 byte[] clientEncrypt = E.encryptfunc(keyac, tocl);
		        	 outToClient.write(clientEncrypt);
						
				 }
				 else
				 {
					 String tocl = "ticket not validated";
		        	 byte[] clientEncrypt = E.encryptfunc(keyac, tocl);
		        	 outToClient.write(clientEncrypt);
		        	 continue;
				 }
				  
	    	     
	         }
		     
		     
	         while(true)
	         {
	        	 accessSocket = new Socket("localhost", 6799);
	        	 connectionSocket = fromclSocket.accept();
	             
	             outToClient = new DataOutputStream(connectionSocket.getOutputStream());
	             
	             incl = new DataInputStream(connectionSocket.getInputStream());
	             inserver = new DataInputStream(accessSocket.getInputStream());
	             
	             
	             outToServer = new DataOutputStream(accessSocket.getOutputStream());
	             
	             fromclient = new byte[40000];
	             fromserver = new byte[40000];
	              noofby = 0;
	             noofby = incl.read(fromclient);
	             fromclient1 = new byte[noofby];
	             System.arraycopy(fromclient, 0, fromclient1, 0, noofby);
	             
	         
	             
	             System.out.println("From Client: "+E.asHex(fromclient1));
	             
	             byte[] clientdec = E.decryptfuncbyte(keyac, fromclient1);
	             
	             System.out.println("From Client Decrypted by kac: "+E.asHex(clientdec));
	             byte[] clientEncrypt = E.encryptfunc(keyas, clientdec);
	             
	             System.out.println("encrypted to serer:"+E.asHex(clientEncrypt));
	             outToServer.write(clientEncrypt);
	            
	             int noofby1 = 0;
	             
	             noofby1 = inserver.read(fromserver);
	             fromserver1 = new byte[noofby1];
	             
	             System.arraycopy(fromserver, 0, fromserver1, 0, noofby1);
	           
	             String checkexit = E.decryptfuncstr(keyas, fromserver1);
	            
	             
	             noofby1 = 0;    
	             noofby1 = inserver.read(fromserver);
	             fromserver1 = new byte[noofby1];
	             
	             System.arraycopy(fromserver, 0, fromserver1, 0, noofby1);
	             
	             byte[] serverDecrypt = E.decryptfuncbyte(keyas, fromserver1);
	             byte[] serverEncrypt = E.encryptfunc(keyac, serverDecrypt);
	             System.out.println("From Server: "+E.asHex(fromserver1));
	             
	             outToClient.write(serverEncrypt);
	             connectionSocket.close();
	             accessSocket.close();
	             
	            if(checkexit.equals("EXIT"))
	            {
	            	break;
	            }
	         }
        }
	}

	
}
