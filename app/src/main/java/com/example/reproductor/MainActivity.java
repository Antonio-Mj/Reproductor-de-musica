package com.example.reproductor;

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

public class MainActivity extends AppCompatActivity {

    Button play_pause, btn_repetir, btn_anterior, btn_siguiente, btn_detener;
    ImageButton btn_like;
    SeekBar seekBar, volumeBar;
    MediaPlayer mp;
    ImageView iv;
    int repetir = 2, posicion = 0;
    MediaPlayer vectormp[] = new MediaPlayer[3];
    Handler handler = new Handler();
    boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play_pause = findViewById(R.id.btn_play);
        btn_repetir = findViewById(R.id.btn_norepetir);
        btn_anterior = findViewById(R.id.btn_anterior);
        btn_siguiente = findViewById(R.id.btn_siguiente);
        btn_detener = findViewById(R.id.btn_detener);
        btn_like = findViewById(R.id.btn_like);
        seekBar = findViewById(R.id.seekBar);
        volumeBar = findViewById(R.id.volumeBar);
        iv = findViewById(R.id.imageView);

        vectormp[0] = MediaPlayer.create(this, R.raw.pista_uno);
        vectormp[1] = MediaPlayer.create(this, R.raw.pista_dos);
        vectormp[2] = MediaPlayer.create(this, R.raw.pista_tres);

        btn_like.setOnClickListener(this::LikeNoLike);

        actualizarImagen();

        play_pause.setOnClickListener(this::PlayPause);
        btn_repetir.setOnClickListener(this::Reproducir);
        btn_anterior.setOnClickListener(this::Anterior);
        btn_siguiente.setOnClickListener(this::Siguiente);
        btn_detener.setOnClickListener(this::Stop);

        volumeBar.setMax(100);
        volumeBar.setProgress(50);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                if (vectormp[posicion] != null) {
                    vectormp[posicion].setVolume(volume, volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

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
            actualizarseekBar();
        }
    }

    public void Stop(View view) {
        if (vectormp[posicion] != null) {
            vectormp[posicion].stop();
            vectormp[0] = MediaPlayer.create(this, R.raw.pista_uno);
            vectormp[1] = MediaPlayer.create(this, R.raw.pista_dos);
            vectormp[2] = MediaPlayer.create(this, R.raw.pista_tres);
            posicion = 0;
            play_pause.setBackgroundResource(R.drawable.ic_reproducir);
            iv.setImageResource(R.drawable.song);
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            handler.removeCallbacksAndMessages(null);
            seekBar.setProgress(0);
        }
    }

    public void Reproducir(View view) {
        if (repetir == 1) {
            btn_repetir.setBackgroundResource(R.drawable.ic_norepetir);
            vectormp[posicion].setLooping(false);
            repetir = 2;
            Toast.makeText(this, "No repetir", Toast.LENGTH_SHORT).show();
        } else {
            btn_repetir.setBackgroundResource(R.drawable.ic_repetir);
            vectormp[posicion].setLooping(true);
            repetir = 1;
            Toast.makeText(this, "Repetir", Toast.LENGTH_SHORT).show();
        }
    }

    public void Siguiente(View view) {
        if (repetir == 1) {
            vectormp[posicion].seekTo(0);
            vectormp[posicion].start();
            Toast.makeText(this, "Repetir canción actual", Toast.LENGTH_SHORT).show();
            handler.removeCallbacksAndMessages(null);
            seekBar.setProgress(0);
            actualizarseekBar();
        } else {
            if (posicion < vectormp.length - 1) {
                if (vectormp[posicion].isPlaying()) {
                    vectormp[posicion].stop();
                    vectormp[posicion].release();
                }
                posicion++;
                vectormp[posicion] = MediaPlayer.create(this, getMediaResource(posicion));
                vectormp[posicion].start();
                play_pause.setBackgroundResource(R.drawable.ic_pausa);
                actualizarImagen();
                handler.removeCallbacksAndMessages(null);
                seekBar.setProgress(0);
                actualizarseekBar();
            } else {
                Toast.makeText(this, "No hay más canciones", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Anterior(View view) {
        if (repetir == 1) {
            vectormp[posicion].seekTo(0);
            vectormp[posicion].start();
            Toast.makeText(this, "Repetir canción actual", Toast.LENGTH_SHORT).show();
            handler.removeCallbacksAndMessages(null);
            seekBar.setProgress(0);
            actualizarseekBar();
        } else {
            if (posicion > 0) {
                if (vectormp[posicion].isPlaying()) {
                    vectormp[posicion].stop();
                    vectormp[posicion].release();
                }
                posicion--;
                vectormp[posicion] = MediaPlayer.create(this, getMediaResource(posicion));
                vectormp[posicion].start();
                play_pause.setBackgroundResource(R.drawable.ic_pausa);
                actualizarImagen();
                handler.removeCallbacksAndMessages(null);
                seekBar.setProgress(0);
                actualizarseekBar();
            } else {
                Toast.makeText(this, "No hay canciones anteriores", Toast.LENGTH_SHORT).show();
            }
        }
    }

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

    private void actualizarImagen() {
        if (posicion == 0) {
            iv.setImageResource(R.drawable.song);
        } else if (posicion == 1) {
            iv.setImageResource(R.drawable.song2);
        } else if (posicion == 2) {
            iv.setImageResource(R.drawable.song3);
        }
    }

    private int getMediaResource(int position) {
        switch (position) {
            case 0:
                return R.raw.pista_uno;
            case 1:
                return R.raw.pista_dos;
            case 2:
                return R.raw.pista_tres;
            default:
                return R.raw.pista_uno;
        }
    }

    private void actualizarseekBar() {
        if (vectormp[posicion] != null) {
            seekBar.setMax(vectormp[posicion].getDuration());
            handler.postDelayed(updateSeekBar, 1000);
        }
    }

    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (vectormp[posicion] != null) {
                seekBar.setProgress(vectormp[posicion].getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }
    };
}
