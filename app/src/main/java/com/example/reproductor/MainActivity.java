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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button play_pause, btn_repetir, btn_anterior, btn_siguiente, btn_detener, btn_random;
    ImageButton btn_like;
    SeekBar seekBar, volumeBar;
    ImageView iv;
    int posicion = 0;
    MediaPlayer[] vectormp = new MediaPlayer[8];
    Handler handler = new Handler();
    boolean isLiked = false;
    boolean isRandom = false;
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
        btn_random = findViewById(R.id.btn_random);
        seekBar = findViewById(R.id.seekBar);
        volumeBar = findViewById(R.id.volumeBar);
        iv = findViewById(R.id.imageView);

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
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Inicialización de la imagen asociada a la canción actual
        actualizarImagen();

        // Inicialización del BroadcastReceiver para cambios en el volumen
        volumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    volumeBar.setProgress(currentVolume);
                }
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
            play_pause.setBackgroundResource(R.drawable.ic_reproducir);
            Toast.makeText(this, "Pausa", Toast.LENGTH_SHORT).show();
            handler.removeCallbacksAndMessages(null);
        } else {
            vectormp[posicion].start();
            play_pause.setBackgroundResource(R.drawable.ic_pausa);
            Toast.makeText(this, "Play", Toast.LENGTH_SHORT).show();
            seekBar.setProgress(0);
            actualizarSeekBar();
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
    }

    // Método para repetir o no repetir la canción
    public void Repetir(View view) {
        if (vectormp[posicion] != null) {
            if (vectormp[posicion].isLooping()) {
                vectormp[posicion].setLooping(false);
                btn_repetir.setBackgroundResource(R.drawable.ic_norepetir);
                Toast.makeText(this, "No repetir", Toast.LENGTH_SHORT).show();
            } else {
                vectormp[posicion].setLooping(true);
                btn_repetir.setBackgroundResource(R.drawable.ic_repetir);
                Toast.makeText(this, "Repetir", Toast.LENGTH_SHORT).show();
            }
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
        vectormp[posicion] = MediaPlayer.create(this, getMediaResource(posicion));
        vectormp[posicion].start();
        play_pause.setBackgroundResource(R.drawable.ic_pausa);
        actualizarImagen();
        handler.removeCallbacksAndMessages(null);
        seekBar.setProgress(0);
        actualizarSeekBar();
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
        vectormp[posicion] = MediaPlayer.create(this, getMediaResource(posicion));
        vectormp[posicion].start();
        play_pause.setBackgroundResource(R.drawable.ic_pausa);
        actualizarImagen();
        handler.removeCallbacksAndMessages(null);
        seekBar.setProgress(0);
        actualizarSeekBar();
    }

    // Método para cambiar entre modo aleatorio y no aleatorio
    public void Random(View view) {
        isRandom = !isRandom;
        if (isRandom) {
            btn_random.setBackgroundResource(R.drawable.random);
            Toast.makeText(this, "Modo aleatorio activado", Toast.LENGTH_SHORT).show();
        } else {
            btn_random.setBackgroundResource(R.drawable.norandom);
            Toast.makeText(this, "Modo aleatorio desactivado", Toast.LENGTH_SHORT).show();
            playedPositions.clear();
        }
    }

    // Método para cambiar el estado de "Like" de una canción
    public void LikeNoLike(View view) {
        isLiked = !isLiked;
        if (isLiked) {
            btn_like.setImageResource(R.drawable.like2);
            Toast.makeText(this, "Liked", Toast.LENGTH_SHORT).show();
        } else {
            btn_like.setImageResource(R.drawable.nolike);
            Toast.makeText(this, "No Liked", Toast.LENGTH_SHORT).show();
        }
    }

    // Actualizar la imagen asociada a la canción actual
    private void actualizarImagen() {
        int[] imageResources = {
                R.drawable.song,
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
        switch (position) {
            case 0:
                return R.raw.pista_uno;
            case 1:
                return R.raw.pista_dos;
            case 2:
                return R.raw.pista_tres;
            case 3:
                return R.raw.pista_cuatro;
            case 4:
                return R.raw.pista_cicno;
            case 5:
                return R.raw.pista_seis;
            case 6:
                return R.raw.pista_siete;
            case 7:
                return R.raw.pista_ocho;
            default:
                return R.raw.pista_uno;
        }
    }

    // Actualizar la barra de progreso del MediaPlayer actual
    private void actualizarSeekBar() {
        if (vectormp[posicion] != null) {
            seekBar.setMax(vectormp[posicion].getDuration());
            handler.postDelayed(updateSeekBar, 1000);
        }
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
