package blove.baidupcs.fs;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import blove.baidupcs.api.BaiduPcs;
import blove.baidupcs.api.error.BaiduPcsFileNotExistsException;
import blove.baidupcs.api.response.FileMetaWithExtra2;

public class BaiduPcsDirectoryStream implements DirectoryStream<Path> {

	private final List<FileMetaWithExtra2> fileInfos;
	private final BaiduPcsPath dir;

	public BaiduPcsDirectoryStream(BaiduPcsPath dir, Filter<? super Path> filter)
			throws IOException {
		try {
			this.dir = dir;

			BaiduPcs service = ((BaiduPcsFileStore) Files.getFileStore(dir))
					.getService();
			if (!service.meta(dir.toServiceString()).isDir())
				throw new NotDirectoryException(dir.toString());
			fileInfos = service.list(dir.toServiceString());
			Iterator<FileMetaWithExtra2> itr = fileInfos.iterator();
			while (itr.hasNext()) {
				Path path = dir.resolve(itr.next().getFileName());
				if (!filter.accept(path))
					itr.remove();
			}
		} catch (BaiduPcsFileNotExistsException e) {
			throw new NoSuchFileException(dir.toString());
		}
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public Iterator<Path> iterator() {
		return new Iterator<Path>() {
			Iterator<FileMetaWithExtra2> itr = fileInfos.iterator();

			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public Path next() {
				return dir.resolve(itr.next().getFileName());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"Remove in directory stream iterator is not supported.");
			}
		};
	}

}
