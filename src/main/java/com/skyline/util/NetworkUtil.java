package com.skyline.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

public class NetworkUtil {
	private static Logger logger = LoggerFactory.getLogger(NetworkUtil.class);
	private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;
	private static final int DEFAULT_REQUEST_TIMEOUT = 3000;
	private static final int DEFAULT_SOCKET_TIMEOUT = 5000;

	private static List<Header> mergeHeaders(List<Header> headers, List<Header> defaultHeaders) {
		if (headers == null || headers.size() == 0) {
			return defaultHeaders == null ? new ArrayList<>() : defaultHeaders;
		}

		if (defaultHeaders == null || defaultHeaders.size() == 0) {
			return headers;
		}

		Map<String, Header> data = new HashMap<>();
		for (Header header : defaultHeaders) {
			data.put(header.getName().toLowerCase(), header);
		}
		for (Header header : headers) {
			data.put(header.getName().toLowerCase(), header);
		}

		List<Header> result = new ArrayList<>();
		for (String key : data.keySet()) {
			result.add(data.get(key));
		}

		return result;
	}

	public static String sendPost(String url) throws IOException {
		return sendPost(url, null, null, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
	}

	public static String sendPost(String url, Map<String,String> params) throws IOException {
		return sendPost(url, params, null, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
	}

	public static String sendPost(String url, Map<String,String> params, List<Header> headers) throws IOException {
		return sendPost(url, params, headers, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
	}

	public static String sendPost(
			String url,
			Map<String,String> params,
			List<Header> headers,
			int connectionTimeout,
			int requestTimeout,
			int socketTimeout
	) throws IOException {
		//http conf
		HttpPost httppost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(connectionTimeout).setConnectionRequestTimeout(requestTimeout)
				.setSocketTimeout(socketTimeout).build();
		httppost.setConfig(requestConfig);

		// determine headers
		List<Header> defaultHeaders = new ArrayList<>();
		defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json;charset=UTF-8"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "gzip"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8"));
		defaultHeaders.add(new BasicHeader(HttpHeaders.CONTENT_ENCODING, "gzip"));
		headers = mergeHeaders(headers, defaultHeaders);
		httppost.setHeaders(headers.toArray(new Header[]{}));

		// process params
		if (params != null && params.size() > 0) {
			boolean isForm = false;
			for (Header header : headers) {
				if (header.getName().equals(HttpHeaders.CONTENT_TYPE)
						&& StringUtils.isNotEmpty(header.getValue())
						&& header.getValue().toLowerCase().contains("x-www-form-urlencoded")) {
					isForm = true;
					break;
				}
			}

			String paramStr;
			if (isForm) {
				List<String> tmp = new ArrayList<>();
				for (String key : params.keySet()) {
					tmp.add(key + "=" + params.get(key));
				}
				paramStr = StringUtils.join(tmp, "&");
			} else {
				paramStr = JSON.toJSONString(params);
			}
			httppost.setEntity(new StringEntity(paramStr, Charset.forName("UTF-8")));
		}

		//execute
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(httppost);

		//process response
        HttpEntity entity = response.getEntity();
        String result = null;
        if (entity != null) {
        	result = EntityUtils.toString(entity, Charset.forName("UTF-8"));
        }
		if(response != null) response.close();
    	if(httpClient != null) httpClient.close();
    	
		return result;
	}

	public String get(String url, Map<String,String> header) throws Exception {
		return sendGet(url, header, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_REQUEST_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
	}

	public static String sendGet(String url, Map<String,String> headers, int connectionTimeout, int requestTimeout, int socketTimeout) throws Exception {
		BufferedReader in = null;
		try {
			List<Header> headers_list = null;
			if ((headers != null) && (headers.size() != 0)) {
				headers_list = new ArrayList<>();
				Iterator<String> iterator = headers.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					headers_list.add(new BasicHeader(key, headers.get(key)));
				}
			}
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(connectionTimeout).setConnectionRequestTimeout(requestTimeout)
					.setSocketTimeout(socketTimeout).build();

			HttpGet request = new HttpGet();
			request.setURI(new URI(url));
			if (headers_list != null) request.setHeaders(headers_list.toArray(new Header[] {}));
			request.setConfig(requestConfig);

			HttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(request);

			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer();
			String line;
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			return sb.toString();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					logger.error("Http get error", e);
				}
			}
		}
	}
	
	public static int fetchPostRequestStatus(String _url, String params) throws IOException {
		URL url = new URL(_url);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent", "Chrome/61.0.3163.100");
		conn.setDoOutput(true);  
        conn.setDoInput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        if(params != null) out.print(params);
        out.flush();
		return conn.getResponseCode();
	}
	
	public static int fetchGetRequestStatus(String _url) throws IOException {
		URL url = new URL(_url);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent", "Chrome/61.0.3163.100");
		conn.connect();
		return conn.getResponseCode();
	}
	
	public static boolean download(String _url, String params, String filePath, JSONObject json) throws IOException {
		URL url = new URL(_url);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		if (params != null) {
            conn.setDoOutput(true);
            conn.setDoInput(true);
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			out.print(params);
			out.flush();
		}

        Map<String, List<String>> _headers = conn.getHeaderFields();
        JSONObject headers = new JSONObject();
        if(_headers != null) {
        	for (String key: _headers.keySet()) {
        		if(key != null) {
        			headers.put(key, _headers.get(key));
        		}
			}
        }
        
        json.put("headers", headers);
		String contentType = conn.getContentType();
		if (StringUtils.isBlank(contentType)) {
			return false;
		}
		
		if (contentType.toLowerCase().contains("application/json")) {
			String result = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            in.close();
            if(result != null && !result.equals("")) {
            	json.put("data", result);
            }
            return false;
		} else if (contentType.toLowerCase().contains("application/octet-stream") || contentType.toLowerCase().contains("application/zip")) {
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filePath),false)));
			DataInputStream dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
			byte[] _buff = new byte[2048];
			int len;
			while((len = dis.read(_buff)) >= 0){
				dos.write(_buff, 0, len);
			}
			dis.close();
			dos.close();
			return true;
		}
		return false;
	}
	
	public static void writeToResponse(HttpServletResponse response, Object responseModel) throws IOException {
		if (response == null) return;
		String res = "";
		if(responseModel != null) res = JSON.toJSONString(responseModel);
		PrintWriter writer = response.getWriter();
		writer.write(res);
		writer.flush();
		writer.close();
	}
	
	public static class TcpFileOperation {
		private int buffer = 1024 * 1024;
		private int fileName_sector = 256;
		private int params_sector = 1024;
		private int errorMsg_sector = 256;
		private TcpFileServer tcpFileServer = new TcpFileServer();
		private TcpFileClient tcpFileClient = new TcpFileClient();
		
		private static TcpFileOperation tcpFileOperation = new TcpFileOperation();
		public static TcpFileOperation getInstance() {
			return tcpFileOperation;
		}
		
		public int TcpFileServerListen(int port, String path, boolean isKeep, CallBack callback, Auth auth) {
			return tcpFileServer.listen(port, path, isKeep, callback, auth);
		}
		public int TcpFileClientSend(String url, int desPort, int sourcePort, String filePath, String params) {
			return tcpFileClient.sendFile(url, desPort, sourcePort, filePath, params);
		}
		
		private class TcpFileServer extends Thread {
			private Socket socket;
			private String path;
			private CallBack callback;
			private Auth auth;
			
			public TcpFileServer(Socket socket, String path, CallBack callback, Auth auth) {
				this.socket = socket;
				this.path = path;
				this.callback = callback;
				this.auth = auth;
			}
			
			public TcpFileServer() {
				this(null, null, null, null);
			}

			public int listen(int port, String path, boolean isKeep, CallBack callback, Auth auth) {
				try {
					Socket socket = new Socket("127.0.0.1", port);
					socket.close();
					return -1;
				} catch (Exception e) {}
				
				if (path == null) return -2;
				File file = new File(path);
				if (!file.exists()) file.mkdirs();
				if (!file.isDirectory()) return -3;

				new Thread(() -> {
					ServerSocket ss = null;
					try {
						ss = new ServerSocket(port);
						while (true) {
							System.out.println("listen " + port + " ...");
							Socket s = ss.accept();
							CallBack _callback = callback;
							Auth _auth = auth;
							if(_callback == null) _callback = new DoNothingCallBack();
							if(_auth == null) _auth = new Auth();
							new TcpFileServer(s, path, _callback, _auth).start();
							if(!isKeep) {
								ss.close();
								break;
							}
						}
					} catch (Exception e) {
						try {
							if (ss != null) ss.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}).start();
				return 0;
			}
			
			public void run() {
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				BufferedOutputStream f_bos = null;
				byte[] _buffer = new byte[buffer];
				int len;
				File file = null;
				String params = null;
				int exception_help = 0;
				
				try {
					do {
						bis = new BufferedInputStream(socket.getInputStream());
						bos = new BufferedOutputStream(socket.getOutputStream());
						
						byte[] _params = new byte[params_sector];
						len = bis.read(_params, 0, params_sector);
						if (len == -1) {
							communicateHelp("Fetch params error.", -6, bos, null);
							break;
						}
						params = StringUtil.bytesToString(_params, len);
						if(!auth.isPermitted(params)) {
							communicateHelp("Auth Failed.", -7, bos, params);
							break;
						}
						
						byte[] _fileName = new byte[fileName_sector];
						len = bis.read(_fileName, 0, fileName_sector);
						if (len == -1) {
							communicateHelp("Fetch fileName error.", -1, bos, params);
							break;
						}
						String fileName = StringUtil.bytesToString(_fileName, len);

						byte[] _fileSize = new byte[8];
						len = bis.read(_fileSize, 0, 8);
						if (len == -1) {
							communicateHelp("Fetch fileSize error.", -2, bos, params);
							break;
						}
						long fileSize = bytesToLong(_fileSize);
						if (fileSize <= 0) {
							communicateHelp("Fetch fileSize error.", -2, bos, params);
							break;
						}
						if (fileSize > OSUtil.getDiskFreeSpace(path)) {
							communicateHelp("No more free disk space.", -3, bos, params);
							break;
						}

						String fullPath = path + auth.getSecondPath() + "/" + fileName;
						file = new File(fullPath);
						file.getParentFile().mkdirs();
						if (file.exists()) {
							communicateHelp("The file is already exist.", -4, bos, params);
							break;
						}
						
						communicateHelp("PreCheck Success.", 0, bos, params);
						exception_help = 1;

						f_bos = new BufferedOutputStream(new FileOutputStream(file));
						while ((len = bis.read(_buffer)) != -1) {
							f_bos.write(_buffer, 0, len);
						}
						f_bos.flush();
						communicateHelp("FileSave Success.", 1, bos, params);
					} while (false);
				} catch (Exception e) {
					if(exception_help == 1) {
						if(f_bos != null) {
							try {f_bos.close();} catch (IOException e1) {e1.printStackTrace();}
						}
						if(file != null && file.exists()) {
							file.delete();
						}
					}
					e.printStackTrace();
					communicateHelp("Socket Exception.", -5, bos, params);
				} finally {
					try {
						if (f_bos != null) f_bos.close();
						if (bis != null) bis.close();
						if (bos != null) bos.close();
						if (socket != null) socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
			
			private void communicateHelp(String errorMsg, int errorCode, BufferedOutputStream bos, String params) {
				try {
					byte[] _errorMsg = StringUtil.stringToBytes(errorMsg, errorMsg_sector);
					byte[] _errorCode = new byte[] {(byte)errorCode};
					bos.write(_errorMsg, 0, errorMsg_sector);
					bos.write(_errorCode, 0, 1);
					bos.flush();
					if (errorCode < 0) {
						this.callback.failed(errorCode, params);
					} else if (errorCode == 1) {
						this.callback.success(params);
					}
					System.out.println(errorMsg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private class TcpFileClient {
			public int sendFile(String url, int desPort, int sourcePort, String filePath, String params) {
				File file = new File(filePath);
				if (!file.exists())
					return -11;
				if (!file.isFile())
					return -12;
	
				Socket s = new Socket();
				int len;
				int result;
	
				BufferedOutputStream bos = null;
				BufferedInputStream bis = null;
				BufferedInputStream f_bis = null;
	
				try {
					do {
						s.connect(new InetSocketAddress(url, desPort), sourcePort);
						bos = new BufferedOutputStream(s.getOutputStream());
						bis = new BufferedInputStream(s.getInputStream());
	
						byte[] _params = StringUtil.stringToBytes(params, params_sector);
						String fileName = StringUtils.substringAfterLast(filePath, "/");
						byte[] _fileName = StringUtil.stringToBytes(fileName, fileName_sector);
						long fileSize = file.length();
						byte[] _fileSize = longToBytes(fileSize);
						
						bos.write(_params, 0, params_sector);
						bos.write(_fileName, 0, fileName_sector);
						bos.write(_fileSize, 0, 8);
						bos.flush();
	
						if((result = (int)chargeHelp(bis)) != 0) break;
	
						f_bis = new BufferedInputStream(new FileInputStream(filePath));
						byte[] _buffer = new byte[buffer];
						while ((len = f_bis.read(_buffer)) != -1) {
							bos.write(_buffer, 0, len);
						}
						bos.flush();
						s.shutdownOutput();
						result = (int)chargeHelp(bis);
					} while (false);
				} catch (Exception e) {
					e.printStackTrace();
					result = -15;
				} finally {
					try {
						if (f_bis != null) f_bis.close();
						if (bis != null) bis.close();
						if (bos != null) bos.close();
						if (s != null) s.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				return result;
			}
		
			private long chargeHelp(BufferedInputStream bis) throws IOException {
				int len;
				byte[] _errorMsg = new byte[errorMsg_sector];
				len = bis.read(_errorMsg, 0, errorMsg_sector);
				if (len == -1) {
					return -13;
				}
				String errorMsg = StringUtil.bytesToString(_errorMsg, len);
				System.out.println("errorMsg: " + errorMsg);
				
				byte _errorCode[] = new byte[1];
				len = bis.read(_errorCode, 0, 1);
				if (len == -1) {
					return -14;
				}
				int errorCode = _errorCode[0];
				System.out.println("errorCode: " + errorCode);
				return errorCode;
			}
		}

		private class DoNothingCallBack implements CallBack {
			@Override
			public void success(String params) {
			}
			@Override
			public void failed(int errorCode, String params) {
			}
		}
		
		public interface CallBack {
			void success(String params);
			void failed(int errorCode, String params);
		}
		
		public static class Auth {
			private String secondPath = "";

			public void setSecondPath(String secondPath) {
				this.secondPath = secondPath;
			}

			public String getSecondPath() {
				return this.secondPath;
			}

			public boolean isPermitted(String params) {
				return true;
			}
		}
		
		public long bytesToLong(byte[] bytes) {
			long result = 0;
			int len = bytes.length;
			for (int i=len-1; i>=0; i--) {
				result = result | (((long)(bytes[i] & 0xFF)) << (8*(len-1-i)));
			}
			return result;
		}
		
		public byte[] longToBytes(long data) {
			int len = 8;
			byte[] result = new byte[len];
			Arrays.fill(result, (byte)0);
			for(int i=0; i<len; i++) {
				result[i] = (byte)(data >> (len-i-1)*8);
			}
			return result;
		}
	}
	
	public static int tcpFileServerListen(int port, String path) {
		return tcpFileServerListen(port, path, false);
	}
	public static int tcpFileServerListen(int port, String path, boolean isKeep) {
		return tcpFileServerListen(port, path, isKeep, null);
	}
	public static int tcpFileServerListen(int port, String path, boolean isKeep, TcpFileOperation.CallBack callback) {
		return tcpFileServerListen(port, path, isKeep, callback, null);
	}
	public static int tcpFileServerListen(int port, String path, boolean isKeep, TcpFileOperation.CallBack callback, TcpFileOperation.Auth auth) {
		return TcpFileOperation.getInstance().TcpFileServerListen(port, path, isKeep, callback, auth);
	}
	
	/**
	 * 
	 * @param url
	 * @param desPort
	 * @param sourcePort
	 * @param filePath
	 * @param params
	 * @return 
	 * -11: File is not exist.
	 * -12: The path is not file.
	 * -13: Client can not fetch errorMsg.
	 * -14: Client can not fetch errorCode.
	 * -15: Client Socket Error, 
	 * -1: Server can not fetch the file name.
	 * -2: Server can not fetch the file size.
	 * -3: Server has no more free disk space.
	 * -4: The file has been exist on the server.
	 * -5: Server Socket Error.
	 * 0: preCheck success.
	 * 1: fileSave success.
	 */
	public static int tcpFileClientSend(String url, int desPort, int sourcePort, String filePath, String params) {
		return TcpFileOperation.getInstance().TcpFileClientSend(url, desPort, sourcePort, filePath, params);
	}
	public static int tcpFileClientSend(String url, int desPort, int sourcePort, String filePath) {
		return tcpFileClientSend(url, desPort, sourcePort, filePath, null);
	}
	
    public static void main( String[] args ) {
        /*tcpFileServerListen(11039, "/Users/skyline/Documents", false, null, new TcpFileOperation.Auth(){
        	public boolean isPermitted(String params) {
        		setSecondPath("/test");
				return true;
			}
        });*/

        JSONObject json = new JSONObject();
		try {
			download("http://172.18.5.42:88/biz-dev/apppackage/bd-bd.zip", null, "/Users/skyline/Downloads/bd-bd-1.zip", json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
