package com.example.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

enum class OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LIST
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String
) {
    val firestoreException = exception as? FirebaseFirestoreException
    val code = firestoreException?.code?.name ?: "UNKNOWN"
    val msg = exception.localizedMessage ?: "No details"
    Log.e("FirestoreError", "[$code] Failed during $operationType on $path: $msg", exception)
}
