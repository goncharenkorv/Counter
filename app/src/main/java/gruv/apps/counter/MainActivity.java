package gruv.apps.counter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Счетчик, состоящий из одной активити,
 * с полем TextView и двумя кнопками Увеличения/Уменьшения
 *
 * С озвучиванием нажатий на кнопки + вибро
 * С сохранением/восстановлением значения с помощью SharedPreferences
 *
 * Не забыть добавить разрешение на вибрацию в манифест
 * <uses-permission android:name="android.permission.VIBRATE" />
 *
 * Без фрагментов
 * Без настроек (нельзя отключить вибро или звук)
 *
 * @author Goncharenko Ruslan
 */
public class MainActivity extends AppCompatActivity {

    // Имя файла, где будут храниться данные (SharedPreferences)
    private static final String DATA_FILE_NAME = "counters";

    // Ключ для переменной value в файле
    private static final String VALUE_LEY = "key";

    // максимальное значение счетчика:
    private static final int MAX_VALUE = 9999;
    // минимальное значение счетчика:
    private static final int MIN_VALUE = -9999;
    // значение счетчика по-умолчанию:
    private static final int DEFAULT_VALUE = 0;

    private static final long VIBRATION_ENCREASE_DURATION = 40; // Milliseconds
    private static final long VIBRATION_DECREASE_DURATION = 60; // Milliseconds

    // значение счетчика:
    private int value = DEFAULT_VALUE;

    // текстовое поле со счетчиком:
    private TextView counterLabel;
    // кнопка увеличения:
    private Button incrementButton;
    // кнопка уменьшения:
    private Button decrementButton;

    private SharedPreferences sharedPreferences;

    private Vibrator vibrator;

    private enum Sound {INCREMENT_SOUND, DECREMENT_SOUND};

    private SoundPool soundPool;
    private SparseIntArray soundsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // счетчик (число):
        counterLabel = (TextView) findViewById(R.id.counterLabel);
        // устанавливаем слушателя нажатия:
        counterLabel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                increment();
            }
        });

        // кнопка увеличения:
        incrementButton = (Button) findViewById(R.id.incrementButton);
        // устанавливаем слушателя нажатия:
        incrementButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                increment();
            }
        });

        // кнопка уменьшения:
        decrementButton = (Button) findViewById(R.id.decrementButton);
        // устанавливаем слушателя нажатия:
        decrementButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                decrement();
            }
        });

        // Инициализируем вибратор
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Устанавливаем звуки
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundsMap = new SparseIntArray(2);
        soundsMap.put(Sound.INCREMENT_SOUND.ordinal(), soundPool.load(this, R.raw.increment_sound, 1));
        soundsMap.put(Sound.DECREMENT_SOUND.ordinal(), soundPool.load(this, R.raw.decrement_sound, 1));
    }

    /**
     * При постановке на паузу сохраняем значение переменной value
     */
    @Override
    protected void onPause() {
        super.onPause();

        // сохраняем значение переменной value
        saveValue();
    }

    /**
     * После возвращения в активность восстанавливаем значение переменной value
     */
    @Override
    protected void onResume() {
        super.onResume();

        //Считываем ранее сохраненное в SharedPreferences значение в переменную value:
        loadValue();

        // устанавлиываем значение счетчика:
        setValue(value);
    }

    /**
     * Возвращает SharedPreferences, создавая при необходимости
     *
     * @return SharedPreferences
     */
    @NonNull
    private SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getBaseContext().getSharedPreferences(DATA_FILE_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    /**
     * Считываем ранее сохраненное в SharedPreferences значение
     * в переменную value
     */
    private void loadValue() {
        if (getSharedPreferences().contains(VALUE_LEY)) {
            value = getSharedPreferences().getInt(VALUE_LEY, DEFAULT_VALUE);
        }
    }

    /**
     * Сохраняем в SharedPreferences значение переменной value
     */
    private void saveValue() {
        SharedPreferences.Editor dataEditor = getSharedPreferences().edit();
        dataEditor.putInt(VALUE_LEY, value);
        dataEditor.commit();
    }

    /**
     * Увеличивем значение переменной value на единицу,
     * если это возможно
     */
    private void increment() {
        if (value < MAX_VALUE) {
            setValue(++value);
            vibrator.vibrate(VIBRATION_ENCREASE_DURATION);
            playSound(Sound.INCREMENT_SOUND);
        }
    }

    /**
     * Уменьшаем значение переменной value на единицу,
     * если это возможно
     */
    private void decrement() {
        if (value > MIN_VALUE) {
            setValue(--value);
            vibrator.vibrate(VIBRATION_DECREASE_DURATION);
            playSound(Sound.DECREMENT_SOUND);
        }
    }

    /**
     * Проигрываем звук
     */
    private void playSound(@NonNull Sound sound) {
       float volume = getVolume();
        soundPool.play(soundsMap.get(sound.ordinal()), volume, volume, 1, 0, 1f);
    }

    /**
     * "Считываем" текущее значение громкости (для музыки)
     * */
    private float getVolume(){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return (curVolume / maxVolume);
    }

    /**
     * Проверяем корректность параметра. При необходимости - корректируем.
     * Устанавливаем значение в переменную this.value и в поле counterLabel
     * Устанавливаем доступность/недоступность кнопок Увеличения/Уменьшения
     */
    private void setValue(int value) {
        if (value > MAX_VALUE) {
            value = MAX_VALUE;
        } else if (value < MIN_VALUE) {
            value = MIN_VALUE;
        }
        this.value = value;
        counterLabel.setText(Integer.toString(value));
        checkStateOfButtons();
    }

    /**
     * Устанавливаем доступность/недоступность кнопок Увеличения/Уменьшения
     */
    private void checkStateOfButtons() {
        incrementButton.setEnabled(value < MAX_VALUE);
        decrementButton.setEnabled(value > MIN_VALUE);
    }
}
