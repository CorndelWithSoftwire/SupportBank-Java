package training.supportbank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Account {

    private String owner;
    private List<Transaction> incomingTransactions = new ArrayList<>();
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    public Account(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void addIncomingTransaction(Transaction transaction) {
        incomingTransactions.add(transaction);
    }

    public void addOutgoingTransaction(Transaction transaction) {
        outgoingTransactions.add(transaction);
    }

    public BigDecimal calculateBalance() {
        BigDecimal totalIn = incomingTransactions.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOut = outgoingTransactions.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIn.subtract(totalOut);
    }

    public List<Transaction> getAllTransactionsInDateOrder() {
        List<Transaction> allTransations = new ArrayList<>();
        allTransations.addAll(incomingTransactions);
        allTransations.addAll(outgoingTransactions);
        allTransations.sort(Comparator.comparing(Transaction::getDate));
        return allTransations;
    }
}
