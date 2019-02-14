package com.sinapsisenergia.fileshuffle;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.Instant;
import java.util.zip.GZIPInputStream;

import com.sinapsisenergia.fileshuffle.exception.EncryptException;

import javafx.scene.shape.Path;

/**
 * 
 * Class responsible for encrypting and decrypting files.
 * 
 * @author Joao Lopes
 * @since February 11th 2019 | v1.0
 * @version v1.2
 *
 */
public final class FileShuffle {

	//
	// instance attributes
	//
	private File file;

	private final byte[] fullFileSignatureBytes = FULL_FILE_SIGNATURE.getBytes();
	private final byte[] partialFileSignatureBytes = PARTIAL_FILE_SIGNATURE.getBytes();
	private final long fullFileArraySize = fullFileSignatureBytes.length;
	private final long partialFileArraySize = partialFileSignatureBytes.length;
	private byte isCompressed = 0;
	
	//
	// constants
	//
	private static final String FULL_FILE_SIGNATURE = "9203903X4CRYPTX01927";
	private static final String PARTIAL_FILE_SIGNATURE = "0003903X4CRYPTX01000";

	//
	// Constructors
	//

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

	//
	// methods
	//

	/**
	 * Method that encrypts the file. The boolean parameter indicates whether
	 * encryption must be done on the whole file or only part of it.
	 * 
	 * @param <bold>boolean</bold> wholeFile, indicates whether encryption must be
	 *        done on the whole file or only part of it.
	 * @throws IOException      if an error ocurred while trying to read the file.
	 *                          Or if the encryption signature is already present.
	 * @throws EncryptException if the file is smaller than 8 bytes or if encryption
	 *                          signature was found (in other words the file has
	 *                          already been ecrypted)
	 */
	public void encrypt(boolean fullEncrypt) throws IOException, EncryptException {

		Instant start = Instant.now();
		
		//
		// getting the day of current date to serve as seed to shuffle bytes
		//
		final long seed = Instant.now().toEpochMilli();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		
		// TODO create a method to encrypt files smaller than 8 bytes
		if (raf.length() < 8) {
			raf.close();
			throw new EncryptException("To small to encrypt");
		}
		
		if (isSignaturePresent(raf)) {
			raf.close();
			throw new EncryptException("File already encrypted!!! Cannot encrypt again!");
		}
		
		if ((isGZipped(file) || isZipFile(file)) && raf.length() > 5000 && fullEncrypt == false) {
			isCompressed = 1;
			encryptEndOfFile(raf, seed);
		}

		if (fullEncrypt) {

			try {

				while (true) {

					long readLong = raf.readLong();
					long encrypt = readLong + seed;
					raf.seek(raf.getFilePointer() - 8);
					raf.writeLong(encrypt);

				}

			} catch (EOFException e) {
				
				raf.writeByte(isCompressed);
				raf.writeLong(seed);
				raf.write(fullFileSignatureBytes);
				raf.close();

			} catch (Exception e) {
				throw new IOException(e);
			}

		} else {

			encrypt5000bytes(raf, seed);

		}

		System.out.println("Elapsed: " + Duration.between(start, Instant.now()).getSeconds() + " seconds");

	}

	private void encryptEndOfFile(RandomAccessFile raf, long seed) throws IOException {

		long endBytes = (raf.length() - 5000) > 2500 ? 2500 : (raf.length() - 5000); 
		
		raf.seek(raf.length() - endBytes);
		
		try {
			while (true) {
				
				long readLong = raf.readLong();
				long encrypt = readLong + seed;
				raf.seek(raf.getFilePointer() - 8);
				raf.writeLong(encrypt);
				
			}
		} catch (EOFException e) {
			raf.seek(0);
			return;
		}
		
	}

