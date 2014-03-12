package blove.baidupcs.service.request.files;

import java.util.List;

/**
 * 用于批量移动或批量拷贝接口的参数类型。
 * 
 * @author blove
 * 
 */
public class MoveBatchOrCopyBatchParam {
	private List<FromTo> list;

	/**
	 * 新建一个实例。
	 * 
	 * @param list
	 *            源文件地址和目标文件地址对应的列表。
	 */
	public MoveBatchOrCopyBatchParam(List<FromTo> list) {
		this.list = list;
	}

	/**
	 * 源文件地址和目标文件地址对应的列表。
	 * 
	 * @return
	 */
	public List<FromTo> getList() {
		return list;
	}

	public static class FromTo {
		private String from;
		private String to;

		public FromTo(String from, String to) {
			this.from = from;
			this.to = to;
		}

		/**
		 * 执行copy或move操作成功的源文件地址。
		 * 
		 * @return
		 */
		public String getFrom() {
			return from;
		}

		/**
		 * 执行copy或move操作成功的目标文件地址。
		 * 
		 * @return
		 */
		public String getTo() {
			return to;
		}

	}

}
