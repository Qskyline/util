package com.skyline.util;

import ch.ethz.ssh2.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class OSUtil {

	static Logger logger = LoggerFactory.getLogger(OSUtil.class);

	public static List<String> getLanIp() throws SocketException, UnknownHostException {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> candidateAddresses = new ArrayList<String>();

		// 遍历所有的网络接口
		for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
				.hasMoreElements();) {
			NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
			// 在所有的接口下再遍历IP
			for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
				InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
				if (inetAddr != null && !inetAddr.isLoopbackAddress() && inetAddr instanceof Inet4Address) {
					if (inetAddr.isSiteLocalAddress()) {
						result.add(inetAddr.getHostAddress());
					} else {
						candidateAddresses.add(inetAddr.getHostAddress());
					}
				}
			}
		}

		if (result.size() != 0) {
			return result;
		} else if (candidateAddresses.size() != 0) {
			return candidateAddresses;
		} else {
			result.add(InetAddress.getLocalHost().getHostAddress());
			return result;
		}
	}

	public static long getDiskFreeSpace(String path) {
		File diskPartition = new File(path);
		if (!diskPartition.isDirectory())
			return -1;
		return diskPartition.getFreeSpace();
	}

	public static class ShellResult {
		private String std_out;
		private String std_err;
		private String std_in;
		private int exist_code;

		public String getStd_out() {
			return std_out;
		}
		public void setStd_out(String std_out) {
			this.std_out = std_out;
		}

		public String getStd_err() {
			return std_err;
		}
		public void setStd_err(String std_err) {
			this.std_err = std_err;
		}

		public String getStd_in() {
			return std_in;
		}
		public void setStd_in(String std_in) {
			this.std_in = std_in;
		}

		public int getExist_code() {
			return exist_code;
		}
		public void setExist_code(int exist_code) {
			this.exist_code = exist_code;
		}
	}

	public static ShellResult execShell(String ip, int port, String user, String password, String shell) throws IOException {
		Connection conn = new Connection(ip, port);
		conn.connect();
		if (conn.authenticateWithPassword(user, password)) {
			ShellResult shellResult = execShell(conn, shell);
			conn.close();
			return shellResult;
		} else {
			conn.close();
			throw new IOException("Authentication failed");
		}
	}

	public static ShellResult execShell(Connection conn, String shell) throws IOException {
		String DEFAULTCHART = "UTF-8";
		ShellResult shellResult;
		Session session = conn.openSession();
		session.execCommand(shell);
		shellResult = new ShellResult();
		shellResult.setStd_out(processStdout(session.getStdout(), DEFAULTCHART));
		shellResult.setStd_err(processStdout(session.getStderr(), DEFAULTCHART));
		shellResult.setStd_in(shell);
		shellResult.setExist_code(session.getExitStatus());
		session.close();
		return shellResult;
	}

	private static String processStdout(InputStream in, String charset) {
		InputStream stdout = new StreamGobbler(in);
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout, charset));
			String line;
			while ((line = br.readLine()) != null) {
				buffer.append(line + "\n");
			}
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	public static class ScpFile {
		private String ip;
		private int port;
		private String filePath;
		private String scpUser;
		private String scpPassword;
		private String type;
		private boolean isCompress;
		private String compressPath;
		private Connection conn;

		public ScpFile(String filePath, String ip, int port, String scpUser, String scpPassword) {
			this.filePath = filePath;
			this.ip = ip;
			this.port = port;
			this.scpUser = scpUser;
			this.scpPassword = scpPassword;
			this.isCompress = false;
			this.compressPath = null;
			this.conn = null;

			if (!StringUtils.isBlank(filePath)) {
				if (StringUtils.isBlank(ip)) {
					this.type = "local";
				} else if (StringUtils.isBlank(scpUser) || StringUtils.isBlank(scpPassword) || port <= 0) {
					this.type = null;
				} else {
					this.type = null;
					try {
						Connection conn = new Connection(ip, port);
						conn.connect();
						if (conn.authenticateWithPassword(scpUser, scpPassword)) {
							this.conn = conn;
							this.type = "remote";
						} else {
							logger.error("Authentication failed");
						}
					} catch (IOException e) {
						logger.error(StringUtil.getExceptionStackTraceMessage(e));
					}
				}
			} else {
				this.type = null;
			}
		}

		public ScpFile(String filePath) {
			this(filePath, null, -1, null, null);
		}

		public String getIp() {
			return ip;
		}
		public String getFilePath() {
			return filePath;
		}
		public String getScpUser() {
			return scpUser;
		}
		public String getScpPassword() {
			return scpPassword;
		}
		public String getType() {
			return type;
		}
		public int getPort() {
			return port;
		}
		public String getCompressPath() {
			return compressPath;
		}
		public void setCompressPath(String compressPath) {
			this.compressPath = compressPath;
		}
		public boolean isCompress() {
			return isCompress;
		}
		public void setCompress(boolean compress) {
			isCompress = compress;
		}
		public Connection getConn() {
			return conn;
		}
		public void closeConn() {
			if (this.conn != null) {
				this.conn.close();
			}
		}
	}

	public static ShellResult scp_shell(ScpFile srcFile, ScpFile dstFile) {
		String DEFAULTCHART = "UTF-8";
		Runtime runtime = Runtime.getRuntime();
		ShellResult shellResult = new ShellResult();
		try {
				Process process = runtime.exec("expect -v");
				String check = processStdout(process.getErrorStream(), DEFAULTCHART);
				if (!StringUtils.isBlank(check)) {
					System.out.println(check);
					System.out.println("Attempt install expect ...");
					runtime.exec("yum -y install expect");
					process = runtime.exec("expect -v");
					check = processStdout(process.getErrorStream(), DEFAULTCHART);
					if (!StringUtils.isBlank(check)) {
						System.out.println(check);
						System.out.println("ERROR: Can not find or install the command 'expect'.");
						return null;
					}
				}

				if (srcFile.getType() == null || dstFile.getType() == null) {
					return null;
				}

				String workDir = "/tmp/scp-" + TimeUtil.getDateNow().getTime();
				boolean is_src_local = srcFile.getType().equals("local");
				boolean is_dst_local = dstFile.getType().equals("local");
				String command;
				String expext =
						"expect {\n" +
						"  timeout {\n" +
						"    exit\n" +
						"  }\n" +
						"  eof {\n" +
						"    exit\n" +
						"  }\n" +
						"  -re \"Are you sure you want to continue connecting (yes/no)?\" {\n" +
						"    send \"yes\\r\"\n" +
						"    exp_continue\n" +
						"  }\n" +
						"  -re \"password:\" {\n" +
						"    send \"$password\\r\"\n" +
						"  }\n" +
						"}\n" +
						"expect eof\n" +
						"__EOF\n";
				if (is_src_local && is_dst_local) {
					command = "cp -fr " + srcFile.getFilePath() + " " + dstFile.getFilePath();
				} else if (is_src_local && !is_dst_local) {
					command = "#!/usr/bin/env bash\n" +
							"password='" + dstFile.getScpPassword() + "'\n" +
							"expect << __EOF\n" +
							"set timeout -1\n" +
							"spawn bash -c \"scp -P " + dstFile.getPort() + " " + srcFile.getFilePath() + " " + dstFile.getScpUser() + "@" + dstFile.getIp() + ":" + dstFile.getFilePath() +"\"\n" +
							expext;
				} else if (!is_src_local && is_dst_local) {
					command = "#!/usr/bin/env bash\n" +
							"password='" + srcFile.getScpPassword() + "'\n" +
							"expect << __EOF\n" +
							"set timeout -1\n" +
							"spawn bash -c \"scp -P " + srcFile.getPort() + " " + srcFile.getScpUser() + "@" + srcFile.getIp() + ":" + srcFile.getFilePath() + " " + dstFile.getFilePath() + "\"\n" +
							expext;
				} else {
					String transfer_dir = workDir + "/transfer";
					process = runtime.exec("rm -fr " + transfer_dir);
					check = processStdout(process.getErrorStream(), DEFAULTCHART);
					if (!StringUtils.isBlank(check)) {
						System.out.println(check);
						return null;
					}

					process = runtime.exec("mkdir -p " + transfer_dir);
					check = processStdout(process.getErrorStream(), DEFAULTCHART);
					if (!StringUtils.isBlank(check)) {
						System.out.println(check);
						return null;
					}

					command = "#!/usr/bin/env bash\n" +
							"password='" + srcFile.getScpPassword() + "'\n" +
							"expect << __EOF\n" +
							"set timeout -1\n" +
							"spawn bash -c \"scp -P " + srcFile.getPort() + " " + srcFile.getScpUser() + "@" + srcFile.getIp() + ":" + srcFile.getFilePath() + " " + transfer_dir + "/\"\n" +
							expext +
							"password='" + dstFile.getScpPassword() + "'\n" +
							"expect << __EOF\n" +
							"set timeout -1\n" +
							"spawn bash -c \"scp -P " + dstFile.getPort() + " " + transfer_dir + "/* " + dstFile.getScpUser() + "@" + dstFile.getIp() + ":" + dstFile.getFilePath() +"\"\n" +
							expext;
				}

				File shell_dir = new File(workDir);
				if (!shell_dir.exists()) {
					shell_dir.mkdirs();
				}
				if (shell_dir.isDirectory()) {
					FileOutputStream fos = new FileOutputStream(workDir + "/scp.sh", false);
					fos.write(command.getBytes());
					fos.close();
				} else {
					return null;
				}

				process = runtime.exec("sh " + workDir + "/scp.sh");
				shellResult.setStd_out(processStdout(process.getInputStream(), DEFAULTCHART));
				shellResult.setStd_err(processStdout(process.getErrorStream(), DEFAULTCHART));
				shellResult.setStd_in("sh " + workDir + "/scp.sh");
				FileUtils.deleteDirectory(shell_dir);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return shellResult;
	}

	public static void scp_java(ScpFile srcFile, ScpFile dstFile) throws Exception {
		if (srcFile.type == null || dstFile.getType() == null) {
			throw new Exception("Can not discriminate the file Type.");
        }

        boolean is_src_local = srcFile.getType().equals("local");
		File src_file = new File(srcFile.getFilePath());
		boolean src_is_dir;
		if (is_src_local) {
			if (!src_file.exists()) {
				throw new IOException("Can not find the srcFile");
			}
			src_is_dir = src_file.isDirectory();
			if (src_is_dir || srcFile.isCompress()) {
				String tarGzPath = System.getProperty("java.io.tmpdir") + src_file.getName() + "-" + TimeUtil.getDateNow().getTime() + ".tar.gz";
				CompressUtil.tarGzCompress(src_file, tarGzPath);
				srcFile.setCompress(true);
				srcFile.setCompressPath(tarGzPath);
			}
        } else {
			ShellResult shellResult = execShell(srcFile.getConn(), "ls " + srcFile.getFilePath());
			if (shellResult.getExist_code() != 0) {
				throw new IOException("Can not find the srcFile");
			} else {
				src_is_dir = !srcFile.getFilePath().equals(shellResult.getStd_out());
				if (src_is_dir || srcFile.isCompress()) {
					String tarGzPath = "/tmp/" + src_file.getName() + "-" + TimeUtil.getDateNow().getTime() + ".tar.gz";
					String cmd = "cd " + src_file.getParentFile()  + " && tar -czf " + tarGzPath + " " + src_file.getName();
					ShellResult tmp = execShell(srcFile.getConn(), cmd);
					if (tmp.getExist_code() != 0) {
						throw new IOException("SrcFile compress failed.");
					}
					srcFile.setCompress(true);
					srcFile.setCompressPath(tarGzPath);
				}
			}
		}

		boolean is_dst_local = dstFile.getType().equals("local");
		File dst_file = new File(srcFile.getFilePath());
		if (is_dst_local) {
			if (!dst_file.exists()) dst_file.mkdirs();
			if (dst_file.isFile()) {
				throw new IOException("The dstFile must be directory.");
			}
		} else {
			ShellResult shellResult = execShell(dstFile.getConn(), "ls " + dstFile.getFilePath());
			if (shellResult.getExist_code() == 0) {
				if (dstFile.getFilePath().equals(shellResult.getStd_out())) {
					throw new IOException("The dstFile must be a directory.");
				}
			}
			else if (shellResult.getExist_code() == 2) {
				ShellResult tmp = execShell(dstFile.getConn(), "mkdir -p " + dstFile.getFilePath());
				if (tmp.getExist_code() != 0) {
					throw new IOException("Can not create dstFile directory.");
				}
			} else {
				throw new IOException("Can not fetch the dstFile status.");
			}
		}

		if (is_src_local && is_dst_local) {
			if(srcFile.isCompress()) {
				CompressUtil.unTarGz(srcFile.getCompressPath(), dstFile.getFilePath(), true);
			} else {
				Files.copy(src_file.toPath(), dst_file.toPath());
			}
		} else if (is_src_local && !is_dst_local) {
			if (srcFile.isCompress()) {
				File local_tar_file = new File(srcFile.getCompressPath());
				putFile(new SCPClient(dstFile.getConn()), local_tar_file, "/tmp/");
				ShellResult shellResult = execShell(dstFile.getConn(), "cd " + dstFile.getFilePath() + " && tar -xzf " + "/tmp/" + local_tar_file.getName());
				if (shellResult.getExist_code() != 0) {
					throw new IOException("Can not extract temp file to dstDir.");
				}
				ShellResult tmp = execShell(dstFile.getConn(),"rm -fr /tmp/" + local_tar_file.getName());
				if (tmp.getExist_code() != 0) {
					logger.error("Clear temp file failed.");
				}
				local_tar_file.deleteOnExit();
			} else {
				putFile(new SCPClient(dstFile.getConn()), src_file, dstFile.getFilePath());
			}
		} else if (!is_src_local && is_dst_local) {
			if (srcFile.isCompress()) {
				String tempFileName = new File(srcFile.getCompressPath()).getName();
				getFile(new SCPClient(srcFile.getConn()), srcFile.getCompressPath(), System.getProperty("java.io.tmpdir"));
				CompressUtil.unTarGz(System.getProperty("java.io.tmpdir") + tempFileName, dstFile.getFilePath(), true);
				ShellResult tmp = execShell(srcFile.getConn(),"rm -fr /tmp/" + tempFileName);
				if (tmp.getExist_code() != 0) {
					logger.error("Clear temp file failed.");
				}
			} else {
				getFile(new SCPClient(srcFile.getConn()), srcFile.getFilePath(), dstFile.getFilePath());
			}
		} else {
			if (srcFile.isCompress()) {
				String tempFileName = new File(srcFile.getCompressPath()).getName();
				getFile(new SCPClient(srcFile.getConn()), srcFile.getCompressPath(), System.getProperty("java.io.tmpdir"));
				File tmp_tar_file = new File(System.getProperty("java.io.tmpdir") + tempFileName);
				putFile(new SCPClient(dstFile.getConn()), tmp_tar_file,"/tmp/");
				ShellResult shellResult = execShell(dstFile.getConn(), "cd " + dstFile.getFilePath() + " && tar -xzf " + "/tmp/" + tempFileName);
				if (shellResult.getExist_code() != 0) {
					throw new IOException("Can not extract temp file to dstDir.");
				}
				ShellResult tmp = execShell(srcFile.getConn(),"rm -fr /tmp/" + tempFileName);
				if (tmp.getExist_code() != 0) {
					logger.error("Clear temp file failed.");
				}
				tmp = execShell(dstFile.getConn(),"rm -fr /tmp/" + tempFileName);
				if (tmp.getExist_code() != 0) {
					logger.error("Clear temp file failed.");
				}
				tmp_tar_file.deleteOnExit();
			} else {
				String tmp_file_name = new File(srcFile.getFilePath()).getName();
				getFile(new SCPClient(srcFile.getConn()), srcFile.getFilePath(), System.getProperty("java.io.tmpdir"));
				File tmp_file = new File(System.getProperty("java.io.tmpdir") + tmp_file_name);
				putFile(new SCPClient(dstFile.getConn()), tmp_file, dstFile.getFilePath());
				tmp_file.deleteOnExit();
			}
		}
	}

	private static void putFile(SCPClient client, File localFile, String dstDirectory) throws IOException {
		SCPOutputStream scpOutputStream = client.put(localFile.getName(), localFile.length(), dstDirectory, null);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(localFile));
		int len;
		byte[] bytes = new byte[10240];
		while ((len = bis.read(bytes)) != -1) {
			scpOutputStream.write(bytes, 0, len);
		}
		scpOutputStream.flush();
		scpOutputStream.close();
		bis.close();
	}

	private static void getFile(SCPClient client, String remoteFile, String dstDirectory) throws IOException {
        char t = System.getProperty("line.separator").charAt(0);
        dstDirectory = StringUtil.trim(dstDirectory, t) + t;
		String fileName = new File(remoteFile).getName();
		SCPInputStream scpInputStream = client.get(remoteFile);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(dstDirectory + fileName)));
		int len;
		byte[] bytes = new byte[10240];
		while ((len = scpInputStream.read(bytes)) != -1) {
			bos.write(bytes, 0, len);
		}
		bos.flush();
		bos.close();
		scpInputStream.close();
	}

	public static boolean replaceStringInSmallFile(File file , String match_str, String dst_str) {
		if (file == null) {
			logger.error("The param file can not be null.");
			return false;
		}

		if (!file.isFile()) {
			logger.error("The param file can not be a directory.");
			return false;
		}

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			CharArrayWriter caw = new CharArrayWriter();
			String line;
			while((line = br.readLine()) != null){
				caw.write(line);
				caw.append(System.getProperty("line.separator"));
			}
			br.close();

			FileWriter fw=new FileWriter(file);
			fw.write(caw.toString().replaceAll(match_str, dst_str));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			logger.error(StringUtil.getExceptionStackTraceMessage(e));
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
	}

}
