package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        CategoryEntity::class,
        PeopleEntity::class,
        LoanEntity::class,
        InsuranceEntity::class,
        StockEntity::class,
        CreditCardEntity::class,
        RecurringExpenseEntity::class,
        GovtSchemeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FamilyDatabase : RoomDatabase() {
    abstract val dao: FamilyFinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FamilyDatabase? = null

        fun getDatabase(context: Context): FamilyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamilyDatabase::class.java,
                    "family_finance_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
