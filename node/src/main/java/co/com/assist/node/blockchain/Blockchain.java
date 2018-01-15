package co.com.assist.node.blockchain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

public class Blockchain {

	public static final Block GENESIS_BLOCK;

	static {
		int index = 0;
		String previousHash = String.join("", Collections.nCopies(64, "0"));
		String timestamp = ZonedDateTime.of(1983, 02, 28, 0, 0, 0, 0, ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		String data = "";
		String hash = calculateHash(index, previousHash, timestamp, data);

		GENESIS_BLOCK = new Block(index, previousHash, timestamp, data, hash);
	}

	private static final Blockchain instance = new Blockchain();

	public static Blockchain getInstance() {
		return instance;
	}

	private List<Block> blocks = Collections.synchronizedList(new ArrayList<Block>());

	private Blockchain() {
		addBlock(GENESIS_BLOCK);
	}

	public List<Block> getBlocks() {
		return Collections.unmodifiableList(blocks);
	}

	public boolean addBlock(Block newBlock) {
		if (blocks.isEmpty() && newBlock.equals(GENESIS_BLOCK)) {
			return blocks.add(newBlock);
		} else if (isValidNewBlock(newBlock, getLastBlock())) {
			return blocks.add(newBlock);
		} else {
			return false;
		}
	}

	public Block getBlock(int index) {
		return blocks.get(index);
	}

	public Block getLastBlock() {
		if (blocks.isEmpty()) {
			return null;
		} else {
			return blocks.get(blocks.size() - 1);
		}
	}

	public Block generateNextBlock(String data) {
		Block previousBlock = getLastBlock();

		int index = previousBlock.getIndex() + 1;
		String previousHash = previousBlock.getHash();
		String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		String hash = calculateHash(index, previousHash, timestamp, data);

		return new Block(index, previousHash, timestamp, data, hash);
	}

	private static String calculateHash(int index, String previousHash, String timestamp, String data) {
		String dataToHash = String.format("%06d", index) + previousHash + timestamp + data;
		return DigestUtils.sha256Hex(dataToHash);
	};

	private static String calculateHash(Block block) {
		return calculateHash(block.getIndex(), block.getPreviousHash(), block.getTimestamp(), block.getData());
	};

	private static boolean isValidNewBlock(Block newBlock, Block previousBlock) {
		if (previousBlock.getIndex() + 1 != newBlock.getIndex()) {
			System.err.println("invalid index");
			return false;
		} else if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
			System.err.println("invalid previousHash");
			return false;
		} else if (!calculateHash(newBlock).equals(newBlock.getHash())) {
			System.err.println("invalid hash");
			return false;
		} else {
			return true;
		}
	};

	private boolean isValidChain(List<Block> blocks) {
		if (blocks.isEmpty() || !GENESIS_BLOCK.equals(blocks.get(0))) {
			System.err.println("invalid genesis block");
			return false;
		}

		Block previousBlock = blocks.get(0);
		for (int i = 1; i < blocks.size(); i++) {
			if (isValidNewBlock(blocks.get(i), previousBlock)) {
				previousBlock = blocks.get(i);
			} else {
				System.err.println("invalid block");
				return false;
			}
		}

		return true;
	}

	public boolean isValidChain() {
		return isValidChain(blocks);
	}

	public boolean replaceChain(List<Block> newBlocks) {
		if (newBlocks.size() > blocks.size() && isValidChain(newBlocks)) {
			blocks = newBlocks;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Block block : blocks) {
			sb.append(block).append(System.lineSeparator());
		}
		return sb.toString();
	}

}
