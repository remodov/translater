package com.translater.format.model

data class Step(
    var order: Int? = null,
    var searchValue: String? = null,
    var searchType: String? = null,
    var extractor: String? = null
)