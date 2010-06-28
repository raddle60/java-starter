package com.raddle.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * java 启动程序
 */
public class Starter {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		try {
			Properties p = new Properties();
			File f = new File("starter.properties");
			if (f.exists()) {
				p.load(new FileInputStream(f));
				String mainClass = p.getProperty("main.class");
				if (mainClass == null || mainClass.trim().length() == 0) {
					JOptionPane.showMessageDialog(null, "请在starter.properties配置Main Class！");
					return;
				}
				// classpath路径
				mainClass = mainClass.trim();
				String pClasspath = p.getProperty("classpath");
				Set<String> classpathes = new LinkedHashSet<String>();
				classpathes.add("classes");
				if (pClasspath != null) {
					String[] cls = pClasspath.split(",");
					for (String s : cls) {
						if (s != null && s.trim().length() > 0) {
							classpathes.add(s.trim());
						}
					}
				}
				// lib路径
				String plibDir = p.getProperty("libDir");
				Set<String> libDirs = new LinkedHashSet<String>();
				libDirs.add("lib");
				if (plibDir != null) {
					String[] dirs = plibDir.split(",");
					for (String s : dirs) {
						if (s != null && s.trim().length() > 0) {
							classpathes.add(s.trim());
						}
					}
				}
				// 命令行
//				String osName = System.getProperties().getProperty("os.name");
//				if (osName.toLowerCase().indexOf("windows") != -1) {
					StringBuffer batCommandLine = new StringBuffer();
					// windows
					batCommandLine.append("cmd /c \"start javaw -cp ");
					appendCpDirs(classpathes, libDirs, batCommandLine, ";");
					batCommandLine.append(" ").append(mainClass).append("\"");
					// 命令行文件
					Writer batWriter = new FileWriter(new File("start.bat"));
					batWriter.write("cd " + new File("").getAbsolutePath() + "\n");
					batWriter.write(batCommandLine.toString());
					batWriter.close();
//				} else {
					StringBuffer shellCommandLine = new StringBuffer();
					// 其他
					shellCommandLine.append("java -cp ");
					appendCpDirs(classpathes, libDirs, shellCommandLine, ":");
					shellCommandLine.append(" ").append(mainClass);
					// 命令行文件
					Writer shellWriter = new FileWriter(new File("start.sh"));
					shellWriter.write("cd " + new File("").getAbsolutePath() + "\n");
					shellWriter.write(shellCommandLine.toString());
					shellWriter.close();
//				}
				JOptionPane.showMessageDialog(null, "已生成启动脚本");
			} else {
				OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), "utf-8");
				w.write("# java main 类\n");
				w.write("main.class=\n");
				w.write("# classpath 路径 , 用逗号隔开的多个目录\n");
				w.write("classpath=classes,config\n");
				w.write("# lib 路径 , 用逗号隔开的多个目录\n");
				w.write("libdir=lib\n");
				w.close();
				JOptionPane.showMessageDialog(null, "系统自动生成了starter.properties，請填写main.class后再次执行！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "生成脚本出错:" + e.getMessage());
		}
	}

	private static void appendCpDirs(Set<String> classpathes, Set<String> libDirs, StringBuffer commandLine, String spliter) {
		for (String cls : classpathes) {
			if (new File(cls).isDirectory()) {
				commandLine.append("classes").append(spliter);
			}
		}
		for (String dir : libDirs) {
			File libDir = new File(dir);
			if (libDir.isDirectory()) {
				List<File> fileList = getLibFile(libDir);
				for (File file : fileList) {
					commandLine.append(file.getAbsolutePath().substring(libDir.getAbsolutePath().length() - libDir.getName().length())).append(spliter);
				}
			}
		}
	}

	private static List<File> getLibFile(File libDir) {
		List<File> fileList = new ArrayList<File>();
		File[] subFileList = libDir.listFiles();
		for (int i = 0; i < subFileList.length; i++) {
			File file = subFileList[i];
			if (file.isFile()) {
				if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
					fileList.add(file);
				}
			} else if (file.isDirectory()) {
				fileList.addAll(getLibFile(file));
			}
		}
		return fileList;
	}
}
