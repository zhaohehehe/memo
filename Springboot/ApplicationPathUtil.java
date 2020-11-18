package com.bonc.dataplatform.buw.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ClassPathResource;

public class ApplicationPathUtil {
	private ApplicationPathUtil() {
	}

	public static String getSpringbootWarRootPath() throws IOException {
		String path = new ClassPathResource("").getFile().getAbsolutePath();
		return java.net.URLDecoder.decode(Paths.get(path).getParent().getParent().toString(), "UTF-8");
	}

	public static String getSpringbootJarRootPath() throws UnsupportedEncodingException {
		ApplicationHome home = new ApplicationHome(ApplicationPathUtil.class);
		File jarFile = home.getSource().getParentFile();
		return java.net.URLDecoder.decode(jarFile.getPath(), "UTF-8");
	}
	
	public static InputStream loadClassPathResource(String path) throws IOException {
		ClassPathResource resource = new ClassPathResource(path);
		return resource.getInputStream();
	}
}
