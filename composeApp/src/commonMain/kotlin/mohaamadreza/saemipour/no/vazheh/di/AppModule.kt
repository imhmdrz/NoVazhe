package mohaamadreza.saemipour.no.vazheh.di

import mohaamadreza.saemipour.no.vazheh.ui.screens.FaceGameViewModel
import mohaamadreza.saemipour.no.vazheh.ui.screens.MemoryGameViewModel
import mohaamadreza.saemipour.no.vazheh.ui.screens.ColorSortingViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.AuthViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.ChildViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.MotherViewModel
import mohaamadreza.saemipour.no.vazheh.ui.viewmodels.QuizViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** App module containing ViewModels */
val appModule = module {

    viewModelOf(::AuthViewModel)
    viewModelOf(::ChildViewModel)
    viewModelOf(::MotherViewModel)
    viewModelOf(::QuizViewModel)
    viewModelOf(::FaceGameViewModel)
    viewModelOf(::MemoryGameViewModel)
    viewModelOf(::ColorSortingViewModel)
}
