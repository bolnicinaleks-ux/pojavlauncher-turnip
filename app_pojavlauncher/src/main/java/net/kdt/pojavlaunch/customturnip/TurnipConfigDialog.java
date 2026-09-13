package net.kdt.pojavlaunch.customturnip;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;

public class TurnipConfigDialog {
    private AlertDialog mDialog;
    private TurnipRecyclerViewAdapter mAdapter;

    public void show() {
        refresh();
        mDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        if (mAdapter != null) {
            mAdapter.reload();
        }
    }

    public void prepare(Context context, ActivityResultLauncher<Object> installDriverLauncher) {
        RecyclerView dialogView = new RecyclerView(context);
        dialogView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mAdapter = new TurnipRecyclerViewAdapter(context);
        dialogView.setAdapter(mAdapter);

        mDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.turnip_config_dialog_title)
                .setView(dialogView)
                .setPositiveButton(R.string.turnip_driver_add, (dialog, which) -> installDriverLauncher.launch(null))
                .setNeutralButton(R.string.turnip_driver_delete, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        mDialog.setOnShowListener(dialog -> {
            Button neutralBtn = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
            neutralBtn.setOnClickListener(view -> {
                boolean isDeleting = !mAdapter.getIsDeleting();
                mAdapter.setIsDeleting(isDeleting);
                neutralBtn.setText(isDeleting ? R.string.multirt_config_setdefault : R.string.turnip_driver_delete);
            });
        });
    }
}
