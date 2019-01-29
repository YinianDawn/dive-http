package dive.http.mime;

import dive.http.common.BaseMimeHttp;
import dive.http.common.Detail;
import dive.http.common.MimeRequest;
import dive.http.common.model.Header;
import dive.http.common.model.Method;
import dive.http.common.model.Parameter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * java原生http请求
 * @author dawn
 */
public class MimeHttp extends BaseMimeHttp {

	@Override
	public String execute(MimeRequest request, Detail detail) {
		Objects.requireNonNull(request, "mime request is null");

		String url = dive.http.common.MimeHttp.url(request);
		Method method = request.getMethod();
		Header requestHeader = request.getHeader();
		Parameter parameter = request.getParameter();
		String string = request.getString();
		Object para = null != parameter ? parameter : string;

		HttpURLConnection connection = null;
		PrintWriter out = null;
		InputStream in = null;
		Integer code = null;
		Header responseHeader = Header.build();
		String result = null;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod(method.name());

			// "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
			if (null != requestHeader) {
				requestHeader.each(connection::setRequestProperty);
			}

			connection.connect();
			if (!method.equals(Method.GET)) {
				out = new PrintWriter(connection.getOutputStream());
				if (null != parameter) {
					out.print(parameter.concat());
				} else if (null != string) {
					out.print(string);
				}
				out.flush();
			}

			in = connection.getInputStream();

			BufferedReader buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = buffer.readLine()) != null) {
				sb.append(line).append("\n");
			}
			result = sb.toString();
			if (null != detail) {
				code = connection.getResponseCode();
				Map<String,List<String>> headerMap = connection.getHeaderFields();
				headerMap.forEach((k, vs) -> vs.forEach(v -> responseHeader.add(k, v)));
				detail.accept(url,
						method,
						requestHeader,
						para,
						code,
						responseHeader,
						result);
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (null != connection) {
				try {
					code = connection.getResponseCode();
					if (403 == code) {
						error("maybe you should add 'User-Agent' for headers, for example: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			error(e, "MimeClient", url);
			if (e.getMessage().contains("Connection timed out")) {
				result = dive.http.common.MimeHttp.except("Connection timed out: connect", detail,
						url,
						method,
						requestHeader,
						para,
						code,
						responseHeader,
						result);
			}
		} finally {
			try {
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.close();
				}
			} catch (Exception e) {
				error("MimeClient close is error. url --> " + url);
			}
		}
		if (null == result) {
			error("MimeClient result is null. url --> " + url);
		}
		return result;
	}

}

