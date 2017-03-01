package io.oddworks.device.model

import org.json.JSONObject

data class OddError(val id: String,
                    val status: String,
                    val code: String,
                    val title: String,
                    val detail: String,
                    val meta: JSONObject? = null)
