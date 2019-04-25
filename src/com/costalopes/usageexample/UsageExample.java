package com.costalopes.usageexample;

import java.io.File;
import java.io.IOException;

import com.costalopes.fileshuffle.FileShuffle;
import com.costalopes.fileshuffle.exception.EncryptException;

public class UsageExample {
	
	//
	// insert file absolute path
	//
	private static final String FILE_PATH = "/home/joao/Documentos/example.txt";
	
	public static void main(String[] args) {
		
		File file = new File(FILE_PATH);
		
		FileShuffle fileShuffle;
		
		try {
			fileShuffle = new FileShuffle(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			boolean a = fileShuffle.isEncrypted();
			System.out.println(a);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		//
		// use code bellow to encrypt the file
		//
		
		
//		try {
//			fileShuffle.encrypt(false);
//		} catch (IOException | EncryptException e) {
//			e.printStackTrace();
//		}
		
		//
		// use code bellow to decrypt the file
		//
		
		try {
			fileShuffle.decrypt();
		} catch (IOException | EncryptException e) {
			e.printStackTrace();
		}
		
	}
	
}
