package mohaamadreza.saemipour.no.vazheh.di

import org.koin.core.context.startKoin

/** Initialize Koin for iOS Call this from Swift: KoinHelperKt.doInitKoin() */
fun initKoin() {
    startKoin { modules(sharedModule, appModule) }
}
