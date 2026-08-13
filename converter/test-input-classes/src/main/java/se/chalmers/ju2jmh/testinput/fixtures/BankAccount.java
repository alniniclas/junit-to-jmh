package se.chalmers.ju2jmh.testinput.fixtures;

public class BankAccount {
    private int balance;

    public BankAccount(int initial) {
        this.balance = initial;
    }

    public void withdraw(int amount) {
        if (amount > balance) throw new IllegalArgumentException("Insufficient funds");
        balance -= amount;
    }

    public void deposit(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative deposit");
        balance += amount;
    }

    public int getBalance() {
        return balance;
    }
}

