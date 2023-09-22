package jp.ac.meijou.android.taskreview.ui;

import static jp.ac.meijou.android.taskreview.room.ToDo.DATE_FORMAT;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import jp.ac.meijou.android.taskreview.databinding.ActivityRegisterBinding;

/**
 * 日付を選択するダイアログを表示するクラス<br>
 * カレンダー式のダイヤログを表示し、{@link EditText}には入力された日付を表示する。
 */
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
        if(editText.getText().toString().isEmpty()) {
            final var localDate = LocalDate.now();
            var year = localDate.getYear();
            var month = localDate.getMonthValue() - 1; // DatePickerDialogの月は0から始まる
            var day = localDate.getDayOfMonth();
            return new DatePickerDialog(requireActivity(), this, year, month, day);
        } else {
            final var date = editText.getText().toString().split("-");
            var year = Integer.parseInt(date[0]);
            var month = Integer.parseInt(date[1]) - 1; // DatePickerDialogの月は0から始まる
            var day = Integer.parseInt(date[2]);
            return new DatePickerDialog(requireActivity(), this, year, month, day);
        }
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
        // DatePickerDialogの月は0から始まるので、+ 1する
        editText.setText(String.format(DATE_FORMAT, year, month + 1, dayOfMonth));
    }
}
