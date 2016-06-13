package net.sourceforge.jaad.spi.javasound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

public class AACAudioFileReader extends AudioFileReader {

	public static final AudioFileFormat.Type AAC = new AudioFileFormat.Type("AAC", "aac");
	public static final AudioFileFormat.Type MP4 = new AudioFileFormat.Type("MP4", "mp4");
	private static final AudioFormat.Encoding AAC_ENCODING = new AudioFormat.Encoding("AAC");
	private static final int MARK_READ_LIMIT = 100000;

	@Override
	public AudioFileFormat getAudioFileFormat(InputStream in) throws UnsupportedAudioFileException, IOException {
		try {
			return getAudioFileFormat(in, AudioSystem.NOT_SPECIFIED);
		}
		finally {
			in.reset();
		}
	}

	@Override
	public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
		final URLConnection connection = url.openConnection();
		InputStream in = connection.getInputStream();
		if(! in.markSupported()) in = new BufferedInputStream(in);
		try {
			return getAudioFileFormat(in,connection.getContentLength());
		}
		finally {
			in.close();
		}
	}

	@Override
	public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
		RandomAccessFile raf = new RandomAccessFile(file,"r");
		try{
			return new JaadAudioFileFormat(MP4, (int) file.length(), MP4AudioInputStream.getFormat(raf), AudioSystem.NOT_SPECIFIED);
		}finally{
			raf.close();
		}
	}

	private AudioFileFormat getAudioFileFormat(InputStream in, int mediaLength) throws UnsupportedAudioFileException, IOException {
		if(!in.markSupported()) throw new IOException("Provided InputStream must support mark");
		in.mark(MARK_READ_LIMIT);
		try{
			final byte[] head = new byte[12];
			in.read(head);
			if(! new String(head, 4, 4,"US-ASCII").equals("ftyp")){
				throw new UnsupportedAudioFileException("The string 'ftyp' is not presenting startin at byte 4");
			}
			in.reset();
			in.mark(MARK_READ_LIMIT);
			return new JaadAudioFileFormat(MP4, mediaLength, MP4AudioInputStream.getFormat(in), AudioSystem.NOT_SPECIFIED);
		}finally{
			in.reset();
		}
	}

	//================================================
	private AudioInputStream getAudioInputStream(InputStream in, int mediaLength) throws UnsupportedAudioFileException, IOException {
		final AudioFileFormat aff = getAudioFileFormat(in, mediaLength);
		return new MP4AudioInputStream(in, aff.getFormat(), aff.getFrameLength());
	}

	@Override
	public AudioInputStream getAudioInputStream(InputStream in) throws UnsupportedAudioFileException, IOException {
		return getAudioInputStream(in, AudioSystem.NOT_SPECIFIED);
	}

	@Override
	public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
		final URLConnection connection = url.openConnection();
		final InputStream in = new BufferedInputStream(connection.getInputStream());
		return getAudioInputStream(in, connection.getContentLength());
	}

	@Override
	public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
		final AudioFileFormat aff = getAudioFileFormat(file);
		return new MP4AudioInputStream(new RandomAccessFile(file,"r"), aff.getFormat(), aff.getFrameLength());
	}
}
