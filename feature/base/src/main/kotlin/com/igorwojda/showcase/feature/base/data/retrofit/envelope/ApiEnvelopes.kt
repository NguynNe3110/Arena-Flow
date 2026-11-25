package com.igorwojda.showcase.feature.base.data.retrofit.envelope

import kotlinx.serialization.Serializable

/** Standard single-object success envelope `{ "data": T }` per api-convention.md §4.3. */
@Serializable
data class ApiEnvelope<T>(
    val data: T,
)

/** Offset pagination metadata per api-convention.md §7.1. */
@Serializable
data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
)

/** Standard offset-paginated collection envelope per api-convention.md §4.3 & §7.1. */
@Serializable
data class PaginatedEnvelope<T>(
    val data: List<T>,
    val pagination: PaginationDto,
)

/** Standard cursor-paginated list payload per api-convention.md §7.2. */
@Serializable
data class CursorListDto<T>(
    val items: List<T>,
    val hasMore: Boolean,
)

/** Field-level validation error item per api-convention.md §11. */
@Serializable
data class FieldValidationErrorDto(
    val field: String,
    val message: String,
)

/** Error payload inside `ApiErrorEnvelope` per api-convention.md §4.4 & §11. */
@Serializable
data class ApiErrorBodyDto(
    val code: String,
    val message: String,
    val details: String? = null,
    val timestamp: String? = null,
    val path: String? = null,
    val validationErrors: List<FieldValidationErrorDto> = emptyList(),
)

/** Standard error envelope `{ "error": { ... } }` per api-convention.md §4.4. */
@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorBodyDto,
)
