package com.skyline.util;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class OSUtil {
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

	public static ShellResult execShell(String ip, int port, String user, String password, String shell) {
		String DEFAULTCHART = "UTF-8";
		ShellResult shellResult = null;
		try {
			Connection conn = new Connection(ip, port);
			conn.connect();
			if (conn.authenticateWithPassword(user, password)) {
				Session session = conn.openSession();
				session.execCommand(shell);
				shellResult = new ShellResult();
				shellResult.setStd_out(processStdout(session.getStdout(), DEFAULTCHART));
				shellResult.setStd_err(processStdout(session.getStderr(), DEFAULTCHART));
				shellResult.setStd_in(shell);
				shellResult.setExist_code(session.getExitStatus());
				session.close();
			}
			conn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		public ScpFile(String filePath, String ip, int port, String scpUser, String scpPassword) {
			this.filePath = filePath;
			this.ip = ip;
			this.port = port;
			this.scpUser = scpUser;
			this.scpPassword = scpPassword;

			if (!StringUtils.isBlank(filePath)) {
				if (StringUtils.isBlank(ip)) {
					this.type = "local";
				} else if (StringUtils.isBlank(scpUser) || StringUtils.isBlank(scpPassword) || port <= 0) {
					this.type = null;
				} else {
					this.type = "remote";
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
	}

	public static ShellResult scp(ScpFile srcFile, ScpFile dstFile) {
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

	public static boolean replaceStringInFile(File file , String match_str, String dst_str) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			CharArrayWriter caw = new CharArrayWriter();
			String line;
			while((line = br.readLine()) != null){
				line = line.replaceAll(match_str, dst_str);
				caw.write(line);
				caw.append(System.getProperty("line.separator"));
			}
			br.close();

			FileWriter fw=new FileWriter(file);
			caw.writeTo(fw);
			fw.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		System.out.println(replaceStringInFile(new File("/Users/skyline/Downloads/ierp_dev0724.conf"), "server\\s+web-8080-331101.kcssz.cloud.kingdee.com", "server 127.0.0.1:1024"));
	}
}
