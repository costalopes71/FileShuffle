package com.sinapsisenergia.tests;

import java.io.File;
import java.io.IOException;

import com.sinapsisenergia.fileshuffle.FileShuffle;

public class Tests {

	private static final String OIGTHBYTES = "/home/joao/Documentos/workspaces/FileShuffle_workspace/docs/teste.bin";
	@SuppressWarnings("unused")
	private static final String BIG = "/home/joao/Documentos/workspaces/FileEncryptor_workspace/docs/CEB_TODAS_SUB_20190208-160048_750.cbdb";
	
	public static void main(String[] args) {
		
		File file = new File(OIGTHBYTES);
		
		FileShuffle fileShuffle;
		try {
			fileShuffle = new FileShuffle(file );
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			fileShuffle.encrypt(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			fileShuffle.decrypt(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
