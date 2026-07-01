package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FinanceRepository(private val dao: FamilyFinanceDao) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()
    val allPeople: Flow<List<PeopleEntity>> = dao.getAllPeople()
    val allLoans: Flow<List<LoanEntity>> = dao.getAllLoans()
    val allInsurance: Flow<List<InsuranceEntity>> = dao.getAllInsurance()
    val allStocks: Flow<List<StockEntity>> = dao.getAllStocks()
    val allCreditCards: Flow<List<CreditCardEntity>> = dao.getAllCreditCards()

    suspend fun insertTransaction(
        amount: Double,
        category: String,
        payer: String,
        notes: String,
        type: String = "Expense",
        paymentMethod: String = "Cash",
        isSynced: Boolean = false,
        autoSaveRule: String = "Round Up ($1)", // "None", "Round Up ($1)", "Round Up ($5)", "5% Auto-Save", "10% Auto-Save"
        customDate: Long? = null
    ) = withContext(Dispatchers.IO) {
        
        // Calculate auto-save contribution based on rule - only apply to Expenses
        var contribution = 0.0
        if (amount > 0 && type == "Expense") {
            when (autoSaveRule) {
                "Round Up ($1)" -> {
                    val nextDollar = kotlin.math.ceil(amount)
                    contribution = nextDollar - amount
                    if (contribution == 0.0 && amount > 0) {
                        contribution = 1.0 // If already flat, contribute $1
                    }
                }
                "Round Up ($5)" -> {
                    val nextFive = kotlin.math.ceil(amount / 5.0) * 5.0
                    contribution = nextFive - amount
                    if (contribution == 0.0 && amount > 0) {
                        contribution = 5.0
                    }
                }
                "5% Auto-Save" -> {
                    contribution = amount * 0.05
                }
                "10% Auto-Save" -> {
                    contribution = amount * 0.10
                }
                else -> {
                    contribution = 0.0
                }
            }
        }
        
        // Match 2 decimals
        contribution = kotlin.math.round(contribution * 100.0) / 100.0

        // If there's any contribution, add it to our automated savings goals!
        if (contribution > 0.0) {
            val activeGoals = dao.getAllSavingsGoals().first().filter { it.isAutomated }
            if (activeGoals.isNotEmpty()) {
                // Split contribution equally among active automated goals
                val portion = contribution / activeGoals.size
                val roundedPortion = kotlin.math.round(portion * 100.0) / 100.0
                
                activeGoals.forEach { goal ->
                    val updatedGoal = goal.copy(
                        currentSaved = goal.currentSaved + roundedPortion
                    )
                    dao.updateSavingsGoal(updatedGoal)
                }
            } else {
                // If there are no automated goals but there is some goal, put it in the first goal
                val allGoals = dao.getAllSavingsGoals().first()
                if (allGoals.isNotEmpty()) {
                    val updatedGoal = allGoals[0].copy(
                        currentSaved = allGoals[0].currentSaved + contribution
                    )
                    dao.updateSavingsGoal(updatedGoal)
                } else {
                    contribution = 0.0 // No goals to save to
                }
            }
        }

        val tx = TransactionEntity(
            amount = amount,
            category = category,
            date = customDate ?: System.currentTimeMillis(),
            payer = payer,
            notes = notes,
            type = type,
            paymentMethod = paymentMethod,
            isSynced = isSynced,
            autoSaveContribution = contribution
        )
        dao.insertTransaction(tx)
    }

    suspend fun deleteTransaction(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteTransaction(id)
    }

    suspend fun updateTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.insertTransaction(tx)
    }

    suspend fun insertBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        dao.insertBudget(budget)
    }

    suspend fun insertSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        dao.insertSavingsGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        dao.updateSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteSavingsGoal(id)
    }

    suspend fun insertCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        dao.insertCategory(category)
    }

    suspend fun insertPerson(person: PeopleEntity) = withContext(Dispatchers.IO) {
        dao.insertPerson(person)
    }

    suspend fun deletePerson(id: Int) = withContext(Dispatchers.IO) {
        dao.deletePerson(id)
    }

    suspend fun insertLoan(loan: LoanEntity) = withContext(Dispatchers.IO) {
        dao.insertLoan(loan)
    }

    suspend fun deleteLoan(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteLoan(id)
    }

    suspend fun insertInsurance(insurance: InsuranceEntity) = withContext(Dispatchers.IO) {
        dao.insertInsurance(insurance)
    }

    suspend fun deleteInsurance(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteInsurance(id)
    }

    suspend fun insertStock(stock: StockEntity) = withContext(Dispatchers.IO) {
        dao.insertStock(stock)
    }

    suspend fun deleteStock(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteStock(id)
    }

    suspend fun insertCreditCard(card: CreditCardEntity) = withContext(Dispatchers.IO) {
        dao.insertCreditCard(card)
    }

    suspend fun deleteCreditCard(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteCreditCard(id)
    }
    
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearTransactions()
        dao.clearBudgets()
        dao.clearSavingsGoals()
        dao.clearCategories()
        dao.clearPeople()
        dao.clearLoans()
        dao.clearInsurance()
        dao.clearStocks()
        dao.clearCreditCards()
    }

    suspend fun clearStocksOnly() = withContext(Dispatchers.IO) {
        dao.clearStocks()
    }

    // Seed checking on app start to ensure high-fidelity experience immediately!
    suspend fun seedMockDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingBudgets = dao.getAllBudgets().first()
        if (existingBudgets.isEmpty()) {
            // Seed budgets mapped to the categories in the prompt
            val defaultBudgets = listOf(
                BudgetEntity("Food", 600.0),
                BudgetEntity("Grocery", 400.0),
                BudgetEntity("Rent", 1500.0),
                BudgetEntity("Bills", 300.0),
                BudgetEntity("Fuel", 200.0),
                BudgetEntity("Medical", 150.0),
                BudgetEntity("Other", 200.0)
            )
            dao.insertBudgets(defaultBudgets)
        }

        val existingGoals = dao.getAllSavingsGoals().first()
        if (existingGoals.isEmpty()) {
            // Seed savings goals
            val defaultGoals = listOf(
                SavingsGoalEntity(name = "🛡️ Emergency Fund", targetAmount = 10000.0, currentSaved = 3500.0, targetDate = "Dec 2026", isAutomated = true),
                SavingsGoalEntity(name = "📈 Girls Post Office Scheme", targetAmount = 5000.0, currentSaved = 1200.0, targetDate = "Aug 2028", isAutomated = false),
                SavingsGoalEntity(name = "📈 Boys Post Office Scheme", targetAmount = 5000.0, currentSaved = 1000.0, targetDate = "Aug 2028", isAutomated = false)
            )
            dao.insertSavingsGoals(defaultGoals)
        }

        val existingTransactions = dao.getAllTransactions().first()
        if (existingTransactions.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            
            // Seed past transactions with realistic Excel data
            val list = listOf(
                TransactionEntity(amount = 45000.0, category = "Salary", date = now - 10 * dayMs, payer = "Dad", notes = "Monthly salary credit", type = "Income", paymentMethod = "Bank Transfer", isSynced = false, autoSaveContribution = 0.0),
                TransactionEntity(amount = 12000.0, category = "Rent", date = now - 8 * dayMs, payer = "Dad", notes = "Flat Monthly Rent", type = "Expense", paymentMethod = "Bank Transfer", isSynced = false, autoSaveContribution = 0.0),
                TransactionEntity(amount = 1450.20, category = "Grocery", date = now - 6 * dayMs, payer = "Mom", notes = "Supermarket stock up", type = "Expense", paymentMethod = "Credit Card", isSynced = true, autoSaveContribution = 0.80),
                TransactionEntity(amount = 4000.00, category = "Stocks", date = now - 5 * dayMs, payer = "Dad", notes = "Mutual Fund SIP investment", type = "Investment", paymentMethod = "UPI", isSynced = false, autoSaveContribution = 0.0),
                TransactionEntity(amount = 450.00, category = "Fuel", date = now - 4 * dayMs, payer = "Dad", notes = "Refuel family SUV", type = "Expense", paymentMethod = "Debit Card", isSynced = false, autoSaveContribution = 5.00),
                TransactionEntity(amount = 149.99, category = "Bills", date = now - 3 * dayMs, payer = "Sarah", notes = "Netflix subscription", type = "Expense", paymentMethod = "UPI", isSynced = true, autoSaveContribution = 0.01),
                TransactionEntity(amount = 1800.00, category = "Emergency Fund", date = now - 2 * dayMs, payer = "Mom", notes = "Monthly emergency reserve cash", type = "Investment", paymentMethod = "Cash", isSynced = true, autoSaveContribution = 0.0),
                TransactionEntity(amount = 890.40, category = "Bills", date = now - 1 * dayMs, payer = "Mom", notes = "Water Bill payment", type = "Expense", paymentMethod = "UPI", isSynced = true, autoSaveContribution = 0.60)
            )
            for (tx in list) {
                dao.insertTransaction(tx)
            }
        }

        val existingCategories = dao.getAllCategories().first()
        if (existingCategories.isEmpty()) {
            val defaultCategories = listOf(
                CategoryEntity("Salary", "Income"),
                CategoryEntity("Business", "Income"),
                CategoryEntity("Bonus", "Income"),
                CategoryEntity("Money Received", "Income"),
                CategoryEntity("Food", "Expense"),
                CategoryEntity("Grocery", "Expense"),
                CategoryEntity("Shopping", "Expense"),
                CategoryEntity("Bills", "Expense"),
                CategoryEntity("Fuel", "Expense"),
                CategoryEntity("Medical", "Expense"),
                CategoryEntity("Education", "Expense"),
                CategoryEntity("Credit Card Expense", "Expense"),
                CategoryEntity("Money Given", "Expense"),
                CategoryEntity("Money Won't Be Returned", "Expense"),
                CategoryEntity("Stocks", "Investment"),
                CategoryEntity("Mutual Fund", "Investment"),
                CategoryEntity("Gold", "Investment"),
                CategoryEntity("Emergency Fund", "Investment"),
                CategoryEntity("Property", "Investment"),
                CategoryEntity("Other", "Expense")
            )
            dao.insertCategories(defaultCategories)
        }

        val existingPeople = dao.getAllPeople().first()
        if (existingPeople.isEmpty()) {
            val defaultPeople = listOf(
                PeopleEntity(name = "Kumar", moneyGiven = 5000.0, moneyReceived = 2000.0, pendingBalance = 3000.0, status = "Pending"),
                PeopleEntity(name = "Priya", moneyGiven = 3000.0, moneyReceived = 3000.0, pendingBalance = 0.0, status = "Closed"),
                PeopleEntity(name = "Amit", moneyGiven = 1500.0, moneyReceived = 0.0, pendingBalance = 1500.0, status = "Pending")
            )
            for (p in defaultPeople) { dao.insertPerson(p) }
        }

        val existingLoans = dao.getAllLoans().first()
        if (existingLoans.isEmpty()) {
            val defaultLoans = listOf(
                LoanEntity(name = "🏡 Home Loan", totalAmount = 4500000.0, monthlyEmi = 35000.0, paidAmount = 1400000.0, remainingAmount = 3100000.0, endDate = "Dec 2035"),
                LoanEntity(name = "🚗 SUV Car Loan", totalAmount = 1200000.0, monthlyEmi = 22000.0, paidAmount = 528000.0, remainingAmount = 672000.0, endDate = "Jun 2028")
            )
            for (l in defaultLoans) { dao.insertLoan(l) }
        }

        val existingInsurance = dao.getAllInsurance().first()
        if (existingInsurance.isEmpty()) {
            val defaultInsurance = listOf(
                InsuranceEntity(policyName = "🏥 Family Health Cover", company = "HDFC ERGO", premium = 18000.0, coverage = 1000000.0, renewalDate = "15 Oct 2026", status = "Active"),
                InsuranceEntity(policyName = "🛡️ Term Life Insurance", company = "LIC of India", premium = 24000.0, coverage = 15000000.0, renewalDate = "22 Jul 2026", status = "Active")
            )
            for (i in defaultInsurance) { dao.insertInsurance(i) }
        }

        val existingStocks = dao.getAllStocks().first()
        if (existingStocks.isEmpty()) {
            val defaultStocks = listOf(
                StockEntity(buyDate = "12 Jan 2025", stockName = "Tata Motors Ltd", symbol = "TATAMOTORS", quantity = 50, buyPrice = 820.0, investedAmount = 41000.0, currentPrice = 910.0, currentValue = 45500.0, profitLoss = 4500.0, returnPercent = 10.97),
                StockEntity(buyDate = "05 Mar 2025", stockName = "Reliance Industries", symbol = "RELIANCE", quantity = 25, buyPrice = 2850.0, investedAmount = 71250.0, currentPrice = 3120.0, currentValue = 78000.0, profitLoss = 6750.0, returnPercent = 9.47)
            )
            for (s in defaultStocks) { dao.insertStock(s) }
        }

        val existingCards = dao.getAllCreditCards().first()
        if (existingCards.isEmpty()) {
            val defaultCards = listOf(
                CreditCardEntity(cardName = "💳 Regalia Gold", totalSpend = 45000.0, paidAmount = 15000.0, pendingAmount = 30000.0, status = "Due"),
                CreditCardEntity(cardName = "💳 Amazon Pay ICICI", totalSpend = 12000.0, paidAmount = 12000.0, pendingAmount = 0.0, status = "Paid")
            )
            for (c in defaultCards) { dao.insertCreditCard(c) }
        }
    }
}
