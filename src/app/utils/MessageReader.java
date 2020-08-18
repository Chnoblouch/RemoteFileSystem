package app.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MessageReader {
	
	private InputStream inputStream;
	private MessageReaderOutput output;
	
	private int messageLength = -1;
	private int bytesLeft = 0;
	private ByteBuffer buffer = null;
	
	public MessageReader(InputStream inputStream, MessageReaderOutput output) {
		this.inputStream = inputStream;
		this.output = output;
	}
	
	public void read() {
		try {
			if(messageLength == -1) readMessageLength();
			else readMessage();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readMessageLength() throws IOException {
		if(inputStream.available() >= Integer.BYTES) {				
			byte[] bytes = new byte[Integer.BYTES];
			inputStream.read(bytes);
			ByteBuffer lengthBuffer = ByteBuffer.wrap(bytes);
			
			messageLength = lengthBuffer.getInt(0);
			bytesLeft = messageLength;			
			buffer = ByteBuffer.allocate(messageLength);
		}
	}
	
	private void readMessage() throws IOException {
		if(inputStream.available() > 0) {
			int length = Math.min(inputStream.available(), Math.min(2048, bytesLeft));
			byte[] tempBuffer = new byte[length];
			
			bytesLeft -= length;
			
			inputStream.read(tempBuffer);
			buffer.put(tempBuffer);
						
			if(bytesLeft < 1) {
				output.receive(buffer, messageLength);
				
				messageLength = -1;
				buffer = null;
			}
		}
	}

}
