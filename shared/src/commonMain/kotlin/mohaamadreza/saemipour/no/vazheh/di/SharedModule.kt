package mohaamadreza.saemipour.no.vazheh.di

import mohaamadreza.saemipour.no.vazheh.data.AuthRepository
import mohaamadreza.saemipour.no.vazheh.data.ChildRepository
import mohaamadreza.saemipour.no.vazheh.data.ContentRepository
import mohaamadreza.saemipour.no.vazheh.data.ProgressRepository
import mohaamadreza.saemipour.no.vazheh.data.QuizRepository
import mohaamadreza.saemipour.no.vazheh.data.TokenStorage
import mohaamadreza.saemipour.no.vazheh.data.createSettings
import mohaamadreza.saemipour.no.vazheh.network.createHttpClient
import org.koin.dsl.module

/** Shared module containing repositories and network dependencies */
val sharedModule = module {
    // Network
    single { createHttpClient() }

    // Storage
    single { createSettings() }
    single { TokenStorage(get()) }

    // Repositories
    single { AuthRepository(get(), get()) }
    single { ContentRepository(get(), get()) }
    single { ChildRepository(get(), get()) }
    single { QuizRepository(get(), get()) }
    single { ProgressRepository(get(), get()) }
}
