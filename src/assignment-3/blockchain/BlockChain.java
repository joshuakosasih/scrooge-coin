// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

package blockchain;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    public static class BlockInfo {
        public final Integer blockHeight;
        public final UTXOPool utxoPool;

        public BlockInfo(Integer bHeight, UTXOPool uPool) {
            blockHeight = bHeight;
            utxoPool = uPool;
        }
    }

    private TransactionPool transactionPool;
    private Block maxHeightBlock;
    private BlockInfo maxHeightBlockInfo;
    private HashMap<ByteBuffer, BlockInfo> blockMap;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        blockMap = new HashMap<>();
        maxHeightBlock = genesisBlock;

        UTXOPool utxoPool = processBlock(genesisBlock, new UTXOPool());
        maxHeightBlockInfo = new BlockInfo(1, utxoPool);
        transactionPool = new TransactionPool();

        blockMap.put(ByteBuffer.wrap(genesisBlock.getHash()), maxHeightBlockInfo);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return maxHeightBlock;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return maxHeightBlockInfo.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return transactionPool;
    }

    /**  */
    private UTXOPool processBlock(Block block, UTXOPool utxoPool) {
        TxHandler txHandler = new TxHandler(utxoPool);
        txHandler.handleTxs(block.getTransactions().toArray(new Transaction[0]));
        return txHandler.getUTXOPool();
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // null check
        if (block == null)
            return false;

        // reject genesis block
        if (block.getPrevBlockHash() == null)
            return false;

        // reject invalid coinbase
        Transaction coinbase = block.getCoinbase();
        if (coinbase == null || coinbase.getOutputs().size() != 1 || block.getCoinbase().getOutput(0).value != Block.COINBASE)
            return false;

        // fetch previous block
        BlockInfo prevBlockInfo = blockMap.get(ByteBuffer.wrap(block.getPrevBlockHash()));
        if (prevBlockInfo == null)
            return false;

        // stop creating block from old chain
        Integer newHeight = prevBlockInfo.blockHeight + 1;
        if (newHeight <= (maxHeightBlockInfo.blockHeight - CUT_OFF_AGE))
            return false;

        // TxHandler validation
        UTXOPool newUPool = processBlock(block, prevBlockInfo.utxoPool);
        BlockInfo newBlockInfo = new BlockInfo(newHeight, newUPool);
        blockMap.put(ByteBuffer.wrap(block.getHash()), newBlockInfo);

        // update maxHeightBlock
        if (newHeight > maxHeightBlockInfo.blockHeight) {
            maxHeightBlock = block;
            maxHeightBlockInfo = newBlockInfo;
        }

        // block is valid
        updateTransactionPool(block);
        return true;
    }

    /** Update transactionPool by removing transactions that exist in the block */
    private void updateTransactionPool(Block block) {
        for (Transaction tx : block.getTransactions()) {
            transactionPool.removeTransaction(tx.getHash());
        }
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }
}