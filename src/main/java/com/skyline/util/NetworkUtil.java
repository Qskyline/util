package com.skyline.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import net.sf.json.JSONObject;

public class NetworkUtil 
{
	public static String sendPost(String url, String params) throws ClientProtocolException, IOException {
		String result = null;
		
		String[] param = params.split("&");
		List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();		
		for (String string : param) {
			String[] pair = string.split("=");
			if(pair.length != 2) continue;
			formparams.add(new BasicNameValuePair(pair[0], pair[1]));
		}
		UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");  
		
		HttpPost httppost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(50000).setConnectionRequestTimeout(10000)
                .setSocketTimeout(50000).build();
		httppost.setConfig(requestConfig);
		httppost.setEntity(uefEntity);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(httppost); 
		
        HttpEntity entity = response.getEntity();
        if (entity != null) {  
        	result = EntityUtils.toString(entity, "UTF-8");  
        }
        
		if(response != null) response.close();
    	if(httpClient != null) httpClient.close();
    	
		return result;
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
		conn.setDoOutput(true);
        conn.setDoInput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        out.print(params);
        out.flush();
        
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
			conn = null;
			return false;
		}
		
		if (contentType.toLowerCase().contains("application/json")) {
			String result = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            in.close();
            if(result != null && !result.equals("")) {
            	json.put("data", result);
            }
            return false;
		} else if (contentType.toLowerCase().contains("application/octet-stream")) {
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filePath),false)));
			DataInputStream dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
			byte[] _buff = new byte[2048];
			int len = 0;
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
		if(responseModel != null) res = JSONObject.fromObject(responseModel).toString();		
		PrintWriter writer = response.getWriter();
		writer.write(res);
		writer.flush();
		writer.close();
	}
	
	public static class TcpFileOperation {
		private int buffer = 1024 * 1024;
		private int fileName_sector = 256;
//		private int fileSize_sector = 6;
		private int params_sector = 1024;
		private int errorMsg_sector = 256;
//		private int errorCode_sector = 6;
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
				
				new Thread() {
					@Override
					public void run() {
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
					}
				}.start();
				return 0;
			}
			
			public void run() {
				BufferedInputStream bis = null;
				BufferedOutputStream bos = null;
				BufferedOutputStream f_bos = null;
				byte[] _buffer = new byte[buffer];			
				int len = 0;				
				File file = null;
				int exeption_help = 0; 
				
				try {
					do {
						bis = new BufferedInputStream(socket.getInputStream());
						bos = new BufferedOutputStream(socket.getOutputStream());
						
						byte[] _params = new byte[params_sector];
						len = bis.read(_params, 0, params_sector);
						if (len == -1) {
							communicateHelp("Fetch params error.", -6, bos);
							break;
						}
						String params = StringUtil.bytesToString(_params, len);
						if(!auth.isPermitted(params)) {
							communicateHelp("Auth Failed.", -7, bos);
							break;
						}
						
						byte[] _fileName = new byte[fileName_sector];
						len = bis.read(_fileName, 0, fileName_sector);
						if (len == -1) {
							communicateHelp("Fetch fileName error.", -1, bos);
							break;
						}
						String fileName = StringUtil.bytesToString(_fileName, len);

						byte[] _fileSize = new byte[8];
						len = bis.read(_fileSize, 0, 8);
						if (len == -1) {
							communicateHelp("Fetch fileSize error.", -2, bos);
							break;
						}
						long fileSize = bytesToLong(_fileSize);
						if (fileSize <= 0) {
							communicateHelp("Fetch fileSize error.", -2, bos);
							break;
						}
						if (fileSize > OSUtil.getDiskFreeSpace(path)) {
							communicateHelp("No more free disk space.", -3, bos);
							break;
						}

						String fullPath = path + "/" + fileName;
						file = new File(fullPath);
						if (file.exists()) {
							communicateHelp("The file is already exist.", -4, bos);
							break;
						}
						
						communicateHelp("PreCheck Success.", 0, bos);
						exeption_help = 1;

						f_bos = new BufferedOutputStream(new FileOutputStream(file));
						while ((len = bis.read(_buffer)) != -1) {
							f_bos.write(_buffer, 0, len);
						}
						f_bos.flush();
						communicateHelp("FileSave Success.", 1, bos);
					} while (false);
				} catch (Exception e) {
					if(exeption_help == 1) {
						if(f_bos != null) {
							try {f_bos.close();} catch (IOException e1) {e1.printStackTrace();}
						}
						if(file != null && file.exists()) {
							file.delete();
						}
					}
					e.printStackTrace();
					communicateHelp("Socket Exception.", -5, bos);
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
			
			private void communicateHelp(String errorMsg, int errorCode, BufferedOutputStream bos) {
				try {
					byte[] _errorMsg = StringUtil.stringToBytes(errorMsg, errorMsg_sector);
					byte[] _errorCode = new byte[] {(byte)errorCode};
					bos.write(_errorMsg, 0, errorMsg_sector);
					bos.write(_errorCode, 0, 1);
					bos.flush();
					if (errorCode < 0) {
						this.callback.faied(errorCode);
					} else if (errorCode == 1) {
						this.callback.success();
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
				int len = 0;
				int result = -11111111;
	
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
				int len = 0;
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
			public void success() {
			}
			@Override
			public void faied(int errorCode) {
			}
		}
		
		public static interface CallBack {
			public void success();
			public void faied(int errorCode);
		}
		
		public static class Auth {
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
	
    public static void main( String[] args )
    {
        tcpFileServerListen(11039, "D:/kingdeecloudbackup", true, null, new TcpFileOperation.Auth(){
        	public boolean isPermitted(String params) {
				return false;
			}
        });
    }
}