	/**
	 * Method that decrypts the file. The boolean parameter indicates whether
	 * decryption must be done on the whole file or only part of it.
	 * 
	 * @param <bold>boolean</bold> fullDecrypt, indicates whether decryption must be
	 *        done on the whole file or only part of it.
	 * @throws IOException      or if an error ocurred while trying to read the
	 *                          file.
	 * @throws EncryptException if the file is smaller than 8 bytes, if the
	 *                          encryption signature is not present, if the file was
	 *                          encrypted using different option (full or partial)
	 *                          or if it was not possible to determine the type of
	 *                          signature (full or partial)
	 */
	public void decrypt() throws IOException, EncryptException {

		Instant start = Instant.now();

		RandomAccessFile raf = new RandomAccessFile(file, "rw");

		if (raf.length() < 8) {
			raf.close();
			throw new EncryptException("To small to decrypt");
		}

		//
		// test if crypt signature is in the file
		//
		boolean fullFile = isFullFileSignaturePresent(raf);
		raf.seek(0);
		boolean partialFile = isPartialFileSignaturePresent(raf);

		if (fullFile == false && partialFile == false) {
			raf.close();
			throw new EncryptException("Encryption signature is not present in the file! Cannot decrypt!");
		}

		//
		// cut signature
		//
		raf.seek(0);
		if (fullFile) {
			raf.setLength(raf.length() - fullFileArraySize);
		} else if (partialFile) {
			raf.setLength(raf.length() - partialFileArraySize);
		} else {
			raf.close();
			throw new EncryptException(
					"Could not determine full or partial file encryption signature! Cannot decrypt!");
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

		//
		// get isCompressed flag
		//
		raf.seek(raf.length() - 1);
		isCompressed = raf.readByte();
		raf.setLength(raf.length() - 1);
		
		if (isCompressed == 1) {
			decryptEndFile(raf, seed);
		}
		
		raf.seek(0);

		if (fullFile) {

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

	private void decryptEndFile(RandomAccessFile raf, long seed) throws IOException {
		
		long endBytes = (raf.length() - 5000) > 2500 ? 2500 : (raf.length() - 5000); 
		
		raf.seek(raf.length() - endBytes);
		
		try {
			while (true) {
				
				long readLong = raf.readLong();
				long decrypt = readLong - seed;
				raf.seek(raf.getFilePointer() - 8);
				raf.writeLong(decrypt);
				
			}
		} catch (EOFException e) {
			raf.seek(0);
			return;
		}
		
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

		long maxPosition2Encrypt = raf.length() >= 5000 ? 5000 : raf.length();

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
			raf.writeByte(isCompressed);
			raf.writeLong(seed);
			raf.write(partialFileSignatureBytes);
			raf.close();

		} catch (EOFException e) {

			raf.writeByte(isCompressed);
			raf.writeLong(seed);
			raf.write(partialFileSignatureBytes);
			raf.close();

		}

	}

	private boolean isFullFileSignaturePresent(RandomAccessFile raf) throws IOException {

		byte[] readBytes = new byte[(int) fullFileArraySize];

		try {
			raf.seek(raf.length() - fullFileArraySize);
			raf.read(readBytes);
		} catch (Exception e) {
		}

		for (int i = 0; i < fullFileSignatureBytes.length; i++) {

			if (fullFileSignatureBytes[i] != readBytes[i]) {
				return false;
			}

		}

		return true;
	}

	private boolean isPartialFileSignaturePresent(RandomAccessFile raf) throws IOException {

		byte[] readBytes = new byte[(int) partialFileArraySize];

		try {
			raf.seek(raf.length() - partialFileArraySize);
			raf.read(readBytes);
		} catch (Exception e) {
		}

		for (int i = 0; i < partialFileSignatureBytes.length; i++) {

			if (partialFileSignatureBytes[i] != readBytes[i]) {
				return false;
			}

		}

		return true;
	}

	private boolean isSignaturePresent(RandomAccessFile raf) throws IOException {

		if (isFullFileSignaturePresent(raf))
			return true;

		raf.seek(0);

		if (isPartialFileSignaturePresent(raf))
			return true;

		raf.seek(0);

		return false;
	}

	private static boolean isGZipped(File f) {
		int magic = 0;

		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
			raf.close();
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}

		return magic == GZIPInputStream.GZIP_MAGIC;
	}

	private static boolean isZipFile(File file) throws IOException {

		if (file.length() < 4) {
			return false;
		}

		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		int magic = in.readInt();
		in.close();
		return magic == 0x504b0304;
	}

}
