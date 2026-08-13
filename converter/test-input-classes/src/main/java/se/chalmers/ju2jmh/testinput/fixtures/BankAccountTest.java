package se.chalmers.ju2jmh.testinput.fixtures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BankAccountTest {

    @Test
    public void depositIncreasesBalance() {
        BankAccount acct = new BankAccount(100);
        acct.deposit(50);
        assertEquals(150, acct.getBalance());
    }

    @Test
    public void withdrawDecreasesBalance() {
        BankAccount acct = new BankAccount(100);
        acct.withdraw(30);
        assertEquals(70, acct.getBalance());
    }

    @Test
    public void withdrawMoreThanBalanceThrows() {
        BankAccount acct = new BankAccount(50);
        assertThrows(IllegalArgumentException.class, () -> acct.withdraw(100));
    }

    @Test
    public void negativeDepositThrows() {
        BankAccount acct = new BankAccount(0);
        assertThrows(IllegalArgumentException.class, () -> acct.deposit(-10));
    }
}

