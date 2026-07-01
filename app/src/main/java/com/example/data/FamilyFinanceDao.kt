package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyFinanceDao {

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()

    @Query("DELETE FROM savings_goals")
    suspend fun clearSavingsGoals()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM people")
    suspend fun clearPeople()

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("DELETE FROM insurance")
    suspend fun clearInsurance()

    @Query("DELETE FROM stocks")
    suspend fun clearStocks()

    @Query("DELETE FROM credit_cards")
    suspend fun clearCreditCards()

    // --- Budgets ---
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Query("SELECT * FROM budgets WHERE category = :category")
    suspend fun getBudgetByCategory(category: String): BudgetEntity?

    // --- Savings Goals ---
    @Query("SELECT * FROM savings_goals")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoals(goals: List<SavingsGoalEntity>)

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteSavingsGoal(id: Int)

    // --- Categories ---
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    // --- People ---
    @Query("SELECT * FROM people")
    fun getAllPeople(): Flow<List<PeopleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PeopleEntity)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deletePerson(id: Int)

    // --- Loans ---
    @Query("SELECT * FROM loans")
    fun getAllLoans(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteLoan(id: Int)

    // --- Insurance ---
    @Query("SELECT * FROM insurance")
    fun getAllInsurance(): Flow<List<InsuranceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInsurance(insurance: InsuranceEntity)

    @Query("DELETE FROM insurance WHERE id = :id")
    suspend fun deleteInsurance(id: Int)

    // --- Stocks ---
    @Query("SELECT * FROM stocks")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Query("DELETE FROM stocks WHERE id = :id")
    suspend fun deleteStock(id: Int)

    // --- Credit Cards ---
    @Query("SELECT * FROM credit_cards")
    fun getAllCreditCards(): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCreditCard(id: Int)
}
