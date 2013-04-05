package org.opendatakit.tables.Task;

import org.opendatakit.tables.Activity.importexport.ExportCSVActivity;
import org.opendatakit.tables.util.CsvUtil;

import android.os.AsyncTask;

public class ExportTask
        extends AsyncTask<ExportRequest, Integer, Boolean> {

  /**
	 * 
	 */
	private final ExportCSVActivity exportCSVActivity;

	/**
	 * @param exportCSVActivity
	 */
	public ExportTask(ExportCSVActivity exportCSVActivity) {
		this.exportCSVActivity = exportCSVActivity;
	}

// This says whether or not the secondary entries in the key value store
  // were written successfully. 
  public boolean keyValueStoreSuccessful = true;
    
    protected Boolean doInBackground(ExportRequest... exportRequests) {
        ExportRequest request = exportRequests[0];
        CsvUtil cu = new CsvUtil(this.exportCSVActivity);
        if (request.getIncludeProperties()) {
            return cu.exportWithProperties(this, request.getFile(),
                    request.getTableProperties().getTableId(),
                    request.getIncludeTimestamps(),
                    request.getIncludePhoneNums());
        } else {
            return cu.export(this, request.getFile(),
                    request.getTableProperties().getTableId(),
                    request.getIncludeTimestamps(),
                    request.getIncludePhoneNums());
        }
    }
    
    protected void onProgressUpdate(Integer... progress) {
        // do nothing
    }
    
    protected void onPostExecute(Boolean result) {
        this.exportCSVActivity.dismissDialog(ExportCSVActivity.EXPORT_IN_PROGRESS_DIALOG);
        if (result) {
          if (keyValueStoreSuccessful) {
            this.exportCSVActivity.showDialog(ExportCSVActivity.CSVEXPORT_SUCCESS_DIALOG);
          } else {
            this.exportCSVActivity.showDialog(ExportCSVActivity.CSVEXPORT_SUCCESS_SECONDARY_KVS_ENTRIES_FAIL_DIALOG);
          }
        } else {
            this.exportCSVActivity.showDialog(ExportCSVActivity.CSVEXPORT_FAIL_DIALOG);
        }
    }
}