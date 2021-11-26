package com.translater.generate.model

data class CategoryModel(
    var name: String? = null,
    var pages: List<ShortPageInfo>? = null
)