package consensus_from_trust;

import java.util.ArrayList;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private Set<Transaction> transactions;
    private int roundNum = 0;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        transactions = pendingTransactions;
        pendingTransactions.clear();
    }

    public Set<Transaction> sendToFollowers() {
        return transactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
//        roundNum++;
//        if (roundNum >= 2) {
//            candidates.clear();
//            return;
//        }

        for (Candidate candidate : candidates) {
            transactions.add(candidate.tx);
        }
        candidates.clear();
    }
}
