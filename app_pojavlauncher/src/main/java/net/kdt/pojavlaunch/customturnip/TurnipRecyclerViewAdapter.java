package net.kdt.pojavlaunch.customturnip;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;

import java.util.List;

public class TurnipRecyclerViewAdapter extends RecyclerView.Adapter<TurnipRecyclerViewAdapter.DriverViewHolder> {

    private boolean mIsDeleting = false;
    private final Context mContext;
    private List<CustomTurnipManager.TurnipDriver> mDrivers;

    public TurnipRecyclerViewAdapter(Context context) {
        this.mContext = context;
        reload();
    }

    public void reload() {
        this.mDrivers = CustomTurnipManager.getInstalledDrivers(mContext);
        notifyDataSetChanged();
    }

    public boolean getIsDeleting() {
        return mIsDeleting;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setIsDeleting(boolean isDeleting) {
        this.mIsDeleting = isDeleting;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_turnip_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        holder.bind(mDrivers.get(position));
    }

    @Override
    public int getItemCount() {
        return mDrivers != null ? mDrivers.size() : 0;
    }

    public class DriverViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTitle;
        private final TextView mSubtitle;
        private final Button mSetDefaultBtn;
        private final ImageButton mRemoveBtn;

        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.turnip_driver_title);
            mSubtitle = itemView.findViewById(R.id.turnip_driver_subtitle);
            mSetDefaultBtn = itemView.findViewById(R.id.turnip_driver_setdefaultbtn);
            mRemoveBtn = itemView.findViewById(R.id.turnip_driver_removebtn);
        }

        public void bind(CustomTurnipManager.TurnipDriver driver) {
            mTitle.setText(driver.displayName);
            mSubtitle.setText(driver.description);

            boolean isActive = driver.id.equals(CustomTurnipManager.getActiveDriverId());

            if (mIsDeleting) {
                mSetDefaultBtn.setVisibility(View.GONE);
                if (driver.isCustom) {
                    mRemoveBtn.setVisibility(View.VISIBLE);
                    mRemoveBtn.setOnClickListener(v -> confirmDelete(driver));
                } else {
                    mRemoveBtn.setVisibility(View.GONE);
                }
            } else {
                mRemoveBtn.setVisibility(View.GONE);
                mSetDefaultBtn.setVisibility(View.VISIBLE);

                if (isActive) {
                    mSetDefaultBtn.setText(R.string.turnip_driver_active);
                    mSetDefaultBtn.setEnabled(false);
                } else {
                    mSetDefaultBtn.setText(R.string.multirt_config_setdefault);
                    mSetDefaultBtn.setEnabled(true);
                    mSetDefaultBtn.setOnClickListener(v -> {
                        CustomTurnipManager.setActiveDriver(driver.id);
                        reload();
                    });
                }
            }
        }

        private void confirmDelete(CustomTurnipManager.TurnipDriver driver) {
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.turnip_driver_delete_title)
                    .setMessage(mContext.getString(R.string.turnip_driver_delete_confirm, driver.displayName))
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        sExecutorService.execute(() -> {
                            boolean deleted = CustomTurnipManager.deleteDriver(driver.id);
                            Tools.runOnUiThread(() -> {
                                if (deleted) {
                                    Toast.makeText(mContext, R.string.turnip_driver_deleted_success, Toast.LENGTH_SHORT).show();
                                }
                                reload();
                            });
                        });
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }
}
