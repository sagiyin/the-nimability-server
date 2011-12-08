package edu.purdue.cs.voip.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.google.gson.Gson;

public class TestClient2 {
	Socket socket;
	BufferedReader in;
	private DataOutputStream out;
	private Scanner incoming;
	private PrintStream outgoing;

	public TestClient2(String host, int port) {
		try {
			socket = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			incoming = new Scanner(in);
			outgoing = new PrintStream(out);
		} catch (UnknownHostException e) {
			System.out.format("Unknown Host: %s", host);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.format("Failed to create socket.");
			e.printStackTrace();
		}
	}

	public void start() {
		ClientRequest request = new ClientRequest();
		request.setRequestType(Client.OP_REQUEST_CALL);
		request.setRequestTarget("128.10.25.222 8888");
		Gson gson = new Gson();

		outgoing.println(gson.toJson(request));
		outgoing.flush();

		while (true) {
			if (incoming.hasNextLine()) {
				String jsonString = incoming.nextLine();
				System.out.format("Received server response json:%s\n",
						jsonString);
				ServerResponse response = gson.fromJson(jsonString.toString(),
						ServerResponse.class);
				if (response.getResponseType().equals(Client.RESPONSE_LIST_ALL)) {
					for (String s : response.getListOfClients()) {
						System.out.println(s);
					}
				}else if(response.getResponseType().equals(Client.OP_REACH_CALLEE)){
					String callerIp = response.getRequestTarget();
					ClientRequest requestTmp = new ClientRequest();
					requestTmp.setRequestType(Client.OP_REQUEST_DECLINE);
					requestTmp.setRequestTarget(callerIp);
					outgoing.println(gson.toJson(requestTmp));
					outgoing.flush();
				} else if(response.getResponseType().equals( Client.OP_RESPONSE_CALL)){
			/*		if(response.getCalleeStatus()==(Client.CALLEE_STATUS_BUSY)) System.out.println("CALLEE IS BUSY");
					else if(response.getCalleeStatus()==(Client.CALLEE_STATUS_DECLINE)) System.out.println("CALLEE DECLINE");*/
					 if( ((Integer)response.getCalleeStatus()).equals(Client.CALLEE_STATUS_READY)) {
						System.out.println("CALLEE READY, connecting"); 
						String callerIp = response.getRequestTarget();
						ClientRequest requestTmp = new ClientRequest();
						requestTmp.setRequestType(Client.OP_REQUEST_CONNECTED);
						requestTmp.setRequestTarget(callerIp);
						outgoing.println(gson.toJson(requestTmp));
						outgoing.flush();
					}
					 else{System.out.println("FUCK");}
					/*else if(response.getCalleeStatus()==(Client.CALLEE_STATUS_NOT_EXIST)) System.out.println("CALLEE NOT EXIST");*/
				}
			}

		}

	}

	public static void main(String[] args) {
		TestClient testClient = new TestClient(args[0],
				Integer.parseInt(args[1]));
		testClient.start();
	}
}