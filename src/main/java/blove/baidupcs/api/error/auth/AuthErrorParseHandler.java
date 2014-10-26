package blove.baidupcs.api.error.auth;

import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class AuthErrorParseHandler implements ErrorHandler {
	private static final Gson GSON_INSTANCE = new Gson();

	@Override
	public Throwable handleError(RetrofitError cause) {
		Throwable oriException = cause.getCause();
		Response httpResponse = cause.getResponse();
		if (oriException instanceof Error)// severe Error
			return oriException;
		else if (oriException instanceof IOException)// IOException
			return oriException;
		else {
			BaiduPcsAuthException authException = parseException(httpResponse);
			if (authException != null)
				return authException;
			else
				return oriException;
		}
	}

	private BaiduPcsAuthException parseException(Response httpResponse) {
		if (httpResponse.getBody() != null) {
			try {
				return new BaiduPcsAuthException(GSON_INSTANCE.fromJson(
						new InputStreamReader(httpResponse.getBody().in()),
						AuthErrorResponse.class));
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				// response body的json不符合ErrorResponse，或读取时出现IO异常
				return null;
			}
		} else {
			return null;
		}
	}
}
