package blove.baidupcs.service.response.files;

import java.util.List;

import blove.baidupcs.service.response.BasicResponse;

public class CopyOrMoveResponse extends BasicResponse {
	private Extra extra;

	/**
	 * 包含拷贝或移动的信息。
	 * 
	 * @return
	 */
	public Extra getExtra() {
		return extra;
	}

	@Override
	public String toString() {
		return "CopyOrMoveResponse [\n\textra=" + extra + "\n]";
	}

	public static class Extra {
		private List<FromTo> list;

		/**
		 * 拷贝或移动信息列表。
		 * 
		 * @return
		 */
		public List<FromTo> getList() {
			return list;
		}

		@Override
		public String toString() {
			return "Extra [\n\tlist=" + list + "\n]";
		}

	}

	public static class FromTo {
		private String from;
		private String to;

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

		@Override
		public String toString() {
			return "FromTo [\n\tfrom=" + from + "\n\tto=" + to + "\n]";
		}

	}
}
