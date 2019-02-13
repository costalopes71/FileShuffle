package com.sinapsisenergia.usageexample;

import java.io.File;
import java.io.IOException;

import com.sinapsisenergia.fileshuffle.FileShuffle;
import com.sinapsisenergia.fileshuffle.exception.EncryptException;

public class UsageExample {

	@SuppressWarnings("unused")
	private static final String SMALL_FILE = "";
	private static final String BIG_FILE = "/home/joao/Documentos/CEB_TODAS_SUB_20190213-130349_551.cbdb";
	
	public static void main(String[] args) {
		
		File file = new File(BIG_FILE);
		
		FileShuffle fileShuffle;
		
		try {
			fileShuffle = new FileShuffle(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
//		try {
//			fileShuffle.encrypt(true);
//		} catch (IOException | EncryptException e) {
//			e.printStackTrace();
//		}
		
		try {
			fileShuffle.decrypt(true);
		} catch (IOException | EncryptException e) {
			e.printStackTrace();
		}
		
	}
	
}
