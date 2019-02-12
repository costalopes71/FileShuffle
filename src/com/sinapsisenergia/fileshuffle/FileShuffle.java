package com.sinapsisenergia.fileshuffle;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.Instant;

import javafx.scene.shape.Path;

/**
 * 
 * Class responsible for encrypting and decrypting files.
 * 
 * @author Joao Lopes
 * @since February 11th 2019 | v1.0
 * @version v1.0
 *
 */
public final class FileShuffle {

	private File file;
	private static final String SIGNATURE = "9203903X4CRYPTX01927";
	private final byte[] signature_bytes = SIGNATURE.getBytes();
	private final long bytesSize = signature_bytes.length;

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

	/**
	 * Method that encrypts the file. The boolean parameter indicates whether
	 * encryption must be done on the whole file or only part of it.
	 * 
	 * @param <bold>boolean</bold> wholeFile, indicates whether encryption must be
	 *        done on the whole file or only part of it.
	 * @throws IOException if the file is smaller than 8 bytes or if an error ocurred while trying to read the file. Or if the encryption signature is already present.
	 */
	public void encrypt(boolean wholeFile) throws IOException {

		Instant start = Instant.now();

		//
		// getting the day of current date to serve as seed to change bytes
		//
		final long seed = Instant.now().toEpochMilli();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		if (raf.length() < 8) {
			raf.close();
			throw new IOException("To small to encrypt");
		}

		byte[] readBytes = new byte[(int)bytesSize];
		try {
			raf.seek(raf.length() - bytesSize);
			raf.read(readBytes);
		} catch (Exception e) { }
		
		for (int i = 0; i < signature_bytes.length; i++) {
			
			if (signature_bytes[i] != readBytes[i]) {
				break;
			}
			
			if (i == signature_bytes.length - 1) {
				
				if (signature_bytes[i] == readBytes[i]) {
					raf.close();
					throw new IOException("File was already encrypted!");
				}
				
			}
			
		}
		
		raf.seek(0);

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
				raf.write(signature_bytes);
				raf.close();

			} catch (Exception e) {
				throw new IOException(e);
			}

		} else {

			encrypt5000bytes(raf, seed);

		}

		System.out.println("Elapsed: " + Duration.between(start, Instant.now()).getSeconds() + " seconds");

	}

	/**
	 * Method that decrypts the file. The boolean parameter indicates whether
	 * decryption must be done on the whole file or only part of it.
	 * 
	 * @param <bold>boolean</bold> wholeFile, indicates whether decryption must be
	 *        done on the whole file or only part of it.
	 * @throws IOException if the file is smaller than 8 bytes or if an error ocurred while trying to read the file. Or if the encryption signature is not present.
	 */
	public void decrypt(boolean wholeFile) throws IOException {

		Instant start = Instant.now();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		if (raf.length() < 8) {
			raf.close();
			throw new IOException("To small to decrypt");
		}

		//
		// test if crypt signature is in the file
		//
		byte[] readBytes = new byte[(int)bytesSize];
		try {
			raf.seek(raf.length() - bytesSize);
			raf.read(readBytes);
		} catch (Exception e) {
			raf.close();
			throw new IOException(e);
		}

		for (int i = 0; i < readBytes.length; i++) {
			
			if (readBytes[i] != signature_bytes[i]) {
				raf.close();
				throw new IOException("Encryption signature not present! Can not decrypt!");
			}
			
		}
		
		
		//
		// cut signature
		//
		raf.seek(0);
		raf.setLength(raf.length() - bytesSize);

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

			decrypt5000Bytes(raf, seed);

		}

		System.out.println("Elapsed: " + Duration.between(start, Instant.now()).getSeconds() + " seconds");

	}

	private void decrypt5000Bytes(RandomAccessFile raf, long seed) throws IOException {

		long maxPosition2Decrypt = raf.length() > 5000 ? 5000 : raf.length();

		try {

			while (true) {

				long readLong = raf.readLong();
				long decrypt = readLong - seed;
				raf.seek(raf.getFilePointer() - 8);
				raf.writeLong(decrypt);

				if (raf.getFilePointer() >= maxPosition2Decrypt)
					break;

			}

			raf.close();

		} catch (EOFException e) {

			raf.close();

		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	private void encrypt5000bytes(RandomAccessFile raf, long seed) throws IOException {

		long maxPosition2Encrypt = raf.length() > 5000 ? 5000 : raf.length();

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
			raf.write(signature_bytes);
			raf.close();

		} catch (EOFException e) {

			raf.writeLong(seed);
			raf.write(signature_bytes);
			raf.close();

		}

	}

}
