package com.seideun.java.test.generator.java_reader;

import java.io.File;

public class JavaReader {
	private String clsPath;
	private String clsName;
	private String mtdName;

	public JavaReader(String className, String methodName) {
		String defaultClsPath =
			System.getProperty("user.dir") + File.separator + "target" +
				File.separator + "classes";
		new JavaReader(defaultClsPath, className, methodName);
	}

	public JavaReader(String defaultClsPath, String className,
		String methodName) {
		clsPath = defaultClsPath;
		clsName = className;
		mtdName = methodName;
	}

}
