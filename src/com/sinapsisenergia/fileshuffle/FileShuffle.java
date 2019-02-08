package com.sinapsisenergia.fileshuffle;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.Instant;

import javafx.scene.shape.Path;

public final class FileShuffle {

	private File file;
	
	public FileShuffle(File file) throws IOException {
		this.file = file;
		
		if (!file.exists() || file.isDirectory() || !file.canRead() || !file.canWrite()) {
			throw new IOException();
		}
		
	}
	
	public FileShuffle(String filePath) throws IOException {
		this(new File(filePath));
	}
	
	public FileShuffle(Path path) throws IOException {
		this(path.toString());
	}
	
	public void encrypt(boolean wholeFile) throws IOException {
		
		Instant start = Instant.now();
		
		//
		// getting the day of current date to serve as seed to change bytes
		//
		final long seed = file.lastModified();
		
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		
		if (raf.length() < 8) {
			raf.close();
			throw new IOException("To small to encrypt");
		}
		
		if (wholeFile) {
			
			try {
				
				while (true) {
					
					long readLong = raf.readLong();
					long encrypt = readLong + seed;
					raf.seek(raf.getFilePointer() - 8);
					raf.writeLong(encrypt);
					
				}
				
				
			} catch (EOFException e) {
				
				raf.writeLong(seed);
				raf.close();
				
			} catch (Exception e) {
				throw new IOException(e);
			}
			
		} else {
			
			encrypt5Percent(raf, seed);
			
		}
		
		System.out.println("Elapsed: " + Duration.between(start, Instant.now()).getSeconds() + " seconds");
		
	}
	
	public void decrypt(boolean wholeFile) throws IOException {
		
		Instant start = Instant.now();
		
		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		if (raf.length() < 8) {
			raf.close();
			throw new IOException("To small to decrypt");
		}
		
		//
		// retrieve seed
		//
		raf.seek(raf.length() - 8);
		final long seed = raf.readLong();

		//
		// cut seed
		//
		raf.setLength(raf.length() - 8);
		
		raf.seek(0);
		
		if (wholeFile) {
			
			try {
				
				while (true) {
					
					long readLong = raf.readLong();
					long decrypt = readLong - seed;
					raf.seek(raf.getFilePointer() - 8);
					raf.writeLong(decrypt);
					
				}
				
				
			} catch (EOFException e) {
				
				raf.close();
				
			} catch (Exception e) {
				throw new IOException(e);
			}
			
		} else {
			
			decrypt5Percent(raf, seed);
			
		}
		
		System.out.println("Elapsed: " + Duration.between(start, Instant.now()).getSeconds() + " seconds");
		
	}
	
	private void decrypt5Percent(RandomAccessFile raf, long seed) throws IOException {

		//
		// getting 5% only
		//
		long maxPosition2Encrypt = (raf.length() / 100) * 5;
		
		if (maxPosition2Encrypt < 8) {
			maxPosition2Encrypt = raf.length();
		}
		
		try {
			
			while (true) {
				
				long readLong = raf.readLong();
				long decrypt = readLong - seed;
				raf.seek(raf.getFilePointer() - 8);
				raf.writeLong(decrypt);
				
				if (raf.getFilePointer() >= maxPosition2Encrypt)
					break;
				
			}
			
			raf.close();
			
		} catch (EOFException e) {
			
			raf.close();
			
		} catch (Exception e) {
			throw new IOException(e);
		}
			
	}

	private void encrypt5Percent(RandomAccessFile raf, long seed) throws IOException {
		
		//
		// getting 5% only
		//
		long maxPosition2Encrypt = (raf.length() / 100) * 5;
		
		if (maxPosition2Encrypt < 8) {
			maxPosition2Encrypt = raf.length();
		}
		
		try {
			
			while (true) {
				
				long readLong = raf.readLong();
				long encrypt = readLong + seed;
				raf.seek(raf.getFilePointer() - 8);
				
				raf.writeLong(encrypt);
				
				if (raf.getFilePointer() >= maxPosition2Encrypt)
					break;
				
			}
			
			raf.seek(raf.length());
			raf.writeLong(seed);
			raf.close();
			
		} catch (EOFException e) {
			
			raf.seek(raf.length());
			raf.writeLong(seed);
			raf.close();
			
		}
		
	}
	
}
