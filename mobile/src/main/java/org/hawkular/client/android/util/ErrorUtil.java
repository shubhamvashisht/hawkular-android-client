package org.hawkular.client.android.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;


public class ErrorUtil {
    /**
     * This method is used to set error message to the EditText.
     * @param editText This is EditText.
     * @param errorMessage This is String resource to be shown as error.
     */

    public static void showError(Context context, EditText editText , @StringRes int errorMessage){
        editText.setError(context.getString(errorMessage));
    }

    /**
     * This method is used to show error message in a snackbar.
     * @param view This is the view to find a parent from.
     * @param errorMessage String resource to be shown as error.
     */

    public static void showError(View view, @StringRes int errorMessage){
        Snackbar.make(view.findViewById(android.R.id.content),errorMessage,Snackbar.LENGTH_LONG).show();
    }

    /**
     * This method is used to show error message in layout corresponding to a fragment.
     */

    public static void showError(Fragment fragment, int animatorId, int viewId){
        ViewDirector.of(fragment).using(animatorId).show(viewId);
    }

}
