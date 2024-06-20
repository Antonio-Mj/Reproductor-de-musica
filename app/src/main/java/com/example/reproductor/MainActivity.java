package com.example.reproductor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Button play_pause, btn_repetir, btn_anterior, btn_siguiente, btn_detener, btn_random, btn_like, btn_addlist;
    SeekBar seekBar, volumeBar;
    ImageView iv;
    TextView timerAbsolute, timerNegative, titleSong;
    private List<String> songTitles;
    int posicion = 0, currentSongIndex;
    MediaPlayer[] vectormp = new MediaPlayer[8];
    int position = 0;
    Handler handler = new Handler();
    Runnable runnable;
    boolean isLiked = false, isRandom = false;
    Random random = new Random();
    List<Integer> playedPositions = new ArrayList<>();
    AudioManager audioManager;
    BroadcastReceiver volumeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de los elementos de la interfaz
        play_pause = findViewById(R.id.btn_play);
        btn_repetir = findViewById(R.id.btn_norepetir);
        btn_anterior = findViewById(R.id.btn_anterior);
        btn_siguiente = findViewById(R.id.btn_siguiente);
        btn_detener = findViewById(R.id.btn_detener);
        btn_like = findViewById(R.id.btn_like);
        btn_addlist = findViewById(R.id.btn_addlist);
        btn_random = findViewById(R.id.btn_random);
        seekBar = findViewById(R.id.seekBar);
        volumeBar = findViewById(R.id.volumeBar);
        titleSong = findViewById(R.id.title_song);
        iv = findViewById(R.id.imageView);
        timerAbsolute = findViewById(R.id.timer_absolute);
        timerNegative = findViewById(R.id.timer_negative);

        // Animación para que gire la imagen
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        iv.startAnimation(rotateAnimation);

        // Lista de títulos de canciones
        songTitles = new ArrayList<>();
        songTitles.add("Paint it black - Rolling Stones");
        songTitles.add("Hielo - Eladio Carrionn");
        songTitles.add("NI BIEN NI MAL - Bad Bunny");
        songTitles.add("Carta de despedida - Lit Killah");
        songTitles.add("BÉSAME REMIX - Tiago PZK");
        songTitles.add("Una noche más - Panther");
        songTitles.add("Además de mi - Duki");
        songTitles.add("She don't give a fo - Duki");

        // Inicialización del índice de la canción actual
        posicion = 0;

        // Configuración inicial del título de la canción
        updateSongTitle();

        // Inicialización de los MediaPlayer
        initMediaPlayer();

        // Configuración de los botones y listeners
        btn_like.setOnClickListener(this::LikeNoLike);
        play_pause.setOnClickListener(this::PlayPause);
        btn_repetir.setOnClickListener(this::Repetir);
        btn_anterior.setOnClickListener(this::Anterior);
        btn_siguiente.setOnClickListener(this::Siguiente);
        btn_detener.setOnClickListener(this::Stop);
        btn_random.setOnClickListener(this::Random);
        btn_addlist.setOnClickListener(this::AddToList);

        // Configuración del volumen inicial y su control
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeBar.setMax(maxVolume);
        volumeBar.setProgress(currentVolume);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Configuración del SeekBar para la canción actual
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && vectormp[posicion] != null) {
                    vectormp[posicion].seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Inicialización de la imagen asociada a la canción actual
        actualizarImagen();

        // Iniciar el contador para la canción actual
        iniciarContador();

        // Registro del BroadcastReceiver para cambios en el volumen
        volumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    volumeBar.setProgress(currentVolume);
                }
            }
        };

        // Registro del BroadcastReceiver
        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, filter);
    }

    // Método para iniciar el contador del tiempo transcurrido
    // Método para iniciar el contador del tiempo transcurrido
    private void iniciarContador() {
        // Detener el runnable actual si existe
        handler.removeCallbacksAndMessages(null);

        // Configurar un nuevo runnable para actualizar el contador
        runnable = new Runnable() {
            @Override
            public void run() {
                if (vectormp[posicion] != null && vectormp[posicion].isPlaying()) {
                    // Obtener la posición actual de la canción
                    int currentPosition = vectormp[posicion].getCurrentPosition();

                    // Calcular el tiempo transcurrido en minutos y segundos
                    int seconds = (currentPosition / 1000) % 60;
                    int minutes = (currentPosition / (1000 * 60)) % 60;

                    // Formatear el tiempo en formato "MM:SS"
                    String time = String.format("%02d:%02d", minutes, seconds);

                    // Actualizar el TextView
                    timerNegative.setText(time);
                }

                // Ejecutar este runnable nuevamente después de 1 segundo
                handler.postDelayed(this, 1000);
            }
        };

        // Iniciar el runnable por primera vez
        handler.post(runnable);


        // Configurar un nuevo runnable para actualizar el contador
        runnable = new Runnable() {
            @Override
            public void run() {
                if (vectormp[posicion] != null && vectormp[posicion].isPlaying()) {
                    // Obtener la posición actual de la canción
                    int currentPosition = vectormp[posicion].getCurrentPosition();

                    // Calcular el tiempo transcurrido en minutos y segundos
                    int seconds = (currentPosition / 1000) % 60;
                    int minutes = (currentPosition / (1000 * 60)) % 60;

                    // Formatear el tiempo en formato "MM:SS"
                    String time = String.format("%02d:%02d", minutes, seconds);

                    // Actualizar el TextView
                    timerNegative.setText(time);
                }

                // Ejecutar este runnable nuevamente después de 1 segundo
                handler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(volumeReceiver);
    }

    // Método para inicializar los MediaPlayer
    private void initMediaPlayer() {
        vectormp[0] = MediaPlayer.create(this, R.raw.pista_uno);
        vectormp[1] = MediaPlayer.create(this, R.raw.pista_dos);
        vectormp[2] = MediaPlayer.create(this, R.raw.pista_tres);
        vectormp[3] = MediaPlayer.create(this, R.raw.pista_cuatro);
        vectormp[4] = MediaPlayer.create(this, R.raw.pista_cicno);
        vectormp[5] = MediaPlayer.create(this, R.raw.pista_seis);
        vectormp[6] = MediaPlayer.create(this, R.raw.pista_siete);
        vectormp[7] = MediaPlayer.create(this, R.raw.pista_ocho);
    }

    // Método para reproducir o pausar la canción
    public void PlayPause(View view) {
        if (vectormp[posicion].isPlaying()) {
            vectormp[posicion].pause();
            play_pause.setBackgroundResource(R.drawable.play);
            Toast.makeText(this, "Pausa", Toast.LENGTH_SHORT).show();
            handler.removeCallbacksAndMessages(null);
        } else {
            vectormp[posicion].start();
            play_pause.setBackgroundResource(R.drawable.pause);
            Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();
            seekBar.setProgress(0);
            actualizarSeekBar();
            actualizarDuracionTotal();
        }
    }

    // Método para detener la canción
    public void Stop(View view) {
        if (vectormp[posicion] != null) {
            vectormp[posicion].stop();
            vectormp[posicion].release();
            vectormp[posicion] = null;
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            handler.removeCallbacksAndMessages(null);
            seekBar.setProgress(0);
        }

        // Regresar a la primera canción
        posicion = 0;
        vectormp[posicion] = MediaPlayer.create(this, getMediaResource(posicion));
        actualizarImagen();
        actualizarDuracionTotal();
    }

    // Método para repetir o no repetir la canción
    public void Repetir(View view) {
        if (vectormp[posicion] != null) {
            if (vectormp[posicion].isLooping()) {
                vectormp[posicion].setLooping(false);
                btn_repetir.setBackgroundResource(R.drawable.repeat);
                Toast.makeText(this, "No repetir", Toast.LENGTH_SHORT).show();
            } else {
                vectormp[posicion].setLooping(true);
                btn_repetir.setBackgroundResource(R.drawable.repeat_1);
                Toast.makeText(this, "Repetir", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay una canción cargada", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para avanzar a la siguiente canción
    // Método para avanzar a la siguiente canción
    public void Siguiente(View view) {
        if (vectormp[posicion] != null) {
            vectormp[posicion].stop();
            vectormp[posicion].release();
            vectormp[posicion] = null;
        }

        if (isRandom) {
            int newPosicion;
            do {
                newPosicion = random.nextInt(vectormp.length);
            } while (newPosicion == posicion || playedPositions.contains(newPosicion));
            posicion = newPosicion;
            playedPositions.add(posicion);
            if (playedPositions.size() == vectormp.length) {
                playedPositions.clear();
            }
        } else {
            posicion = (posicion + 1) % vectormp.length;
        }

        // Reiniciar el contador de tiempo transcurrido
        timerNegative.setText("00:00");

        // Actualizar el título de la canción
        updateSongTitle();

        vectormp[posicion] = MediaPlayer.create(this, getMediaResource(posicion));
        vectormp[posicion].start();
        play_pause.setBackgroundResource(R.drawable.pause);

        // Actualizar la imagen asociada a la canción actual
        actualizarImagen();

        actualizarDuracionTotal();
        handler.removeCallbacksAndMessages(null);
        seekBar.setProgress(0);
        actualizarSeekBar();

        // Iniciar el contador de tiempo transcurrido
        iniciarContador();
    }

    // Método para retroceder a la canción anterior
    public void Anterior(View view) {
        if (vectormp[posicion] != null) {
            vectormp[posicion].stop();
            vectormp[posicion].release();
            vectormp[posicion] = null;
        }

        if (isRandom) {
            if (playedPositions.size() > 0) {
                playedPositions.remove(playedPositions.size() - 1);
            }
            if (playedPositions.size() == 0) {
                int newPosicion;
                do {
                    newPosicion = random.nextInt(vectormp.length);
                } while (newPosicion == posicion);
                posicion = newPosicion;
            } else {
                posicion = playedPositions.get(playedPositions.size() - 1);
            }
        } else {
            if (posicion > 0) {
                posicion--;
            } else {
                posicion = vectormp.length - 1;
            }
        }

        // Reiniciar el contador de tiempo transcurrido
        timerNegative.setText("00:00");

        vectormp[posicion] = MediaPlayer.create(this, getMediaResource(posicion));
        vectormp[posicion].start();
        play_pause.setBackgroundResource(R.drawable.pause);
        actualizarImagen();
        updateSongTitle();
        actualizarDuracionTotal();
        handler.removeCallbacksAndMessages(null);
        seekBar.setProgress(0);
        actualizarSeekBar();

        // Iniciar el contador de tiempo transcurrido
        iniciarContador();
    }

    // Método para cambiar entre modo aleatorio y no aleatorio
    public void Random(View view) {
        if (vectormp[posicion] != null) {
            isRandom = !isRandom;

            if (isRandom) {
                btn_random.setBackgroundResource(R.drawable.random_on);
                Toast.makeText(this, "Modo aleatorio activado", Toast.LENGTH_SHORT).show();
                playedPositions.clear(); // Limpiar la lista de posiciones reproducidas cuando se activa el modo aleatorio
            } else {
                btn_random.setBackgroundResource(R.drawable.random_off);
                Toast.makeText(this, "Modo aleatorio desactivado", Toast.LENGTH_SHORT).show();
            }
        }
    }



    // Método para cambiar el estado de "Like" de una canción
    public void LikeNoLike(View view) {
        isLiked = !isLiked;
        if (isLiked) {
            btn_like.setBackgroundResource(R.drawable.like_on);
            Toast.makeText(this, "Se ha añadido a tus 'Me gusta'", Toast.LENGTH_SHORT).show();
        } else {
            btn_like.setBackgroundResource(R.drawable.like_off);
            Toast.makeText(this, "Se ha eliminado de tus 'Me gusta'", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para cambiar entre modo aleatorio y no aleatorio
    public void AddToList(View view) {
        if (vectormp[posicion] != null) {
            if (vectormp[posicion].isLooping()) {
                vectormp[posicion].setLooping(false);
                btn_addlist.setBackgroundResource(R.drawable.playlist_add);
                Toast.makeText(this, "Se ha borrado de tu lista", Toast.LENGTH_SHORT).show();
            } else {
                vectormp[posicion].setLooping(true);
                btn_addlist.setBackgroundResource(R.drawable.playlist_add_check);
                Toast.makeText(this, "Se ha añadido a tu lista", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Actualizar la imagen asociada a la canción actual
    private void actualizarImagen() {
        int[] imageResources = {
                R.drawable.song1,
                R.drawable.song2,
                R.drawable.song3,
                R.drawable.song4,
                R.drawable.song5,
                R.drawable.song6,
                R.drawable.song7,
                R.drawable.song8
        };
        if (posicion >= 0 && posicion < imageResources.length) {
            iv.setImageResource(imageResources[posicion]);
        }
    }

    // Obtener el recurso de media (canción) según la posición
    private int getMediaResource(int position) {
        int[] mediaResources = {
                R.raw.pista_uno,
                R.raw.pista_dos,
                R.raw.pista_tres,
                R.raw.pista_cuatro,
                R.raw.pista_cicno,
                R.raw.pista_seis,
                R.raw.pista_siete,
                R.raw.pista_ocho
        };

        if (position >= 0 && position < mediaResources.length) {
            return mediaResources[position];
        } else {
            return R.raw.pista_uno;
        }
    }

    // Actualizar la barra de progreso del MediaPlayer actual
    private void actualizarSeekBar() {
        if (vectormp[posicion] != null) {
            seekBar.setMax(vectormp[posicion].getDuration());
            handler.postDelayed(updateSeekBar, 1000);

            // Listener para detectar el final de la canción
            vectormp[posicion].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Detener el MediaPlayer actual
                    mp.stop();
                    mp.release();
                    mp = null;

                    // Avanzar a la siguiente canción
                    if (isRandom) {
                        int newPosicion;
                        do {
                            newPosicion = random.nextInt(vectormp.length);
                        } while (newPosicion == posicion || playedPositions.contains(newPosicion));
                        posicion = newPosicion;
                        playedPositions.add(posicion);
                        if (playedPositions.size() == vectormp.length) {
                            playedPositions.clear();
                        }
                    } else {
                        posicion = (posicion + 1) % vectormp.length;
                    }

                    // Crear y comenzar el nuevo MediaPlayer para la siguiente canción
                    vectormp[posicion] = MediaPlayer.create(MainActivity.this, getMediaResource(posicion));
                    vectormp[posicion].start();
                    play_pause.setBackgroundResource(R.drawable.pause);

                    // Actualizar la imagen y el título de la nueva canción
                    actualizarImagen();
                    updateSongTitle();
                    actualizarDuracionTotal();
                    handler.removeCallbacksAndMessages(null);
                    seekBar.setProgress(0);
                    actualizarSeekBar();
                }
            });
        }
    }

    // Añadir el método para actualizar la duración total
    private void actualizarDuracionTotal() {
        if (vectormp[posicion] != null) {
            int duration = vectormp[posicion].getDuration();
            String durationString = formatDuration(duration);
            timerAbsolute.setText(durationString);
        }
    }

    // Añadir el método para formatear la duración
    private String formatDuration(int duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Runnable para actualizar la barra de progreso cada segundo
    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (vectormp[posicion] != null) {
                seekBar.setProgress(vectormp[posicion].getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };

    private void updateSongTitle() {
        if (posicion >= 0 && posicion < songTitles.size()) {
            titleSong.setText(songTitles.get(posicion));
        }
    }
    // Limpiar recursos al destruir la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        for (MediaPlayer mp : vectormp) {
            if (mp != null) {
                mp.release();
            }
        }
    }
    //Cerrar la app con boton de regresar
    public void Back(View view) {
        finishAffinity();
    }
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }
}

