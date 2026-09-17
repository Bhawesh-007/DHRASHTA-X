package com.dhrashtax

import android.app.Application
import com.dhrashtax.data.AlertRepository
import com.dhrashtax.data.DhrashtaDatabase
import com.dhrashtax.data.SettingsRepository
import com.dhrashtax.domain.RuleEngine

class DhrashtaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = DhrashtaDatabase.create(this)
        container = AppContainer(
            alerts = AlertRepository(database.alertDao()),
            settings = SettingsRepository(this),
            ruleEngine = RuleEngine(),
        )
    }
}

data class AppContainer(
    val alerts: AlertRepository,
    val settings: SettingsRepository,
    val ruleEngine: RuleEngine,
)

