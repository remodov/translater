package com.translater.common.model


data class Meta(
    var name: String? = null,
    var property: String? = null,
    var content: String? = null
) {
    val isNameExists: Boolean by lazy {
        property?.isBlank() ?: false && name?.isNotBlank() ?: false
    }
}