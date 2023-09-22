package jp.ac.meijou.android.taskreview.ui;

import static jp.ac.meijou.android.taskreview.room.ToDo.TIME_FORMAT_DEFAULT;
import static jp.ac.meijou.android.taskreview.room.ToDo.timeToInt;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * 時刻を選択するダイアログを表示するクラス<br>
 * ドラムロール式のダイヤログを表示し、{@link EditText}には入力された時刻を表示する。
 */
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    /**
     * 時刻を表示するテキストビュー
     */
    private EditText editText;

    /**
     * コンストラクタ<br>
     * @param editText 時刻を表示するテキストビュー
     */

    public TimePickerFragment(EditText editText) {
        super();
        this.editText = editText;
    }

    /**
     * ダイヤログが作成されるタイミングで呼び出されるメソッド<br><br>
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        var hour = 0;
        var minute = 0;
        if(!editText.getText().toString().isEmpty()) {
            final var time = timeToInt(editText.getText().toString());
            hour = time / 60;
            minute = time % 60;
        }
        return new TimePickerDialog(requireActivity(), android.R.style.Theme_Holo_Dialog, this, hour, minute, true);
    }

    /**
     * 時刻が選択されたときに呼び出されるコールバックメソッド<br>
     * @param view view
     * @param hour 時間
     * @param minute 分
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onTimeSet(TimePicker view, int hour, int minute) {
        editText.setText(hour != 0
                ? String.format(TIME_FORMAT_DEFAULT, hour, minute)
                : String.format("%02d", minute));
    }
}
