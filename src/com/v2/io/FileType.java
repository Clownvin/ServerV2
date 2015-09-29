package com.v2.io;

/**
 * 
 * @author Calvin Gene Hall
 *
 */

public enum FileType {
	USER("./Data/Users/", ".user"), CONFIGURATION("./Data/Configuration/", ".cfg"), MODERATION("./Data/Moderations/",
			".moderation");
	private final String path;
	private final String extension;

	private FileType(String path, String extension) {
		this.path = path;
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public String getPath() {
		return path;
	}
}
