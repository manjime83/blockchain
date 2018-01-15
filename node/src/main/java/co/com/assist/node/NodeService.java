package co.com.assist.node;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import co.com.assist.node.blockchain.Block;
import co.com.assist.node.blockchain.Blockchain;
import co.com.assist.node.consensus.Consensus;

@Path("api")
public class NodeService {

	@GET
	@Path("blocks")
	@Produces(MediaType.APPLICATION_JSON)
	public Response blocks() {
		return Response.ok(Blockchain.getInstance().getBlocks()).build();
	}

	@POST
	@Path("addData")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addData(@FormParam("data") String data) {
		Block newBlock = Blockchain.getInstance().generateNextBlock(data);
		Blockchain.getInstance().addBlock(newBlock);
		Consensus.getInstance().broadcast(newBlock);
		System.out.println("block added: " + newBlock);
		return Response.ok().build();
	}

	@GET
	@Path("peers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response peers() {
		return Response.ok(Consensus.getInstance().getPeerHandlers().stream().map(handler -> {
			Map<String, Object> map = new HashMap<>();
			map.put("hostname", handler.getSocket().getInetAddress());
			map.put("port", Integer.valueOf(handler.getSocket().getPort()));
			return map;
		}).collect(Collectors.toSet())).build();
	}

	@POST
	@Path("addPeer")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addPeer(@FormParam("hostname") String hostname, @FormParam("port") int port) {
		InetSocketAddress peer = new InetSocketAddress(hostname, port);
		Consensus.getInstance().connectToPeer(peer);
		return Response.ok().build();
	}

}
