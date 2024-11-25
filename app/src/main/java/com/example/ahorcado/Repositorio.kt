package com.example.ahorcado

import android.content.ContentValues.TAG
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class Repositorio() {
    private lateinit var db: FirebaseFirestore
    private lateinit var palabras: MutableList<String>

    fun init (){
        db = Firebase.firestore
        palabras = mutableListOf()
    }

    fun getPalabraRandom(): Task<QuerySnapshot> {

        return db.collection("palabras")
            .get()

//        for (document in result) {
//            Log.d(TAG, "${document.id} => ${document.data.get("palabra")}")
//            palabras.add(document.data.get("palabra").toString())
//        }
//
//        return result
    }


}