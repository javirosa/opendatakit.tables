package yoonsung.odk.spreadsheet.activities;

import java.util.ArrayList;
import java.util.List;
import yoonsung.odk.spreadsheet.data.ColumnProperties;
import yoonsung.odk.spreadsheet.data.DataManager;
import yoonsung.odk.spreadsheet.data.DbHelper;
import yoonsung.odk.spreadsheet.data.Query;
import yoonsung.odk.spreadsheet.data.UserTable;
import yoonsung.odk.spreadsheet.view.graphs.LineChart;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class LineGraphDisplayActivity extends Activity
        implements DisplayActivity {
    
    private static final int RCODE_ODKCOLLECT_ADD_ROW = 0;
    
    private DataManager dm;
    private Controller c;
    private Query query;
    private UserTable table;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = new Controller(this, this, getIntent().getExtras());
        dm = new DataManager(DbHelper.getDbHelper(this));
        query = new Query(dm.getAllTableProperties(), c.getTableProperties());
        init();
    }
    
    @Override
    public void init() {
        query.clear();
        query.loadFromUserQuery(c.getSearchText());
        ColumnProperties xCol = c.getTableViewSettings().getLineXCol();
        ColumnProperties yCol = c.getTableViewSettings().getLineYCol();
        List<Double> xValues = new ArrayList<Double>();
        List<Double> yValues = new ArrayList<Double>();
        table = c.getIsOverview() ?
                c.getDbTable().getUserOverviewTable(query) :
                c.getDbTable().getUserTable(query);
        int xIndex = c.getTableProperties().getColumnIndex(
                xCol.getColumnDbName());
        int yIndex = c.getTableProperties().getColumnIndex(
                yCol.getColumnDbName());
        for (int i = 0; i < table.getHeight(); i++) {
            xValues.add(Double.valueOf(table.getData(i, xIndex)));
            yValues.add(Double.valueOf(table.getData(i, yIndex)));
        }
        c.setDisplayView(new LineChart(this, xValues, yValues));
        setContentView(c.getWrapperView());
    }
    
    @Override
    public void onBackPressed() {
        c.onBackPressed();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        switch (requestCode) {
        case RCODE_ODKCOLLECT_ADD_ROW:
            c.addRowFromOdkCollectForm(
                    Integer.valueOf(data.getData().getLastPathSegment()));
            init();
            break;
        default:
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        c.buildOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return c.handleMenuItemSelection(item.getItemId());
    }
    
    @Override
    public void onSearch() {
        c.recordSearch();
        init();
    }
    
    @Override
    public void onAddRow() {
        Intent intent = c.getIntentForOdkCollectAddRow();
        if (intent != null) {
            startActivityForResult(intent, RCODE_ODKCOLLECT_ADD_ROW);
        }
    }
}
