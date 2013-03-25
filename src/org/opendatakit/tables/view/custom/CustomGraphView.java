package org.opendatakit.tables.view.custom;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opendatakit.common.android.utilities.ODKFileUtils;
import org.opendatakit.tables.activities.BarGraphDisplayActivity;
import org.opendatakit.tables.activities.Controller;
import org.opendatakit.tables.activities.ListDisplayActivity;
import org.opendatakit.tables.data.ColumnProperties;
import org.opendatakit.tables.data.DbHelper;
import org.opendatakit.tables.data.KeyValueStore;
import org.opendatakit.tables.data.KeyValueStoreHelper;
import org.opendatakit.tables.data.Table;
import org.opendatakit.tables.data.TableProperties;
import org.opendatakit.tables.data.UserTable;
import org.opendatakit.tables.data.KeyValueStoreHelper.AspectHelper;
import org.opendatakit.tables.util.TableFileUtils;
import org.opendatakit.tables.view.custom.CustomView.Control;
import org.opendatakit.tables.view.custom.CustomView.TableData;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class CustomGraphView extends CustomView {


	private static final String DEFAULT_HTML =
			"<html><body>" +
					"<p>No filename has been specified.</p>" +
					"</body></html>";

	private Context context;
	private Map<String, Integer> colIndexTable;
	private TableProperties tp;
	private UserTable table;
	private String filename;
	private String graphName;
	private String potentialGraphName;
	private GraphData graphData;

	private CustomGraphView(Context context, String graphName, String potentialGraphName) {
		super(context);
		this.context = context;
		this.filename = ODKFileUtils.getAppFolder(TableFileUtils.ODK_TABLES_APP_NAME) + File.separator + "optionspane.html";
		this.graphName = graphName;
		this.potentialGraphName = potentialGraphName;
		Log.i("CustomGraphView", "IDDD: " + graphName);
		colIndexTable = new HashMap<String, Integer>();
	}

	public static CustomGraphView get(Context context, TableProperties tp,
			UserTable table, String graphName, String potentialGraphName) {
		CustomGraphView ctv = new CustomGraphView(context, graphName, potentialGraphName);
		ctv.set(tp, table);
		return ctv;
	}

	private void set(TableProperties tp, UserTable table) {
		this.tp = tp;
		this.table = table;
		colIndexTable.clear();
		ColumnProperties[] cps = tp.getColumns();
		for (int i = 0; i < cps.length; i++) {
			colIndexTable.put(cps[i].getDisplayName(), i);
			String abbr = cps[i].getSmsLabel();
			if (abbr != null) {
				colIndexTable.put(abbr, i);
			}
		}
		graphData = new GraphData(graphName);
	}

	public void display() {
		webView.addJavascriptInterface(new TableControl(context), "control");
		webView.addJavascriptInterface(new TableData(tp, table), "data");
		webView.addJavascriptInterface(graphData, "graph_data");
		if (filename != null) {
			load("file:///" + filename);
		} else {
			loadData(DEFAULT_HTML, "text/html", null);
		}
		initView();
	}

	private class TableControl extends Control {

		public TableControl(Context context) {
			super(context);
		}

		@SuppressWarnings("unused")
		public boolean openItem(int index) {
			Controller.launchDetailActivity(context, tp, table, index);
			return true;
		}
	}

	public void createNewGraph(String graphName) {
		graphData.saveGraphToName(graphName);
	}

	public boolean hasGraph(String graph) {
		return graphData.hasGraph(graph);
	}
	
	public boolean graphIsModified() {
		return graphData.isModified();
	}

	/**
	 * "Unused" warnings are suppressed because the public methods of this
	 * class are meant to be called through the JavaScript interface.
	 */
	protected class GraphData {

		// These are the partition and aspect helpers for setting info in the KVS.
		private KeyValueStoreHelper kvsh;
		private AspectHelper aspectHelper;
		private String graphString;
		boolean isModified;
		private static final String GRAPH_TYPE = "graphtype";
		private static final String X_AXIS = "selectx";
		private static final String Y_AXIS = "selecty";
		private static final String R_AXIS = "selectr";

		private static final String TAG = "GraphData";

		public GraphData(String graphString) {
			isModified = false;
			this.graphString = graphString;
			this.kvsh = tp.getKeyValueStoreHelper(BarGraphDisplayActivity.KVS_PARTITION_VIEWS);
			this.aspectHelper = kvsh.getAspectHelper(this.graphString);
			this.aspectHelper = saveGraphToName(potentialGraphName);
		}
		
		/*if(graphString.equals(BarGraphDisplayActivity.DEFAULT_GRAPH)) {
			aspectHelper.deleteAllEntriesInThisAspect();
			aspectHelper = newAspectHelper;
			graphString = graphName;
		}*/

		public boolean isModified() {
			// TODO Auto-generated method stub
			return isModified;
		}

		//If the graph is DEFAULT_GRAPH then the aspectHelper field is replaced with the new name
		//and the DEFAULT_GRAPH aspect and contents are deleted
		public AspectHelper saveGraphToName(String graphName) {
			AspectHelper newAspectHelper = kvsh.getAspectHelper(graphName);
			String graphType = aspectHelper.getString(GRAPH_TYPE);
			if(graphType != null) {
				if(hasGraph(graphName)) {
					newAspectHelper.deleteAllEntriesInThisAspect();
				}
				newAspectHelper.setString(GRAPH_TYPE, getGraphType());
				if(getGraphType().equals("Bar Graph") || getGraphType().equals("Scatter Plot")) {
					newAspectHelper.setString("selectx", aspectHelper.getString(X_AXIS));
					newAspectHelper.setString("selecty", aspectHelper.getString(Y_AXIS));
				} 
				if(getGraphType().equals("Scatter Plot")) {
					newAspectHelper.setString("selectr", aspectHelper.getString(R_AXIS));
				}
			}
			else {
				newAspectHelper.setString(GRAPH_TYPE, "unset type");
			}
			return newAspectHelper;
		}

		public boolean hasGraph(String graph) {
			List<String> list = kvsh.getAspectsForPartition();
			for(String s : list) {
				Log.d("stufftotest", "in list: " + s);
				if(graph.equals(s))
					return true;
			}
			return false;
		}

		public String getGraphType() {
			String graphType = aspectHelper.getString(BarGraphDisplayActivity.GRAPH_TYPE);
			if(graphType == null || graphType.equals("unset type")) {
				return "";
			} else {
				return graphType;
			}
		}

		public String getGraphXAxis() {
			return loadSelection(X_AXIS);
		}

		public String getGraphYAxis() {
			return loadSelection(Y_AXIS);
		}

		public String getGraphRAxis() {
			return loadSelection(R_AXIS);
		}

		public void saveSelection(String aspect, String value) {
			String oldValue = aspectHelper.getString(aspect);
			if(oldValue == null || !oldValue.equals(value)) {
				isModified = true;
			}
			aspectHelper.setString(aspect, value);
		}

		private String loadSelection(String value) {
			String result =  aspectHelper.getString(value);
			if(result == null) {
				return "";
			} else {
				return result;
			}
		}

		public void deleteDefaultGraph() {
			aspectHelper.deleteAllEntriesInThisAspect();
		}
	}

	//WARNING this destroys the GraphData field object. Use only to prevent saving the default
	//graph when exiting this class
	public void deleteDefaultGraph() {
		graphData.deleteDefaultGraph();
	}
}
