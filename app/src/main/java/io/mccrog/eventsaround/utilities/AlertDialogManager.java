package io.mccrog.eventsaround.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.NumberPicker;

import io.mccrog.eventsaround.R;

public class AlertDialogManager {

    public void showAlertDialog(Context context, String title, String message,
                                Boolean status, DialogInterface.OnClickListener onClickListener) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);

        if (status != null)
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.retry), onClickListener);
        alertDialog.show();
    }

    public void showNumberPickerDialog(Context context, String title, String message,
                                       NumberPicker.OnValueChangeListener valueChangeListener) {
        final NumberPicker numberPicker = new NumberPicker(context);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(1000);

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,context.getString(R.string.set), (dialog, which) ->
                valueChangeListener.onValueChange(numberPicker, numberPicker.getValue(), numberPicker.getValue()));

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,context.getString(R.string.cancel), (dialog, which) -> {
        });

        alertDialog.setView(numberPicker);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
