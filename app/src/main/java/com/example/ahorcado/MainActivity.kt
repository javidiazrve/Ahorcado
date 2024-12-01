package com.example.ahorcado

import android.app.ProgressDialog
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


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding:ActivityMainBinding
    private var palabraEnJuego: String = ""
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var buttonsMediaPlayer: MediaPlayer
    private var componentesLetras: ArrayList<TextView> = ArrayList<TextView>()
    private var botonesLetras: ArrayList<AppCompatButton> = ArrayList<AppCompatButton>()
    private var vidasRestantes: Int = 7
    private var ganador: Boolean = false
    private val audiosList: MutableList<Int> = mutableListOf(
        R.raw.bruh, R.raw.peo, R.raw.the_rock, R.raw.uy_uy_uy, R.raw.frog_laughing_meme,
        R.raw.gato_riendo, R.raw.monkey_gaga, R.raw.no_estes_fumando, R.raw.oh_my_god_meme,
        R.raw.penalti_madrid, R.raw.spongebob_fail)
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

        if(savedInstanceState != null){
            restoreState(savedInstanceState)
        }else{
            iniciarJuego()
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {

        // El with obtiene los valores de el state para poder ser usados mas facilmente dentro de las llaves
        with(outState){

            // Guardamos el estado de las variables necesarias
            putString("palabraEnJuego", palabraEnJuego)
            putInt("vidasRestantes", vidasRestantes)
            putBoolean("ganador", ganador)
            putBoolean("isPlayingLastAudio", mediaPlayer.isPlaying)

            // Guardamos el estado de los botones (Activos / Inactivos) en un array de Booleanos
            val btns: MutableList<Boolean> = mutableListOf()

            for (btn in botonesLetras){
                btns.add(btn.isEnabled)
            }

            putBooleanArray("estadoBotones", btns.toBooleanArray())

            // Guardamos el texto de los TextViews (Letra / Vacio) en un array de Strings
            val letras: MutableList<String> = mutableListOf()

            for (letra in componentesLetras){
                letras.add(letra.text.toString())
            }

            putStringArray("componentesLetras",letras.toTypedArray())
        }

        detenerAllAudios()
        super.onSaveInstanceState(outState)
    }

    private fun restoreState(state: Bundle){

        // El with obtiene los valores de el state para poder ser usados mas facilmente dentro de las llaves
        with(state){
            palabraEnJuego = getString("palabraEnJuego").toString()
            vidasRestantes = getInt("vidasRestantes")
            ganador = getBoolean("ganador")

            // Restauramos el valor de los botones
            for ((index, isEnable) in getBooleanArray("estadoBotones")!!.withIndex()){
                botonesLetras[index].isEnabled = isEnable
            }

            mostrarPalabra()

            // Restauramos el valor de los TextViews
            for ((index, letra) in getStringArray("componentesLetras")!!.withIndex()){
                componentesLetras[index].text = letra
            }

            if(getBoolean("isPlayingLastAudio")){
                detenerAllAudios()
                mediaPlayer.start()
            }
        }

        checkGanador(vidasRestantes)
    }

    private fun initAudioPlayers() {
        // Inicializamos el player para el audio de victoria o derrota
        // que al ser el mismo tendria que inicializarlo una vez
        mediaPlayer = MediaPlayer.create(this, R.raw.chill_audio)
        // Hacemos que se reproduzca infinitamente
        mediaPlayer.isLooping = true

        // Inicializamos el player para el audio cuando el jugador falle
        buttonsMediaPlayer = MediaPlayer.create(this, audiosList.random())
        buttonsMediaPlayer.setOnCompletionListener {
            // Una vez el audio termine liberamos el player
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

        // Las oportunidades disponibles del jugador
        vidasRestantes = 7
        // Establece que hay un ganador o no
        ganador = false
        // Reiniciamos el estado Enabled de los botones para que esten disponibles otra vez
        reiniciarBotones()
        // Dependiendo de las vidas disponibles y si hay un ganador decide que imagen mostrar
        checkGanador(vidasRestantes)
        // Organiza aleatoriamente los audios que se van a reproducir en cada fallo
        setFailAudiosOrder()
        // Detenemos todos los audios que se esten reproduciendo
        detenerAllAudios()
        // Buscamos una palabra en la base de datos y mostramos los labels correspondientes
        setPalabraEnJuegoFirebase()

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

        // Borramos todos los componentes del LinearLayout
        reiniciarComponentes()

        val tamano = palabraEnJuego.length

        val minWidthInDp = (100 * resources.displayMetrics.density).toInt()

        for (index in 0..tamano - 1){

            // Creamos un TextView para cada letra
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

            // Lo Agregamos a la lista donde guardamos los componentes de letras
            componentesLetras.add(textView)
            // Tambien lo agregamos al LinearLayout
            binding.filaLetras.addView(textView)
        }

    }

    private fun setPalabraEnJuegoFirebase() {

        // Inicializamos el Dialog para esperar a la palabra
        var progress = ProgressDialog(this)
        // Indicamos que no se pueda cancelar dando click a la pantalla
        progress.setCancelable(false)
        // Agregamos un mensaje en el Dialog
        progress.setMessage("Cargando Palabra...")
        // Mostramos el Dialog
        progress.show()

        // Usamos la funcion del repositorio y usamos el addOnSuccessListener para esperar el resultado
        repo.getPalabraRandom().addOnSuccessListener{ result ->

            // Una vez obtenidas las palabras elegimos una random de la lista y obtenemos el valor
            // del campo palabra del documento
            palabraEnJuego = result.documents.random().get("palabra").toString()
            // Cargamos los textviews necesarios para mostrar la palabra
            mostrarPalabra()
            // Luego cerramos el Dialog
            progress.dismiss()
        }

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

        // Obtenemos la letra del boton pulsado y revisamos si palabra contiene esa letra
        val letra = btn.text[0]
        val contieneLetra = palabraEnJuego.contains(letra, true)

        if(contieneLetra){
            val tamano = palabraEnJuego.length

            // Si la contiene revisamos letra por letra de la palabra
            for (index in 0..< tamano){
                if(palabraEnJuego[index].equals(letra, true)){
                    // Si la tiene la asignamos al textview que este en la misma posicion de la letra en la palabra
                    componentesLetras[index].text = toUpperCase(letra.toString())
                }
            }

            // Revisamos cuantos aciertos hay contando los textview que no esten vacios
            var aciertos = 0
            for (textView in componentesLetras){
                if(textView.text.toString() != ""){
                    aciertos++
                }
            }

            // y si los aciertos son la misma cantidad de letras es que la palabra se completo y hay un ganador
            if(aciertos == tamano){
                ganador = true
            }

        }else{

            // Si la palabra no contiene la letra restamos una vida
            vidasRestantes--

            // Si ya no tiene vidas pues el jugador a perdido
            if(vidasRestantes == 0){
                // y procedemos a mostrar la palabra completa para que el jugador sepa cual fue
                mostrarPalabraCompleta()
            }else{
                // Si todavia tiene vidas pues reproducimos el siguiente audio de fallo.
                reproducirFailAudio(vidasRestantes)
            }

        }

        // Una vez jugado desactivamos el boton
        btn.isEnabled = false

        // Ahora chequeamos el ganador
        checkGanador(vidasRestantes)
    }

    private fun mostrarPalabraCompleta(){
        val tamano = palabraEnJuego.length

        for (index in 0..<tamano){
            componentesLetras[index].text = toUpperCase(palabraEnJuego[index].toString())
        }

    }

    private fun checkGanador(vidas: Int){

        binding.imgAhorcado.scaleType = ImageView.ScaleType.FIT_CENTER

        // Cargamos las imagenes en un array en orden descendente
        // asi que cuando las vidas bajen pueda verse la imagen que corresponde a esa cantidad de vidas restantes
        // siendo la posicion 0 la imagen de derrota asi que cuando se quede sin vidas se muestre esa imagen
        val imagenes = arrayOf(R.drawable.chill_lose, R.drawable.ultimo, R.drawable.sexto, R.drawable.quinto, R.drawable.cuarto, R.drawable.tercero, R.drawable.segundo, R.drawable.primero)

        // Si hay un ganador mostramos la imagen de victoria
        if(ganador){
            binding.imgAhorcado.setImageResource(R.drawable.chill_win)
        }else{
            // En el caso de que no haya ganador mostramos la imagen correspondiente a las vidas restantes
            binding.imgAhorcado.setImageResource(imagenes[vidas])
        }

        // verificamos si hay ganador o si las vidas se han acabado
        if(ganador || vidas == 0){
            // si es asi detenemos los audios y reproducimos el audio final
            reproducirLastAudio()
            // y ajustamos la imagen que abarque todos los pixeles de X y Y
            // asi la imagen se ve mejor
            binding.imgAhorcado.scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    private fun setFailAudiosOrder(){
        // Desordenamos aleatoriamente la lista de audios
        audiosList.shuffle()
    }

    private fun reproducirFailAudio(audioIndex: Int){
        // Verificamos si se esta reproduciendo para poder detenerlo y liberar el player para el siguiente audio
        if(buttonsMediaPlayer.isPlaying){
            buttonsMediaPlayer.stop()
            buttonsMediaPlayer.release()
        }

        // Creamos otra instancia del media player con el siguiente audio y lo reproducimos
        buttonsMediaPlayer = MediaPlayer.create(this, audiosList.get(audioIndex))
        buttonsMediaPlayer.start()
    }

    private fun reproducirLastAudio(){
        // Detenemos todos los audios y reproducimos el player del audio final
        detenerAllAudios()
        mediaPlayer.start()
    }

    private fun detenerAllAudios(){

        // Verificamos que los player se reproducen y los detenemos y liberamos
        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.prepare()
        }

        if(buttonsMediaPlayer.isPlaying){
            buttonsMediaPlayer.stop()
            buttonsMediaPlayer.reset()
        }

    }


}