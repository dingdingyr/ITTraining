package com.pwc.ittraining.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Environment;

public class FileUtility {
	private String SDCardRoot;
	private String SDStateString;

	public FileUtility() {
		// get the root path of the SD card
		SDCardRoot = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator;
		// get the state of the SD card
		SDStateString = Environment.getExternalStorageState();
	}

	public File createFileInSDCard(String dir, String fileName)
			throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		file.createNewFile();
		return file;
	}
	
	public void deleteFileInSDCard(String dir, String fileName){
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		file.delete();
	}

	/**
	 * Create a folder in the SD card
	 * 
	 * @param dir
	 *            folder name
	 * @return
	 */
	public File creatSDDir(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		return dirFile;
	}

	/**
	 * Check whether the file exists in the folder
	 * 
	 * @param dir
	 *            folder name
	 * @param fileName
	 *            file name
	 * @return
	 */
	public boolean isFileExist(String dir, String fileName) {
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		return file.exists();
	}

	/***
	 * Get the full path of a file
	 * 
	 * @param dir
	 * @param fileName
	 * @return
	 */
	public String getFilePath(String dir, String fileName) {
		return SDCardRoot + dir + File.separator + fileName;
	}

	/***
	 * Get the left storage of the SD card in Byte
	 * 
	 * @return
	 */
	public long getSDAvailableSize() {
		if (SDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {

			File pathFile = android.os.Environment
					.getExternalStorageDirectory();
			android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
	
			long nBlocSize = statfs.getBlockSize();

			long nAvailaBlock = statfs.getAvailableBlocks();

			long nSDFreeSize = nAvailaBlock * nBlocSize;
			return nSDFreeSize;
		}
		return 0;
	}

	/**
	 * Write a bytes array to SD card
	 */
	public boolean write2SD(String dir, String fileName, byte[] bytes) {

		if (bytes == null) {
			return false;
		}

		OutputStream output = null;
		try {
			if (SDStateString.equals(android.os.Environment.MEDIA_MOUNTED)
					&& bytes.length < getSDAvailableSize()) {
				File file = null;
				creatSDDir(dir);
				file = createFileInSDCard(dir, fileName);
				output = new BufferedOutputStream(new FileOutputStream(file));
				output.write(bytes);
				output.flush();
				return true;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/***
	 * Get a file from SD card in Byte array
	 * 
	 * @param dir
	 * @param fileName
	 * @return
	 */
	public byte[] readFromSD(String dir, String fileName) {
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		if (!file.exists()) {
			return null;
		}
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Write the data in InputStream to SD card, read image from internet
	 */
	public File write2SDFromInput(String dir, String fileName, InputStream input) {

		File file = null;
		OutputStream output = null;
		try {
			int size = input.available();
			if (SDStateString.equals(android.os.Environment.MEDIA_MOUNTED)
					&& size < getSDAvailableSize()) {
				creatSDDir(dir);
				file = createFileInSDCard(dir, fileName);
				output = new BufferedOutputStream(new FileOutputStream(file));
				byte buffer[] = new byte[4 * 1024];
				int temp;
				while ((temp = input.read(buffer)) != -1) {
					output.write(buffer, 0, temp);
				}
				output.flush();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return file;
	}

}
