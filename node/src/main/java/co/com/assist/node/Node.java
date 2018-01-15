package co.com.assist.node;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import co.com.assist.node.blockchain.Blockchain;
import co.com.assist.node.consensus.Consensus;
import co.com.assist.node.consensus.PeerHandler;

public class Node implements Runnable {

	public static void main(String[] args) {
		try (FileReader reader = new FileReader(args[0] + ".json")) {
			JsonElement jsonElement = new JsonParser().parse(reader);
			System.out.println(jsonElement);
			NodeConfig nodeConfig = new GsonBuilder().create().fromJson(jsonElement, NodeConfig.class);
			new Node(nodeConfig).run();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private NodeConfig config;

	public Node(NodeConfig config) {
		this.config = config;
	}

	public void run() {
		Blockchain.getInstance().replaceChain(config.getBlochchain());

		config.getPeers().forEach(peer -> {
			String[] data = peer.split(":");
			InetSocketAddress address = new InetSocketAddress(data[0], Integer.parseInt(data[1]));
			Consensus.getInstance().connectToPeer(address);
		});

		startNodeServer();
		startConsensusServer();

	}

	private void startNodeServer() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URI uri = new URL("http", "localhost", config.getApi(), "/node").toURI();
					ResourceConfig config = new ResourceConfig(NodeService.class);
					GrizzlyHttpServerFactory.createHttpServer(uri, config);
				} catch (MalformedURLException | URISyntaxException e) {
					System.err.println(e.getMessage());
				}
			}
		}).start();

		System.out.println("Listening http on port: " + config.getApi());
	}

	private void startConsensusServer() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try (ServerSocket serverSocket = new ServerSocket(config.getP2p())) {
					while (true) {
						Socket socket = serverSocket.accept();

						PeerHandler handler = new PeerHandler(socket);
						handler.start();
						Consensus.getInstance().connectToPeer(handler);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

		System.out.println("Listening socket on port: " + config.getP2p());
	}

}
