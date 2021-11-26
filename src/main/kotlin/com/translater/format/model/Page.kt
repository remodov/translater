package com.translater.format.model

import com.fasterxml.jackson.databind.JsonNode
import com.translater.common.model.Meta

data class Page(
    var sourceUrl: String? = null,
    var uniqueId: String? = null,
    var generatedDate: String? = null,
    //TODO move to payload
    var pictureUrl: String? = null,
    var metas: MutableList<Meta> = mutableListOf(),
    var payload: JsonNode? = null
)