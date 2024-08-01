package com.example.reproductor;

import android.content.BroadcastReceiver;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;

    Button play_pause, btn_repetir, btn_anterior, btn_siguiente, btn_random, btn_like, btn_addlist;
    SeekBar seekBar;
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
        btn_like = findViewById(R.id.btn_like);
        btn_addlist = findViewById(R.id.btn_addlist);
        btn_random = findViewById(R.id.btn_random);
        seekBar = findViewById(R.id.seekBar);
        titleSong = findViewById(R.id.title_song);
        iv = findViewById(R.id.imageView);
        timerAbsolute = findViewById(R.id.timer_absolute);
        timerNegative = findViewById(R.id.timer_negative);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            // Si el usuario no está autenticado, redirigir al usuario a la pantalla de inicio de sesión
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

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
        btn_random.setOnClickListener(this::Random);
        btn_addlist.setOnClickListener(this::AddToList);

        // Configuración del SeekBar para la canción actual
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Si el usuario mueve la seekbar, actualiza el MediaPlayer
                if (vectormp[posicion] != null && fromUser) {
                    vectormp[posicion].seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Opcional: Puedes hacer algo cuando el usuario empieza a mover la seekbar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Opcional: Puedes hacer algo cuando el usuario deja de mover la seekbar
            }
        });

        // Inicialización de la imagen asociada a la canción actual
        actualizarImagen();

        // Actualizar el tiempo total de la primera canción
        actualizarDuracionTotal();

        // Registro del BroadcastReceiver
        IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, filter);
    }

    // Método para iniciar el contador del tiempo transcurrido y actualizar la SeekBar
    private void iniciarContadorYActualizarSeekBar() {
        // Detener el runnable actual si existe
        handler.removeCallbacksAndMessages(null);

        // Configurar un nuevo runnable para actualizar el contador y la SeekBar
        runnable = new Runnable() {
            @Override
            public void run() {
                if (vectormp[posicion] != null && vectormp[posicion].isPlaying()) {
                    // Obtener la posición actual de la canción
                    int currentPosition = vectormp[posicion].getCurrentPosition();

                    // Actualizar el tiempo transcurrido en el TextView
                    int seconds = (currentPosition / 1000) % 60;
                    int minutes = (currentPosition / (1000 * 60)) % 60;
                    String time = String.format("%02d:%02d", minutes, seconds);
                    timerNegative.setText(time);

                    // Actualizar la SeekBar
                    seekBar.setProgress(currentPosition);

                    // Configurar la SeekBar para el máximo de la duración de la canción
                    seekBar.setMax(vectormp[posicion].getDuration());
                }

                // Ejecutar este runnable nuevamente después de 1 segundo
                handler.postDelayed(this, 1000);
            }
        };

        // Iniciar el runnable por primera vez
        handler.post(runnable);
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
            // Si la canción está reproduciéndose, pausarla
            vectormp[posicion].pause();
            play_pause.setBackgroundResource(R.drawable.play);
            // Detener el contador y la actualización de la seekbar
            handler.removeCallbacks(updateSeekBar);
        } else {
            // Si la canción está pausada, iniciarla
            vectormp[posicion].start();
            play_pause.setBackgroundResource(R.drawable.pause);
            // Actualizar la seekbar y el contador
            actualizarSeekBar();
            actualizarDuracionTotal();
            iniciarContadorYActualizarSeekBar();
        }
    }

    // Método para repetir o no repetir la canción
    public void Repetir(View view) {
        if (vectormp[posicion] != null) {
            if (vectormp[posicion].isLooping()) {
                vectormp[posicion].setLooping(false);
                btn_repetir.setBackgroundResource(R.drawable.repeat);
            } else {
                vectormp[posicion].setLooping(true);
                btn_repetir.setBackgroundResource(R.drawable.repeat_1);
            }
        } else {
        }
    }

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
        iniciarContadorYActualizarSeekBar();
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
        iniciarContadorYActualizarSeekBar();
    }

    // Método para cambiar entre modo aleatorio y no aleatorio
    public void Random(View view) {
        if (vectormp[posicion] != null) {
            isRandom = !isRandom;

            if (isRandom) {
                btn_random.setBackgroundResource(R.drawable.random_on);
                playedPositions.clear();
            } else {
                btn_random.setBackgroundResource(R.drawable.random_off);
            }
        }
    }

    // Método para agregar a me gusta o eliminarlo de la lista de me gusta
    public void LikeNoLike(View view) {
        isLiked = !isLiked;
        if (isLiked) {
            btn_like.setBackgroundResource(R.drawable.like_on);
            //Toast.makeText(this, "Se ha añadido a tus 'Me gusta'", Toast.LENGTH_SHORT).show();
        } else {
            btn_like.setBackgroundResource(R.drawable.like_off);
            //Toast.makeText(this, "Se ha eliminado de tus 'Me gusta'", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para agregar o eliminar de la lista de reproducción
    public void AddToList(View view) {
        if (vectormp[posicion] != null) {
            if (vectormp[posicion].isLooping()) {
                vectormp[posicion].setLooping(false);
                btn_addlist.setBackgroundResource(R.drawable.playlist_add);
                //Toast.makeText(this, "Se ha borrado de tu lista", Toast.LENGTH_SHORT).show();
            } else {
                vectormp[posicion].setLooping(true);
                btn_addlist.setBackgroundResource(R.drawable.playlist_add_check);
                //Toast.makeText(this, "Se ha añadido a tu lista", Toast.LENGTH_SHORT).show();
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
            // Actualizar la seekbar cada segundo
            handler.postDelayed(updateSeekBar, 1000);

            // Listener para detectar el final de la canción
            vectormp[posicion].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // Detener y liberar el MediaPlayer actual
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                        vectormp[posicion] = null;
                    }

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

                    // Eliminar todos los callbacks y mensajes del handler
                    handler.removeCallbacks(updateSeekBar);

                    // Actualizar la SeekBar
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
    // Runnable para actualizar la seekbar cada segundo
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
}