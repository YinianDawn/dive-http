package dive.http.client;

import dive.http.common.BaseMimeHttp;
import dive.http.common.Detail;
import dive.http.common.MimeHttp;
import dive.http.common.MimeRequest;
import dive.http.common.model.Header;
import dive.http.common.model.Method;
import dive.http.common.model.Parameter;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 利用apache的httpclient包实现
 * @author dawn
 */
public class ClientHttp extends BaseMimeHttp {

	/**
	 * 底层请求对象
	 */
	private static HttpClient client;

	private static long startTime = System.currentTimeMillis();

	/**
	 * 连接池
	 */
	private static PoolingHttpClientConnectionManager cm =
			new PoolingHttpClientConnectionManager();

	/**
	 * 连接保持策略
	 */
	private static ConnectionKeepAliveStrategy keepAliveStart =
			new DefaultConnectionKeepAliveStrategy() {
		@Override
		public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
			long keepAlive = super.getKeepAliveDuration(response, context);
			if (-1 == keepAlive) {
				keepAlive = 5000;
			}
			return keepAlive;
		}
	};

	/**
	 * 请求配置，超时时间10s
	 */
	private static RequestConfig requestConfig = RequestConfig.custom()
			.setSocketTimeout(10000)
			.setConnectTimeout(10000)
			.setConnectionRequestTimeout(10000)
			.build();

	/*
	 * 实例化对象
	 */
	static {
		client = HttpClients.custom().setConnectionManager(cm)
				.setKeepAliveStrategy(keepAliveStart).build();
	}

	/**
	 * 超时连接监控
	 */
	private static void idleConnectionMonitor(){
		if(startTime + 10000 < System.currentTimeMillis()){
			startTime = System.currentTimeMillis();
			cm.closeExpiredConnections();
			cm.closeIdleConnections(10, TimeUnit.SECONDS);
		}
	}

	@Override
	public String execute(MimeRequest request, Detail detail) {
		Objects.requireNonNull(request, "mime request is null");

		idleConnectionMonitor();

		String url = MimeHttp.url(request);
		Method method = request.getMethod();
		Header requestHeader = request.getHeader();
		Parameter parameter = request.getParameter();
		String string = request.getString();
		Object para = null != parameter ? parameter : string;

		HttpEntity entity = null;
		Integer code = null;
		Header responseHeader = Header.build();
		String result = null;
		try {
			HttpRequestBase http = null;
			switch (method) {
				case GET: http = new HttpGet(url); break;
				case POST: http = new HttpPost(url);break;
				case PUT: http = new HttpPut(url);break;
				case DELETE: http = new HttpDelete(url);break;
			}

			if (null != request.getHeader()) {
				request.getHeader().each(http::setHeader);
			}

			if (!Method.GET.equals(method)) {
				HttpEntityEnclosingRequestBase base = (HttpEntityEnclosingRequestBase) http;

				if (null != parameter) {
					base.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
					List<NameValuePair> pairs = new LinkedList<>() ;
					parameter.forEach((k, v) -> pairs.add(new BasicNameValuePair(k, v.toString())));
					UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(pairs, Consts.UTF_8);
					base.setEntity(urlEncodedFormEntity);
				} else if (null != string) {
					if (!string.trim().startsWith("{") && !string.trim().startsWith("[")) {
						base.setHeader("Content-Type","text/plain; charset=utf-8");
					} else {
						base.setHeader("Content-Type", "application/json; charset=utf-8");
					}
					StringEntity stringEntity = new StringEntity(string, "UTF-8");
					base.setEntity(stringEntity);
				}
			}

			http.setConfig(requestConfig);

			HttpResponse response = client.execute(http);
			entity = response.getEntity();
			if(null == entity) {
				return null;
			}
			result = EntityUtils.toString(entity, "UTF-8");

			if (null != detail) {
				code = response.getStatusLine().getStatusCode();
				org.apache.http.Header[] headers = response.getAllHeaders();
				for (org.apache.http.Header h : headers) {
					responseHeader.add(h.getName(), h.getValue());
				}
				detail.accept(url,
						method,
						requestHeader,
						para,
						code,
						responseHeader,
						result);
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			error(e, "HttpClient", url);
			result = MimeHttp.except("Connection timed out: connect", detail,
					url,
					method,
					requestHeader,
					para,
					code,
					responseHeader,
					result);
		} catch (HttpHostConnectException e) {
			e.printStackTrace();
			error(e, "HttpClient", url);
			result = MimeHttp.except(e.getCause().getMessage(), detail,
					url,
					method,
					requestHeader,
					para,
					code,
					responseHeader,
					result);
		} catch (IOException e) {
			e.printStackTrace();
			error(e, "HttpClient", url);
		} finally {
			if (null != entity) {
				try {
					EntityUtils.consume(entity);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (null == result) {
			error("HttpClient result is null. url --> " + url);
		}
		return result;
	}

}

