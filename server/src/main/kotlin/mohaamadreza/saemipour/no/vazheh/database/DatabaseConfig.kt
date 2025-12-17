package mohaamadreza.saemipour.no.vazheh.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import mohaamadreza.saemipour.no.vazheh.database.tables.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    
    // Use H2 by default for development, PostgreSQL for production
    private val useH2 = System.getenv("USE_POSTGRES")?.toBoolean() != true
    
    // PostgreSQL config
    private val postgresUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/novazheh"
    private val dbUser = System.getenv("DATABASE_USER") ?: "postgres"
    private val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "password"
    
    // H2 config (file-based for persistence between restarts)
    private val h2Url = "jdbc:h2:file:./data/novazheh;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
    
    fun init() {
        if (useH2) {
            initH2()
        } else {
            initPostgres()
        }

        transaction {
            SchemaUtils.create(
                Parents,
                Children,
                Categories,
                Words,
                CustomWords,
                QuizAttempts,
                ChildProgress
            )
        }
        
        val dbType = if (useH2) "H2 (development)" else "PostgreSQL (production)"
        println("✅ Database connected ($dbType) and tables created successfully!")

        DatabaseSeeder.seed()
    }
    
    private fun initH2() {
        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = h2Url
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
    
    private fun initPostgres() {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = postgresUrl
            username = dbUser
            password = dbPassword
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 30000
            connectionTimeout = 30000
            maxLifetime = 1800000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }
}
