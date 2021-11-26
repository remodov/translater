package com.translater.format.model

import java.util.ArrayList

data class Rule(
    var fieldName: String,
    var type: String,
    var isTranslated: String,
    var steps: List<Step> = ArrayList()
)