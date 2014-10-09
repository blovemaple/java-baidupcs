package blove.baidupcs.api.error;

import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.ConversionException;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ErrorParseHandler implements ErrorHandler {
	private static final Gson GSON_INSTANCE = new Gson();

	@Override
	public Throwable handleError(RetrofitError cause) {
		Throwable oriException = cause.getCause();
		Response httpResponse = cause.getResponse();
		if (oriException == null)// http status not ok
			return handleHttpError(httpResponse);
		else if (oriException instanceof Error)// severe Error
			return oriException;
		else if (oriException instanceof IOException)// IOException
			return oriException;
		else if (oriException instanceof ConversionException) // json format
													// not match
			return new InterfaceException((ConversionException) oriException, httpResponse);
		else {// others unknown
			return new BaiduPcsException(oriException, httpResponse);
		}
	}

	private BaiduPcsException handleHttpError(Response httpResponse) {
		ErrorResponse errorResponse = null;

		if (httpResponse.getBody() != null) {
			try {
				errorResponse = GSON_INSTANCE.fromJson(new InputStreamReader(httpResponse.getBody().in()),
						ErrorResponse.class);
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				// response
				// body的json不符合ErrorResponse，或读取时出现IO异常，当作没有body好了，无视之。
			}
		}

		int httpStatus = httpResponse.getStatus();
		if (httpStatus >= 500 && httpStatus < 600) {
			// 5XX服务器错误
			return new ServerException(errorResponse, httpResponse);
		}

		if (errorResponse != null) {
			switch (errorResponse.getError_code()) {
			case "3":
			case "31023":
			case "31208":
				return new InterfaceException(errorResponse, httpResponse);
			case "4":
			case "5":
			case "31024":
			case "31064":
			case "110":
			case "111":
			case "31211":
				return new NoAuthException(errorResponse, httpResponse);
			case "31042":
			case "31043":
			case "31044":
			case "31045":
				return new UserRelatedException(errorResponse, httpResponse);
			case "31061":
				return new BaiduPcsFileExistsException(errorResponse, httpResponse);
			case "31062":
				return new BaiduPcsIllegalFileNameException(errorResponse, httpResponse);
			case "31063":
				return new BaiduPcsParentDirNotExistsException(errorResponse, httpResponse);
			case "31065":
				return new BaiduPcsDirectoryIsFullException(errorResponse, httpResponse);
			case "31066":
			case "31202":
				return new BaiduPcsFileNotExistsException(errorResponse, httpResponse);
			case "31079":
				return new BaiduPcsMD5NotExistsException(errorResponse, httpResponse);
			case "31218":
				return new OutOfStorageException(errorResponse, httpResponse);
			case "31219":
			case "31220":
			case "36013":
				return new OutOfLimitException(errorResponse, httpResponse);
			}
		}

		return new BaiduPcsException(errorResponse, httpResponse);
	}
}
