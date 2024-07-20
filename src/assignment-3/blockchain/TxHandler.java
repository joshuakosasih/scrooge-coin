package blockchain;

import java.util.ArrayList;

public class TxHandler {
    private UTXOPool uPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        uPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        UTXOPool tempPool = new UTXOPool();
        double totalInput = 0;
        double totalOutput = 0;

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            // 1
            if (!uPool.contains(utxo))
                return false;
            Transaction.Output output = uPool.getTxOutput(utxo);
            // 2
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), in.signature))
                return false;
            // 3
            if (tempPool.contains(utxo))
                return false;
            tempPool.addUTXO(utxo, output);

            totalInput += output.value;
        }

        // 4
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output out = tx.getOutput(i);
            if (out.value < 0)
                return false;

            totalOutput += out.value;
        }

        // 5
        return totalInput >= totalOutput;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> txs = new ArrayList<>();
        for (Transaction tx: possibleTxs) {
            if (isValidTx(tx)) {
                for (Transaction.Input in: tx.getInputs())
                    uPool.removeUTXO(new UTXO(in.prevTxHash, in.outputIndex));
                for (int i = 0; i < tx.numOutputs(); i++)
                    uPool.addUTXO(new UTXO(tx.getHash(), i), tx.getOutput(i));
                txs.add(tx);
            }
        }

        return txs.toArray(new Transaction[0]);
    }

    public UTXOPool getUTXOPool() {
        return uPool;
    }
}
