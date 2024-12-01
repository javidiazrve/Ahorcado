package com.example.ahorcado

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore

class Repositorio() {
    private lateinit var db: FirebaseFirestore

    fun init (){
        db = Firebase.firestore
    }

    fun getPalabraRandom(): Task<QuerySnapshot> {

        return db.collection("palabras")
            .get()
    }

}