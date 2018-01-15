package co.com.assist.node.consensus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import co.com.assist.node.blockchain.Block;
import co.com.assist.node.blockchain.Blockchain;

public class PeerHandler extends Thread {

	private Socket socket;

	public PeerHandler(Socket socket) {
		this.socket = socket;
	}

	public Socket getSocket() {
		return socket;
	}

	@Override
	public void run() {
		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			String json;
			while ((json = in.readLine()) != null) {
				JsonObject message = new JsonParser().parse(json).getAsJsonObject();
				System.out.println("Received message: " + message);

				MessageType type = MessageType.valueOf(message.getAsJsonPrimitive("type").getAsString());
				Gson gson = new GsonBuilder().create();

				if (type.equals(MessageType.RESPONSE_BLOCKCHAIN)) {
					JsonElement blockElement = message.get("data");
					Type typeOfMap = new TypeToken<List<Block>>() {
					}.getType();
					List<Block> receivedBlocks = new GsonBuilder().create().fromJson(blockElement, typeOfMap);
					Block latestBlockReceived = receivedBlocks.get(receivedBlocks.size() - 1);
					Block latestBlockHeld = Blockchain.getInstance().getLastBlock();

					if (latestBlockReceived.getIndex() > latestBlockHeld.getIndex()) {
						System.out.println("Blockchain possibly behind. We got: " + latestBlockHeld.getIndex()
								+ " Peer got: " + latestBlockReceived.getIndex());
						if (Blockchain.getInstance().addBlock(latestBlockReceived)) {
							System.out.println("We can append the received block to our chain");
							Consensus.getInstance().broadcast(latestBlockReceived);
						} else if (receivedBlocks.size() == 1) {
							System.out.println("We have to query the chain from our peer");
							Consensus.getInstance().queryAllBlocks();
						} else {
							if (Blockchain.getInstance().replaceChain(receivedBlocks)) {
								System.out.println(
										"Received blockchain is valid. Replacing current blockchain with received blockchain");
								Consensus.getInstance().broadcast(latestBlockReceived);
							} else {
								System.out.println(
										"Received blockchain is not longer than current blockchain. Do nothing");
							}
						}
					} else {
						System.out.println("Received blockchain is not longer than current blockchain. Do nothing");
					}
				} else if (type.equals(MessageType.QUERY_LATEST)) {
					JsonObject responseMessage = new JsonObject();
					responseMessage.addProperty("type", MessageType.RESPONSE_BLOCKCHAIN.toString());
					List<Block> blocks = new ArrayList<>();
					blocks.add(Blockchain.getInstance().getLastBlock());
					responseMessage.add("data", gson.toJsonTree(blocks));
					out.println(responseMessage);
				} else if (type.equals(MessageType.QUERY_ALL)) {
					JsonObject responseMessage = new JsonObject();
					responseMessage.addProperty("type", MessageType.RESPONSE_BLOCKCHAIN.toString());
					responseMessage.add("data", gson.toJsonTree(Blockchain.getInstance().getBlocks()));
					out.println(responseMessage);
				}
			}
		} catch (IOException e) {
			System.err.println("Connection failed to peer: " + socket.getInetAddress() + ":" + socket.getPort());
			Consensus.getInstance().removePeer(this);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public void write(String message) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(message);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
