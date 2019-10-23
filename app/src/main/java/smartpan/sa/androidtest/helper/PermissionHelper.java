package smartpan.sa.androidtest.helper;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smartpan.sa.androidtest.R;

public class PermissionHelper {

    private static final String TAG = "Permission Helper";
    private static final int LOCATION_REQUEST_CODE = 10;
    private static Map<String, String> labelsMap;
    private VIEW_TYPE TYPE;
    private WeakReference<Activity> activityView;
    private WeakReference<Fragment> fragmentView;

    private PermissionsListener pListener;

    private List<String> deniedPermissions = new ArrayList<>();
    private List<String> grantedPermissions = new ArrayList<>();

    public PermissionHelper(Activity view) {
        this.activityView = new WeakReference<Activity>(view);
        TYPE = VIEW_TYPE.ACTIVITY;
    }

    public PermissionHelper(Fragment view) {
        this.fragmentView = new WeakReference<Fragment>(view);
        TYPE = VIEW_TYPE.FRAGMENT;
    }

    private static String getNameFromPermission(String permission) {
        if (labelsMap == null) {
            labelsMap = new HashMap<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                labelsMap.put(Manifest.permission.READ_EXTERNAL_STORAGE, "Read Storage");
            }
            labelsMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Write Storage");
            labelsMap.put(Manifest.permission.CAMERA, "Camera");
            labelsMap.put(Manifest.permission.CALL_PHONE, "Call");
            labelsMap.put(Manifest.permission.READ_SMS, "SMS");
            labelsMap.put(Manifest.permission.RECEIVE_SMS, "Receive SMS");
            labelsMap.put(Manifest.permission.ACCESS_FINE_LOCATION, "Exact Location");
            labelsMap.put(Manifest.permission.ACCESS_COARSE_LOCATION, "Close Location");
        }
        String value = labelsMap.get(permission);
        if (value == null) {
            String[] split = permission.split("\\.");
            return split[split.length - 1];
        } else {
            return value;
        }
    }

    public void requestPermission(@NonNull String[] permissions, int request_code) {
        deniedPermissions.clear();

        if (isViewAttached() && getActivity() != null) {
            boolean allPermissionGranted = true;

            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(getActivity(), permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    allPermissionGranted = false;
                    deniedPermissions.add(permission);
                    Log.d(TAG, "denied " + permission);
                }
            }

            if (!allPermissionGranted) {
                ActivityCompat.requestPermissions(getActivity(),
                        deniedPermissions.toArray(new String[0]), request_code);
            } else {
                pListener.onPermissionGranted(request_code);
            }

        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (isViewAttached() && getActivity() != null) {
            StringBuilder permission_name = new StringBuilder();
            boolean never_ask_again = false;
            grantedPermissions.clear();

            for (String permission : deniedPermissions) {
                if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permission);
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                        never_ask_again = true;
                    }
                    permission_name.append(",");
                    permission_name.append(PermissionHelper.getNameFromPermission(permission));
                }
            }
            String res = permission_name.toString();
            deniedPermissions.removeAll(grantedPermissions);

            if (deniedPermissions.size() > 0) {
                res = res.substring(1);
                if (!never_ask_again) {
                    getRequestAgainAlertDialog(getActivity(), res, requestCode);
                } else {
                    goToSettingsAlertDialog(getActivity(), res, requestCode);
                }
            } else {
                pListener.onPermissionGranted(requestCode);
            }
        }
    }

    private void goToSettingsAlertDialog(final Activity view, String permission_name, final int request_code) {
        new AlertDialog.Builder(view)
                .setTitle(view.getString(R.string.title_permission_required))
                .setMessage(view.getString(R.string.message_need_permission, permission_name))
                .setPositiveButton(view.getString(R.string.btn_go_to_settings),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                intent.setData(Uri.parse("package:" + view.getPackageName()));
                                view.startActivity(intent);
                            }
                        })
                .setNegativeButton(view.getString(R.string.btn_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pListener.onPermissionRejectedManyTimes(deniedPermissions, request_code);
                            }
                        })
                .show();
    }

    private void getRequestAgainAlertDialog(Activity view, String permission_name, final int request_code) {
        new AlertDialog.Builder(view)
                .setTitle(view.getString(R.string.title_permission_required))
                .setMessage(view.getString(R.string.message_need_permission, permission_name))
                .setPositiveButton(view.getString(R.string.btn_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermission(deniedPermissions.toArray(new String[0]), request_code);
                            }
                        })
                .setNegativeButton(view.getString(R.string.btn_no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pListener.onPermissionRejectedManyTimes(deniedPermissions, request_code);
                            }
                        })
                .show();
    }


    private boolean isViewAttached() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView() != null;
            case FRAGMENT:
                return getFragmentView() != null;
        }
        return false;
    }

    private Activity getActivityView() {
        if (activityView != null)
            return activityView.get();
        return null;
    }

    private Fragment getFragmentView() {
        if (fragmentView != null)
            return fragmentView.get();
        return null;
    }

    private Activity getActivity() {
        switch (TYPE) {
            case ACTIVITY:
                return getActivityView();
            case FRAGMENT:
                return getFragmentView().getActivity();
        }
        return null;
    }

    public PermissionHelper setListener(PermissionsListener pListener) {
        this.pListener = pListener;
        return this;
    }

    public void onDestroy() {
        pListener = null;
        activityView = null;
        fragmentView = null;
    }

    private enum VIEW_TYPE {
        ACTIVITY, FRAGMENT
    }

    public interface PermissionsListener {

        void onPermissionGranted(int request_code);

        void onPermissionRejectedManyTimes(@NonNull List<String> rejectedPerms, int request_code);
    }

}
