package app.utils;

import java.nio.ByteBuffer;

public interface MessageReaderOutput {
	
	void receive(ByteBuffer buffer, int length);

}
