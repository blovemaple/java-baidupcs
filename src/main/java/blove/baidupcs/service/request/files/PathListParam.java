package blove.baidupcs.service.request.files;

import java.util.List;

/**
 * 用于需要路径列表的接口的参数类型。
 * 
 * @author blove
 * 
 */
public class PathListParam {
	private List<Path> list;

	/**
	 * 新建一个实例。
	 * 
	 * @param list
	 *             路径列表
	 */
	public PathListParam(List<Path> list) {
		this.list = list;
	}

	/**
	 * 路径列表。
	 * 
	 * @return
	 */
	public List<Path> getList() {
		return list;
	}

	/**
	 * 路径。
	 * 
	 * @author blove
	 */
	public static class Path {
		private String path;

		/**
		 * 新建一个实例。
		 * 
		 * @param path
		 *             路径
		 */
		public Path(String path) {
			this.path = path;
		}

		/**
		 * 路径。
		 * 
		 * @return
		 */
		public String getPath() {
			return path;
		}

	}
}
