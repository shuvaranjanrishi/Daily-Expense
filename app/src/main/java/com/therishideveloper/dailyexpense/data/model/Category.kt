package com.therishideveloper.dailyexpense.data.model

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.therishideveloper.dailyexpense.R

enum class Category(
    val dbKey: String,
    val titleRes: Int,
    val icon: ImageVector
) {
    // --- Income Categories ---
    SALARY("SALARY", R.string.cat_salary, Icons.Default.Payments),
    BUSINESS("BUSINESS", R.string.cat_business, Icons.Default.BusinessCenter),
    FREELANCE("FREELANCE", R.string.cat_freelance, Icons.Default.LaptopMac),
    BONUS("BONUS", R.string.cat_bonus, Icons.Default.Redeem),
    INVESTMENT("INVESTMENT", R.string.cat_investment, Icons.AutoMirrored.Filled.TrendingUp),
    GIFT("GIFT", R.string.cat_gift, Icons.Default.CardGiftcard),

    // --- Expense Categories ---
    FOOD("FOOD", R.string.cat_food, Icons.Default.Restaurant),
    RENT("RENT", R.string.cat_rent, Icons.Default.Home),
    UTILITY("UTILITY", R.string.cat_utility, Icons.Default.SettingsSuggest),
    TRANSPORT("TRANSPORT", R.string.cat_transport, Icons.Default.DirectionsBus),
    SHOPPING("SHOPPING", R.string.cat_shopping, Icons.Default.ShoppingBag),
    HEALTH("HEALTH", R.string.cat_health, Icons.Default.MedicalServices),
    EDUCATION("EDUCATION", R.string.cat_education, Icons.Default.School),
    ENTERTAINMENT("ENTERTAINMENT", R.string.cat_entertainment, Icons.Default.Theaters),

    // --- Default Category ---
    OTHERS("OTHERS", R.string.cat_others, Icons.Default.MoreHoriz);

    companion object {

        fun fromDbKey(key: String?): Category {
            return Category.entries.find { it.dbKey == key } ?: OTHERS
        }

        fun getIncomeCategories() = listOf(
            SALARY,
            BUSINESS,
            FREELANCE,
            BONUS,
            INVESTMENT,
            GIFT,
            OTHERS
        )

        fun getExpenseCategories() = listOf(
            FOOD,
            RENT,
            UTILITY,
            TRANSPORT,
            SHOPPING,
            HEALTH,
            EDUCATION,
            ENTERTAINMENT,
            OTHERS
        )

        fun findDbKeyByLocalizedName(query: String, context: Context): String? {
            return Category.entries.find {
                context.getString(it.titleRes).contains(query, ignoreCase = true)
            }?.dbKey
        }
    }
}