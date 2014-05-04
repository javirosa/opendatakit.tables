package org.opendatakit.tables.utils;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.common.android.data.ColumnProperties;
import org.opendatakit.common.android.data.TableProperties;
import org.opendatakit.common.android.data.UserTable;
import org.opendatakit.tables.activities.AbsBaseActivity;
import org.opendatakit.tables.types.FormType;
import org.opendatakit.tables.utils.SurveyUtil.SurveyFormParameters;

import android.content.Intent;
import android.util.Log;

public class ActivityUtil {
  
  private static final String TAG = ActivityUtil.class.getSimpleName();
  
  /*
   * Examples for how this is done elsewhere can be found in:
   * Examples for how this is done in Collect can be found in the Collect code
   * in org.odk.collect.android.tasks.SaveToDiskTask.java, in the
   * updateInstanceDatabase() method.
   */
  public static void editRow(
      AbsBaseActivity activity,
      UserTable table,
      int rowNum) {
    TableProperties tp = table.getTableProperties();
    FormType formType = FormType.constructFormType(tp);
    if ( formType.isCollectForm() ) {
      Map<String, String> elementKeyToValue = new HashMap<String, String>();
      for (ColumnProperties cp : tp.getDatabaseColumns().values()) {
        String value = table.getData(
            rowNum,
            tp.getColumnIndex(cp.getElementKey()));
        elementKeyToValue.put(cp.getElementKey(), value);
      }

      Intent intent = CollectUtil.getIntentForOdkCollectEditRow(
          activity,
          tp,
          elementKeyToValue,
          null,
          null,
          null,
          table.getRowAtIndex(rowNum).getRowId());

      if (intent != null) {
        CollectUtil.launchCollectToEditRow(activity, intent,
            table.getRowAtIndex(rowNum).getRowId());
      } else {
        Log.e(TAG, "intent null when trying to create for edit row.");
      }
    } else {
      SurveyFormParameters params = formType.getSurveyFormParameters();

      Intent intent = SurveyUtil.getIntentForOdkSurveyEditRow(
          activity,
          tp,
          activity.getAppName(),
          params,
          table.getRowAtIndex(rowNum).getRowId());
      if ( intent != null ) {
        SurveyUtil.launchSurveyToEditRow(activity, intent, tp,
            table.getRowAtIndex(rowNum).getRowId());
      }
    }
  }

}
