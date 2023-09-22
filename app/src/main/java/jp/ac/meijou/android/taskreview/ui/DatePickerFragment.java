package jp.ac.meijou.android.taskreview.ui;

import static jp.ac.meijou.android.taskreview.room.ToDo.DATE_FORMAT;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import jp.ac.meijou.android.taskreview.databinding.ActivityRegisterBinding;

public class DatePickerFragment extends DialogFragment implements OnDateSetListener {
    /**
     * 時刻を表示するテキストビュー
     */
    private EditText editText;

    /**
     * コンストラクタ<br>
     * @param editText 時刻を表示するテキストビュー
     */
    public DatePickerFragment(EditText editText) {
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
        final var calender = Calendar.getInstance();
        var year = calender.get(Calendar.YEAR);
        var month = calender.get(Calendar.MONTH) + 1;  // Calender.MONTHは0から始まるので+1する
        var day = calender.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(requireActivity(), this, year, month, day);
    }

    /**
     * 日付が選択されたときに呼び出されるコールバックメソッド<br>
     * @param view view
     * @param year 年
     * @param month 月
     * @param dayOfMonth 日
     */
    @SuppressLint("DefaultLocale")
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        editText.setText(String.format(DATE_FORMAT, year, month, dayOfMonth));
    }
}
