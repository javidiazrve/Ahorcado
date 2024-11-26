package com.example.ahorcado

import android.content.ContentValues.TAG
import android.icu.lang.UCharacter.toUpperCase
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import com.example.ahorcado.databinding.ActivityMainBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.Normalizer


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding:ActivityMainBinding
    private lateinit var palabras: List<String>
    private var palabraEnJuego: String = ""
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var buttonsMediaPlayer: MediaPlayer
    private var componentesLetras: ArrayList<TextView> = ArrayList<TextView>()
    private var botonesLetras: ArrayList<AppCompatButton> = ArrayList<AppCompatButton>()
    private var vidasRestantes: Int = 7
    private var ganador: Boolean = false
    private val client = OkHttpClient()
    private val audiosList: MutableList<Int> = mutableListOf(R.raw.bruh, R.raw.peo, R.raw.the_rock, R.raw.uy_uy_uy, R.raw.frog_laughing_meme, R.raw.gato_riendo, R.raw.monkey_gaga, R.raw.no_estes_fumando, R.raw.oh_my_god_meme, R.raw.penalti_madrid, R.raw.spongebob_fail)
    private lateinit var repo: Repositorio

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setListButtons()
        setMyListeners()
        initAudioPlayers()
        repo = Repositorio()
        repo.init()

        Log.d("SavedInstanceState", (savedInstanceState != null).toString())

        if(savedInstanceState != null){
            with(savedInstanceState){
                palabraEnJuego = getString("palabraEnJuego").toString()
                vidasRestantes = getInt("vidasRestantes")
                ganador = getBoolean("ganador")

                for ((index, isEnable) in getBooleanArray("estadoBotones")!!.withIndex()){
                    botonesLetras[index].isEnabled = isEnable
                }

                mostrarPalabra()

                for ((index, letra) in getStringArray("componentesLetras")!!.withIndex()){
                    componentesLetras[index].text = letra
                }

                if(getBoolean("isPlayinLastAudio")){
                    mediaPlayer.start()
                    detenerAllAudios()
                }

                checkGanador(vidasRestantes)

            }
        }else{
            palabras = arrayOf("tomar", "andadera", "puerta", "relojeria", "deporte", "amar", "holograma", "programa").toList()
            iniciarJuego()
        }

        Log.d("CREATE", palabraEnJuego)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("palabraEnJuego", palabraEnJuego)
        outState.putInt("vidasRestantes", vidasRestantes)
        outState.putBoolean("ganador", ganador)

        val btns: MutableList<Boolean> = mutableListOf()

        for (btn in botonesLetras){
            btns.add(btn.isEnabled)
        }

        outState.putBooleanArray("estadoBotones", btns.toBooleanArray())

        val letras: MutableList<String> = mutableListOf()

        for (letra in componentesLetras){
            letras.add(letra.text.toString())
        }

        outState.putStringArray("componentesLetras",letras.toTypedArray())

        Log.d("SAVE", palabraEnJuego)

        outState.putBoolean("isPlayingLastAudio", mediaPlayer.isPlaying)

        detenerAllAudios()
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun initAudioPlayers() {
        mediaPlayer = MediaPlayer.create(this, R.raw.chill_audio)
        mediaPlayer.isLooping = true

        buttonsMediaPlayer = MediaPlayer.create(this, audiosList.random())
        buttonsMediaPlayer.setOnCompletionListener {
            buttonsMediaPlayer.release()
        }
    }

    private fun setListButtons() {
        botonesLetras.add(binding.btA)
        botonesLetras.add(binding.btB)
        botonesLetras.add(binding.btC)
        botonesLetras.add(binding.btD)
        botonesLetras.add(binding.btE)
        botonesLetras.add(binding.btF)
        botonesLetras.add(binding.btG)
        botonesLetras.add(binding.btH)
        botonesLetras.add(binding.btI)
        botonesLetras.add(binding.btJ)
        botonesLetras.add(binding.btK)
        botonesLetras.add(binding.btL)
        botonesLetras.add(binding.btM)
        botonesLetras.add(binding.btN)
        botonesLetras.add(binding.btEnye)
        botonesLetras.add(binding.btO)
        botonesLetras.add(binding.btP)
        botonesLetras.add(binding.btQ)
        botonesLetras.add(binding.btR)
        botonesLetras.add(binding.btS)
        botonesLetras.add(binding.btT)
        botonesLetras.add(binding.btU)
        botonesLetras.add(binding.btV)
        botonesLetras.add(binding.btW)
        botonesLetras.add(binding.btX)
        botonesLetras.add(binding.btY)
        botonesLetras.add(binding.btZ)
    }

    private fun reiniciarJuego(){
        iniciarJuego()
    }

    private fun iniciarJuego() {

        vidasRestantes = 7
        ganador = false
        reiniciarBotones()
        checkGanador(vidasRestantes)
        // la funcion setPalabraEnJuegoConApi() obtiene la palabra que se va a jugar desde una Api,
        // esta pueden traer palabras con tilde lo cual no se maneja aqui en el codigo,
        // sientete libre de intentar de alguna forma manejar esto.
//        setPalabraEnJuegoConApi()
//        setPalabraEnJuego()
//        Thread.sleep(2000)
//        mostrarPalabra()
        setFailAudiosOrder()
        detenerAllAudios()
        setPalabraEnJuegoFirebase() // aqui hace todo lo que esta abajo

    }

    private fun reiniciarComponentes() {
        componentesLetras = ArrayList<TextView>()
        binding.filaLetras.removeAllViews()
    }

    private fun reiniciarBotones() {
        for (btn in botonesLetras){
            btn.isEnabled = true
        }
    }

    private fun mostrarPalabra() {

        reiniciarComponentes()

        Log.d("MostrarPalabra", palabraEnJuego)

        val tamano = palabraEnJuego.length

        val minWidthInDp = (100 * resources.displayMetrics.density).toInt()

        for (index in 0..tamano - 1){

            val textView = TextView(this).apply {
                background = AppCompatResources.getDrawable(context, R.drawable.bottom_border)
                textSize = 25f
                minWidth = minWidthInDp
                setPadding(8)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0,  // Ancho definido en base al peso
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f  // layout_weight para distribuir equitativamente el espacio
                )
            }

            componentesLetras.add(textView)

            binding.filaLetras.addView(textView)
        }

    }

    private fun setPalabraEnJuego() {

        palabraEnJuego = palabras.random()

    }

    private fun setPalabraEnJuegoConApi() {

        val request = Request.Builder()
            .url("https://clientes.api.greenborn.com.ar/public-random-word") // Cambia la URL según tu API
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val res = response.body?.string()
                    val palabra = res.toString().substring(2, res?.length!! - 2)
                    palabraEnJuego = cleanString(palabra)
//                    Log.d("MainActivity", "Response: ${palabraEnJuego}")
                }
            }
        })

    }

    private fun setPalabraEnJuegoFirebase() {

        repo.getPalabraRandom().addOnSuccessListener { result ->

            palabraEnJuego = result.documents.random().get("palabra").toString()
            mostrarPalabra()
            setFailAudiosOrder()
            detenerAllAudios()

        }
    }

    fun cleanString(texto: String): String {
        var cadena = Normalizer.normalize(texto, Normalizer.Form.NFD)
        cadena = cadena.replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
        return cadena
    }

    private fun setMyListeners(){

        for (btn in botonesLetras){
            btn.setOnClickListener(this)
        }
        
        binding.btnFlotante.setOnClickListener(this)
        
    }

    override fun onClick(btn: View?) {

        when(btn){
            binding.btnFlotante -> reiniciarJuego()
            else -> if (vidasRestantes != 0 && !ganador) onLetterPress(btn as AppCompatButton) else return
        }

    }

    private fun onLetterPress(btn: AppCompatButton) {

        val letra = btn.text[0]

        val contieneLetra = palabraEnJuego.contains(letra, true)

        if(contieneLetra){
            val tamano = palabraEnJuego.length

            for (index in 0..tamano - 1){
                if(palabraEnJuego[index].equals(letra, true)){
                    componentesLetras[index].text = toUpperCase(letra.toString())
                }
            }

            var counter = 0
            for (textView in componentesLetras){
                if(textView.text.toString() != ""){
                    counter++
                }
            }

            if(counter == tamano){
                ganador = true
            }

        }else{

            vidasRestantes--
            if(vidasRestantes == 0){
                mostrarPalabraCompleta()
            }else{
                reproducirFailAudio(vidasRestantes)
            }
        }

        btn.isEnabled = false

        checkGanador(vidasRestantes)
    }

    private fun mostrarPalabraCompleta(){
        val tamano = palabraEnJuego.length

        var palabraSeparada = palabraEnJuego.trim().split("")
        palabraSeparada = palabraSeparada.subList(1, tamano + 1)

        for (index in 0..<tamano){
            componentesLetras[index].text = toUpperCase(palabraSeparada[index])
        }
    }

    private fun checkGanador(vidas: Int){

        binding.imgAhorcado.scaleType = ImageView.ScaleType.FIT_CENTER
        val imagenes = arrayOf(R.drawable.chill_lose, R.drawable.ultimo, R.drawable.sexto, R.drawable.quinto, R.drawable.cuarto, R.drawable.tercero, R.drawable.segundo, R.drawable.primero)

        if(ganador){
            binding.imgAhorcado.setImageResource(R.drawable.chill_win)
        }else{
            binding.imgAhorcado.setImageResource(imagenes[vidas])
        }

        if(ganador || vidas == 0){
            detenerAllAudios()
            reproducirLastAudio()
            binding.imgAhorcado.scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    private fun setFailAudiosOrder(){
        audiosList.shuffle()
    }

    private fun reproducirFailAudio(audioIndex: Int){
        if(buttonsMediaPlayer.isPlaying){
            buttonsMediaPlayer.stop()
            buttonsMediaPlayer.release()
        }

        buttonsMediaPlayer = MediaPlayer.create(this, audiosList.get(audioIndex))
        buttonsMediaPlayer.start()
    }

    private fun reproducirLastAudio(){
        mediaPlayer.start()
    }

    private fun detenerAllAudios(){

        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.prepare()
            Log.d("AUDIO", "PARAR AUDIO")
        }

        if(buttonsMediaPlayer.isPlaying){
            buttonsMediaPlayer.stop()
            buttonsMediaPlayer.reset()
            Log.d("AUDIO", "PARAR AUDIO BOTONES")
        }

        Log.d("AUDIO", "Salir Audio")

    }


}