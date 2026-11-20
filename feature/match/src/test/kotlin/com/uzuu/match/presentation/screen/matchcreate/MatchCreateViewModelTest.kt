package com.uzuu.match.presentation.screen.matchcreate

import com.uzuu.match.domain.model.MatchType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MatchCreateViewModelTest {
    @Test
    fun `valid draft proceeds to next step`() {
        val holder = MatchDraftHolder()
        val viewModel = MatchCreateViewModel(holder)
        var proceeded = false

        viewModel.validateAndProceed { proceeded = true }

        assertTrue(proceeded)
        assertTrue(viewModel.uiState.value.errors.isEmpty())
    }

    @Test
    fun `invalid time in Kind A produces validation error`() {
        val holder = MatchDraftHolder()
        val viewModel = MatchCreateViewModel(holder)
        viewModel.updateDraft { it.copy(startTime = "20:00", endTime = "19:00") }
        var proceeded = false

        viewModel.validateAndProceed { proceeded = true }

        assertFalse(proceeded)
        assertTrue(viewModel.uiState.value.errors.containsKey("time"))
    }

    @Test
    fun `empty venues in Kind B produces validation error`() {
        val holder = MatchDraftHolder()
        val viewModel = MatchCreateViewModel(holder)
        viewModel.updateDraft { it.copy(kind = MatchType.NEED_VENUE, proposedVenues = emptyList()) }
        var proceeded = false

        viewModel.validateAndProceed { proceeded = true }

        assertFalse(proceeded)
        assertTrue(viewModel.uiState.value.errors.containsKey("venues"))
    }
}
