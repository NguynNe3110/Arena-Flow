package com.igorwojda.showcase.feature.admin

import com.igorwojda.showcase.feature.admin.data.adminDataModule
import com.igorwojda.showcase.feature.admin.domain.adminDomainModule
import com.igorwojda.showcase.feature.admin.presentation.adminPresentationModule

/** Koin modules exported by `:feature:admin`. */
val featureAdminModules = listOf(adminDataModule, adminDomainModule, adminPresentationModule)
