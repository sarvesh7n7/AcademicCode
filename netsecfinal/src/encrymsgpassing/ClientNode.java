package encrymsgpassing;
import java.net.Socket;
import java.io.*;
import java.io.ObjectInputStream.GetField;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
public class ClientNode {


public static void main(String[] args) throws Exception
{
	Socket clientSocket=null;
	DataOutputStream outToServer;
	DataInputStream inserver;
	BufferedReader inFromUser;
	
	/* byte[] keycs = new byte[] {
	0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
	0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
	0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
	};*/
	byte[] keycs = new byte[16];
	
	byte[] keyac = new byte[] {
	0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
	0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
	0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07
	};
	
	
	
	EncryptFunctions E = new EncryptFunctions();
	try
	{
	
	
		String sentence = new String();
		String modifiedSentence;
		
		inFromUser = new BufferedReader( new InputStreamReader(System.in));
		System.out.println("Enter the username");
		String username=inFromUser.readLine();
		
		clientSocket = new Socket("localhost", 6797);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		inserver = new DataInputStream(clientSocket.getInputStream());
		
		boolean flag=false;
		flag=EncryptFunctions.isticketAvailable(username);
		
		String password;
		
		if(!flag)
		{
			System.out.println("Enter the password");
			password=inFromUser.readLine();
			String userrequest = new String(username+"#requestticket");
			String[] usrreq = userrequest.split("#");
			
			String filepath = null;
			filepath = "D:/srkn/"+usrreq[0]+".txt";
			System.out.println(filepath);
			
			keycs = E.givemdHash(usrreq[0]);
			System.out.println(E.asHex(keycs));
			byte[] encrypt = E.encryptfunc(keyac,userrequest);
			byte[] hashval = E.giveHash(userrequest);
			
			
			outToServer.write(encrypt);
			outToServer.flush();
			
			byte[] fromaccess = new byte[40000];
			int noofby = 0;
			noofby = inserver.read(fromaccess);
			byte[] fromaccess1 = new byte[noofby];
			
			System.arraycopy(fromaccess, 0, fromaccess1, 0, noofby);
			String accessSentence = E.decryptfuncstr(keyac, fromaccess1);
			System.out.println("from access: "+accessSentence);
			if(accessSentence.equals("server authentication"))
			{
			//encrypt username and password with server key and send that to access node..
			
			userrequest = username+"#"+password;
			String usern[] = userrequest.split("#");
			encrypt = E.encryptfunc(keycs,userrequest);
			byte[] toSendData = E.encryptfunc(keyac,encrypt);
			outToServer.write(toSendData);
			outToServer.flush();
			
			fromaccess = new byte[40000];
			noofby = 0;
			noofby = inserver.read(fromaccess);
			fromaccess1 = new byte[noofby];
			
			
			System.arraycopy(fromaccess, 0, fromaccess1, 0, noofby);
			byte[] ticketbyte = E.decryptfuncbyte(keyac, fromaccess1);
			String nonaut = new String(ticketbyte);
			
			
			if(nonaut.equals("not authentic"))
			{
				clientSocket.close();
				System.exit(0);
			}
			FileOutputStream foutstream = new FileOutputStream(filepath);
			DataOutputStream outf = new DataOutputStream(foutstream);
			outf.write(ticketbyte);
			outf.close();
			}
			else
			{
			
				clientSocket.close();
				System.exit(0);
				
			}
		}
		
		else
		{
			//here the client has the ticket so message will be send according to that
			String userrequest = new String(username+"#ticket");
			String[] usrreq = userrequest.split("#");
			keycs = E.givemdHash(usrreq[0]);
			
			String filepath = null;
			filepath = "D:/srkn/"+usrreq[0]+".txt";
			System.out.println(filepath);
			
			byte[] toSendData = E.encryptfunc(keyac,userrequest);
			outToServer.write(toSendData);
			outToServer.flush();
			
			FileInputStream finstream = new FileInputStream(filepath);
			DataInputStream inf = new DataInputStream(finstream);
			
			byte[] ticket =new byte[40000];
			int noofby = inf.read(ticket);
			
			byte[] ticket1 = new byte[noofby];
			
			System.arraycopy(ticket, 0, ticket1, 0, noofby);
			
			toSendData = E.encryptfunc(keyac,ticket1);
			outToServer.write(toSendData);
			outToServer.flush();
			
			
			byte[] fromaccess = new byte[40000];
			noofby = inserver.read(fromaccess);
			byte[] fromaccess1 = new byte[noofby];
			System.arraycopy(fromaccess, 0, fromaccess1, 0, noofby);
			String accessSentence = E.decryptfuncstr(keyac, fromaccess1);
			System.out.println("from access: "+accessSentence);
			
			if(accessSentence.equals("ticket validated"))
			{
				//i think the while logic will come here but not sure.
				System.out.println("good to go");
			}
			
			else if(accessSentence.equals("ticket not validated"))
			{
				clientSocket.close();
				System.exit(0);
			}
		
		}
		
		int seqnum = 0;
		String seqnumstr = "1111";
		byte[] seqnumby = seqnumstr.getBytes();
		int seqnumlen=0;
		
			while(true)
			{
				seqnumstr = ""+seqnum;
				seqnumby = seqnumstr.getBytes();
				seqnumlen = seqnumby.length;
				System.out.println(E.asHex(seqnumby));
				System.out.println(seqnumlen);
				clientSocket = new Socket("localhost", 6797);
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				
				inserver = new DataInputStream(clientSocket.getInputStream());
				
				System.out.println("enter the line:");
				sentence = inFromUser.readLine();
				
				byte[] encrypt = E.encryptfunc(keycs,sentence);
				byte[] hashval = E.giveHash(sentence);
				
				System.out.println("Hash Length: "+hashval.length);
				byte[] toServerData = new byte[encrypt.length+hashval.length+seqnumlen];
				
				System.arraycopy(encrypt, 0, toServerData, 0, encrypt.length);
				System.arraycopy(hashval, 0, toServerData, encrypt.length, hashval.length);
				System.arraycopy(seqnumby, 0, toServerData, encrypt.length+hashval.length, seqnumlen);
				System.out.println("enc1 Length: "+toServerData.length);
				
				byte[] toSendData = E.encryptfunc(keyac,toServerData);
				System.out.println("enc2 Length: "+toSendData.length);
				
				outToServer.write(toSendData);
				outToServer.flush();
				
				byte[] fromaccess = new byte[40000];
				int noofby = 0;
				noofby = inserver.read(fromaccess);
				
				byte[] fromaccess1 = new byte[noofby];
				
				
				System.arraycopy(fromaccess, 0, fromaccess1, 0, noofby);
				
				byte[] fromServerdata = E.decryptfuncbyte(keyac,fromaccess1);
				int serverby = fromServerdata.length;
				
	            byte[] fromserdata = new byte[serverby-4];
	            
	            System.arraycopy(fromServerdata, 0, fromserdata, 0, serverby-4);
	            System.arraycopy(fromServerdata, serverby-4, seqnumby, 0, 4);
	            
	            seqnumstr = new String(seqnumby);
	            seqnum = Integer.parseInt(seqnumstr);
	            seqnum = seqnum+1;
				modifiedSentence = E.decryptfuncstr(keycs, fromserdata);
				
				System.out.println("FROM SERVER: " + modifiedSentence);
				if(sentence.equalsIgnoreCase("exit")) break;
				clientSocket.close();
			
			}
		}
		
		
		catch(Exception es)
		{
			es.printStackTrace();
			clientSocket.close();
		}
		clientSocket.close();
	
	}

}
