package co.com.assist.node.blockchain;

public class Block {

	private int index;
	private String previousHash;
	private String timestamp;
	private String data;
	private String hash;

	public Block(int index, String previousHash, String timestamp, String data, String hash) {
		this.index = index;
		this.previousHash = previousHash;
		this.timestamp = timestamp;
		this.data = data;
		this.hash = hash;
	}

	public int getIndex() {
		return index;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getData() {
		return data;
	}

	public String getHash() {
		return hash;
	}

	@Override
	public String toString() {
		return "Block [index=" + index + ", previousHash=" + previousHash + ", timestamp=" + timestamp + ", data="
				+ data + ", hash=" + hash + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + index;
		result = prime * result + ((previousHash == null) ? 0 : previousHash.hashCode());
		result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (index != other.index)
			return false;
		if (previousHash == null) {
			if (other.previousHash != null)
				return false;
		} else if (!previousHash.equals(other.previousHash))
			return false;
		if (timestamp == null) {
			if (other.timestamp != null)
				return false;
		} else if (!timestamp.equals(other.timestamp))
			return false;
		return true;
	}

}
