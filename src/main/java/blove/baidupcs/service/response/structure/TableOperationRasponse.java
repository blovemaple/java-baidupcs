package blove.baidupcs.service.response.structure;

import blove.baidupcs.service.response.BasicResponse;

public class TableOperationRasponse extends BasicResponse {
	private int app_id;
	private String table;

	/**
	 * 应用对应的ID。
	 * 
	 * @return
	 */
	public int getApp_id() {
		return app_id;
	}

	/**
	 * 表名。
	 * 
	 * @return
	 */
	public String getTable() {
		return table;
	}

	@Override
	public String toString() {
		return "TableOperationRasponse [\n\tapp_id=" + app_id + "\n\ttable=" + table + "\n\tgetRequest_id()="
				+ getRequest_id() + "\n]";
	}

}
