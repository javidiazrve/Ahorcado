package com.example.ahorcado

import android.icu.lang.UCharacter.toUpperCase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding:ActivityMainBinding;
    private lateinit var palabras: List<String>;
    private var palabraEnJuego: String = "";
    private var componentesLetras: ArrayList<TextView> = ArrayList<TextView>();
    private var botonesLetras: ArrayList<AppCompatButton> = ArrayList<AppCompatButton>();
    private var vidasRestantes: Int = 7;
    private var ganador: Boolean = false;
    private val client = OkHttpClient()

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

        palabras = arrayOf("tomar", "andadera", "puerta", "relojeria", "deporte", "amar", "holograma", "programa").toList()

        setListButtons()
        setMyListeners()
        iniciarJuego()

    }

    private fun setListButtons() {
        botonesLetras.add(binding.btA);
        botonesLetras.add(binding.btB);
        botonesLetras.add(binding.btC);
        botonesLetras.add(binding.btD);
        botonesLetras.add(binding.btE);
        botonesLetras.add(binding.btF);
        botonesLetras.add(binding.btG);
        botonesLetras.add(binding.btH);
        botonesLetras.add(binding.btI);
        botonesLetras.add(binding.btJ);
        botonesLetras.add(binding.btK);
        botonesLetras.add(binding.btL);
        botonesLetras.add(binding.btM);
        botonesLetras.add(binding.btN);
        botonesLetras.add(binding.btEnye);
        botonesLetras.add(binding.btO);
        botonesLetras.add(binding.btP);
        botonesLetras.add(binding.btQ);
        botonesLetras.add(binding.btR);
        botonesLetras.add(binding.btS);
        botonesLetras.add(binding.btT);
        botonesLetras.add(binding.btU);
        botonesLetras.add(binding.btV);
        botonesLetras.add(binding.btW);
        botonesLetras.add(binding.btX);
        botonesLetras.add(binding.btY);
        botonesLetras.add(binding.btZ);
    }

    private fun iniciarJuego() {
        vidasRestantes = 7
        ganador = false
        reiniciarComponentes()
        reiniciarBotones()
        cambiarImagen(vidasRestantes)
        setPalabraEnJuego()
        // la funcion setPalabraEnJuegoConApi() obtiene la palabra que se va a jugar desde una Api,
        // esta pueden traer palabras con tilde lo cual no se maneja aqui en el codigo,
        // sientete libre de intentar de alguna forma manejar esto.
//        setPalabraEnJuegoConApi()
        Thread.sleep(3000)
        mostrarPalabra()
    }

    private fun reiniciarComponentes() {
        componentesLetras = ArrayList<TextView>()
        binding.filaLetras.removeAllViews();
    }

    private fun reiniciarBotones() {
        for (btn in botonesLetras){
            btn.isEnabled = true;
        }
    }

    private fun mostrarPalabra() {
        var tamano = palabraEnJuego.length;

        val minWidthInDp = (100 * resources.displayMetrics.density).toInt()

        for (index in 0..tamano - 1){

            val textView = TextView(this).apply {
                background = getDrawable(R.drawable.bottom_border)
                textSize = 25f
                minWidth = minWidthInDp
                setPadding(8)
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

        palabraEnJuego = palabras.random();

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
                    var res = response.body?.string();
                    palabraEnJuego = res.toString().substring(2, res?.length!! - 2)
                    Log.d("MainActivity", "Response: ${palabraEnJuego}")
                }
            }
        })

    }

    private fun setMyListeners(){

        for (btn in botonesLetras){
            btn.setOnClickListener(this);
        }
        
        binding.btnFlotante.setOnClickListener(this)
        
    }

    override fun onClick(btn: View?) {

        when(btn){
            binding.btnFlotante -> iniciarJuego()
            else -> if (vidasRestantes != 0 && !ganador) onLetterPress(btn as AppCompatButton) else return
        }

    }

    private fun onLetterPress(btn: AppCompatButton) {

        val letra = btn.text[0];

        val contieneLetra = palabraEnJuego.contains(letra, true)

        if(contieneLetra){
            var tamano = palabraEnJuego.length

            for (index in 0..tamano - 1){
                if(palabraEnJuego[index].equals(letra, true)){
                    componentesLetras[index].text = toUpperCase(letra.toString());
                }
            }

            var counter = 0;
            for (textView in componentesLetras){
                if(textView.text.toString() != ""){
                    counter++
                }
            }

            if(counter == tamano){
                Toast.makeText(this, "Felicidades, Has Ganado!!!", Toast.LENGTH_LONG).show()
                ganador = true;
            }

        }else{
            vidasRestantes--

            cambiarImagen(vidasRestantes)

        }

        btn.isEnabled = false;
    }

    private fun cambiarImagen(vidas: Int){

        var imagenes = arrayOf(R.drawable.ultimo_png, R.drawable.septimo_png, R.drawable.sexto_png, R.drawable.quinto_png, R.drawable.cuarto_png, R.drawable.tercero_png, R.drawable.segundo_png, R.drawable.primero_png);

        binding.imgAhorcado.setImageResource(imagenes[vidas]);

    }


}