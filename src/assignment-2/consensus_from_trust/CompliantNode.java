package consensus_from_trust;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private int roundNum;
    private Set<Integer> newTx, allTx;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        roundNum = numRounds;
        newTx = new HashSet<>();
        allTx = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        for (Transaction t : pendingTransactions) {
            newTx.add(t.id);
            allTx.add(t.id);
        }
        pendingTransactions.clear();
    }

    public Set<Transaction> sendToFollowers() {
        Set<Transaction> txs = new HashSet<Transaction>();

        if (roundNum == 0) {
            newTx = allTx;
        }

        for (Integer i : newTx) {
            Transaction tx = new Transaction(i);
            txs.add(tx);
        }

        newTx.clear();

//        System.out.println("sending " + txs.size() + " transactions");
        roundNum--;

        return txs;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
//        System.out.println("receiving " + candidates.size() + " candidates");
        for (Candidate c : candidates) {
            if (!allTx.contains(c.tx.id)) {
                newTx.add(c.tx.id);
                allTx.add(c.tx.id);
            }
        }
        candidates.clear();
    }
}
