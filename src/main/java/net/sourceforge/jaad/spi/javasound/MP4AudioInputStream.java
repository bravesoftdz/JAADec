package net.sourceforge.jaad.spi.javasound;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.aac.syntax.Constants;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.logging.Level;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.sourceforge.jaad.mp4.api.Track;

class MP4AudioInputStream extends AsynchronousAudioInputStream {

	private final AudioTrack track;
	private final Decoder decoder;
	private final SampleBuffer sampleBuffer;
	private final RandomAccessFile raf;
	private final InputStream in;

	MP4AudioInputStream(RandomAccessFile raf, AudioFormat format, long length) throws IOException {
		super(format, length);
		this.raf = raf;
		this.in = null;
		final MP4Container cont = new MP4Container(raf);
		final Movie movie = cont.getMovie();
		if(movie == null){
			throw new AACException("movie does not contain any movie info");
		}
		final List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
		if(tracks.isEmpty()) throw new AACException("movie does not contain any AAC track");
		track = (AudioTrack) tracks.get(0);

		decoder = new Decoder(track.getDecoderSpecificInfo());
		sampleBuffer = new SampleBuffer();
	}

	MP4AudioInputStream(InputStream in, AudioFormat format, long length) throws IOException {
		super(format, length);
		this.raf = null;
		this.in = in;
		final MP4Container cont = new MP4Container(in);
		final Movie movie = cont.getMovie();
		final List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
		if(tracks.isEmpty()) throw new IOException("movie does not contain any AAC track");
		track = (AudioTrack) tracks.get(0);

		decoder = new Decoder(track.getDecoderSpecificInfo());
		sampleBuffer = new SampleBuffer();
	}

	static AudioFormat getFormat(MP4Container cont) throws IOException,UnsupportedAudioFileException {
		try{
			final SampleBuffer sampleBuffer = new SampleBuffer();
			final Movie movie = cont.getMovie();
			if(movie == null){
				throw new UnsupportedAudioFileException("movie does not contain any movie info");
			}
			final List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
			if(tracks.isEmpty()){
				throw new UnsupportedAudioFileException("movie does not contain any AAC tracks");
			}
			final Track track = (AudioTrack) tracks.get(0);
			final Decoder decoder = new Decoder(track.getDecoderSpecificInfo());
			//read first frame
			if(!track.hasMoreFrames()) {
				throw new UnsupportedAudioFileException("No frames found");
			}
			final Frame frame = track.readNextFrame();
			if(frame==null) {
				throw new UnsupportedAudioFileException("No next frame found");
			}
			decoder.decodeFrame(frame.getData(), sampleBuffer);
			if(sampleBuffer.getBitsPerSample()<=0){
				throw new UnsupportedAudioFileException("Invalid or unsupported audio data");
			}else{
				return new AudioFormat(sampleBuffer.getSampleRate(), sampleBuffer.getBitsPerSample(), sampleBuffer.getChannels(), true, true);
			}
		}catch(AACException e){
			Constants.LOGGER.log(Level.INFO, "Exception parsing AAC", e);
			throw new UnsupportedAudioFileException("Exception parsing AAC: " + e.getMessage());
		}
	}

	static AudioFormat getFormat(InputStream buffer) throws IOException,UnsupportedAudioFileException {
		return getFormat(new MP4Container(buffer));
	}

	static AudioFormat getFormat(RandomAccessFile raf) throws IOException,UnsupportedAudioFileException {
		return getFormat(new MP4Container(raf));
	}

	public void execute() {
		decodeFrame();
		if(buffer.isOpen()) buffer.write(sampleBuffer.getData());
	}

	private void decodeFrame() {
		if(!track.hasMoreFrames()) {
			buffer.close();
			return;
		}
		try {
			final Frame frame = track.readNextFrame();
			if(frame==null) {
				buffer.close();
				return;
			}
			decoder.decodeFrame(frame.getData(), sampleBuffer);
		}
		catch(IOException e) {
			buffer.close();
			return;
		}
	}

	@Override
	public void close() throws IOException {
		try{
			if(raf!=null) raf.close();
			if(in!=null) in.close();
		}finally{
			// ignore
		}
		super.close();
	}
}
