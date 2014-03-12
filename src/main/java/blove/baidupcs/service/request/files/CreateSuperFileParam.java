package blove.baidupcs.service.request.files;

import java.util.List;

/**
 * 用于合并分片文件接口的参数类型。
 * 
 * @author blove
 * 
 */
public class CreateSuperFileParam {
	private List<String> block_list;

	/**
	 * 新建一个实例。
	 * 
	 * @param block_list
	 *            子文件内容的MD5列表。
	 */
	public CreateSuperFileParam(List<String> block_list) {
		this.block_list = block_list;
	}

	/**
	 * 子文件内容的MD5列表。
	 * 
	 * @return
	 */
	public List<String> getBlock_list() {
		return block_list;
	}

}
