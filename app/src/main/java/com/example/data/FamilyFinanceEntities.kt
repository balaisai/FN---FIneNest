package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String,
    val date: Long,
    val payer: String,
    val notes: String,
    val type: String = "Expense", // "Income", "Expense", "Investment", "Transfer"
    val paymentMethod: String = "Cash", // "Cash", "Credit Card", "Debit Card", "Bank Transfer", "UPI", "Cheque"
    val isSynced: Boolean = false, // True if synced in real-time from other family members
    val autoSaveContribution: Double = 0.0 // Amount transferred to automated savings goals from this purchase
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val allocatedLimit: Double
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentSaved: Double,
    val targetDate: String,
    val isAutomated: Boolean = false // If true, automatically receives round-up or percentage savings
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String,
    val type: String // "Income", "Expense", "Investment"
)

@Entity(tableName = "people")
data class PeopleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val moneyGiven: Double,
    val moneyReceived: Double,
    val pendingBalance: Double,
    val status: String // "Pending", "Closed"
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalAmount: Double,
    val monthlyEmi: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val endDate: String
)

@Entity(tableName = "insurance")
data class InsuranceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val policyName: String,
    val company: String,
    val premium: Double,
    val coverage: Double,
    val renewalDate: String,
    val status: String // "Active", "Near Expiry", "Expired"
)

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val buyDate: String,
    val stockName: String,
    val symbol: String,
    val quantity: Int,
    val buyPrice: Double,
    val investedAmount: Double,
    val currentPrice: Double,
    val currentValue: Double,
    val profitLoss: Double,
    val returnPercent: Double
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardName: String,
    val totalSpend: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val status: String // "Paid", "Due"
)

