package com.zgrcan.kalkan.data.firestore

import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.getNumberAsDouble(field: String): Double? {
    return when (val value = get(field)) {
        null -> null
        is Double -> value
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    }
}

internal fun DocumentSnapshot.getNumberAsLong(field: String): Long? {
    return when (val value = get(field)) {
        null -> null
        is Long -> value
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> null
    }
}
