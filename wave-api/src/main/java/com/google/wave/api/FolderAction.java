package com.google.wave.api;

public enum FolderAction {

	MARK_AS_READ("markAsRead"), MARK_AS_UNREAD("markAsUnread"), MUTE("mute"), ARCHIVE(
			"archive");

	private final String s;

	private FolderAction(String value) {
		s = value;
	}

	@Override
	public String toString() {
		return s;
	}
}