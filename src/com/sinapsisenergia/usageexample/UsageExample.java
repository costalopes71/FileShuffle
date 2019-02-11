package com.sinapsisenergia.usageexample;

import java.io.File;
import java.io.IOException;

import com.sinapsisenergia.fileshuffle.FileShuffle;

public class UsageExample {

	private static final String SMALL_FILE = "";
	@SuppressWarnings("unused")
	private static final String BIG_FILE = "";
	
	public static void main(String[] args) {
		
		File file = new File(SMALL_FILE);
		
		FileShuffle fileShuffle;
		
		try {
			fileShuffle = new FileShuffle(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		//encrypt only 5% (boolean false indicates not to encrypt the whole file)
		try {
			fileShuffle.encrypt(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// decrypt only 5% (boolean false indicates not to decrypt the whole file)
		try {
			fileShuffle.decrypt(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
