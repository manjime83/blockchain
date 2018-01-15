package co.com.assist.node;

import java.util.ArrayList;
import java.util.List;

import co.com.assist.node.blockchain.Block;

public class NodeConfig {

	private String name = "node";
	private int api = 3001;
	private int p2p = 6001;
	private List<String> peers = new ArrayList<>();
	private List<Block> blochchain = new ArrayList<>();

	public NodeConfig() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getApi() {
		return api;
	}

	public void setApi(int api) {
		this.api = api;
	}

	public int getP2p() {
		return p2p;
	}

	public void setP2p(int p2p) {
		this.p2p = p2p;
	}

	public List<String> getPeers() {
		return peers;
	}

	public void setPeers(List<String> peers) {
		this.peers = peers;
	}

	public List<Block> getBlochchain() {
		return blochchain;
	}

	public void setBlochchain(List<Block> blochchain) {
		this.blochchain = blochchain;
	}

}
