package co.com.assist.node.consensus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import co.com.assist.node.blockchain.Block;

public class Consensus {

	private static final Consensus instance = new Consensus();

	public static Consensus getInstance() {
		return instance;
	}

	private List<PeerHandler> peers = new ArrayList<>();

	private Consensus() {
	}

	public List<PeerHandler> getPeerHandlers() {
		return Collections.unmodifiableList(peers);
	}

	public void connectToPeer(PeerHandler handler) {
		peers.add(handler);

		JsonObject message = new JsonObject();
		message.addProperty("type", MessageType.QUERY_LATEST.toString());
		handler.write(message.toString());
	}

	public void connectToPeer(InetSocketAddress peer) {
		try {
			Socket socket = new Socket(peer.getHostName(), peer.getPort());

			PeerHandler handler = new PeerHandler(socket);
			new Thread(handler).start();
			connectToPeer(handler);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void removePeer(PeerHandler handler) {
		peers.remove(handler);
	}

	public void broadcast(List<Block> blocks) {
		Gson gson = new GsonBuilder().create();

		JsonObject message = new JsonObject();
		message.addProperty("type", MessageType.RESPONSE_BLOCKCHAIN.toString());
		if (blocks != null && !blocks.isEmpty()) {
			message.add("data", gson.toJsonTree(blocks));
		}

		String json = message.toString();
		System.out.println("broadcasting " + json);

		getPeerHandlers().forEach(handler -> {
			handler.write(json);
		});
	}

	public void broadcast(Block block) {
		List<Block> blocks = new ArrayList<>();
		if (block != null) {
			blocks.add(block);
		}
		broadcast(blocks);
	}

	public void queryLastBlock() {
		JsonObject message = new JsonObject();
		message.addProperty("type", MessageType.QUERY_LATEST.toString());

		String json = message.toString();
		System.out.println("broadcasting " + json);

		getPeerHandlers().forEach(handler -> {
			handler.write(json);
		});
	}

	public void queryAllBlocks() {
		JsonObject message = new JsonObject();
		message.addProperty("type", MessageType.QUERY_ALL.toString());

		String json = message.toString();
		System.out.println("broadcasting " + json);

		getPeerHandlers().forEach(handler -> {
			handler.write(json);
		});
	}
}
