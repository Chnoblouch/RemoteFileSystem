package app;

public class MessageTypes {
	
	public static final byte STARTING_DIRECTORY = 0;
	
	public static final byte REQUEST_FILES = 1;
	public static final byte FILES = 2;
	
	public static final byte CREATE_FILE = 3;
	public static final byte DELETE_FILE = 4;
	public static final byte MOVE_FILE = 5;
	public static final byte COPY_FILE = 6;
	
	public static final byte UPLOAD_FILE_DATA = 7;
	public static final byte REQUEST_FILE_DATA = 8;
	
	public static final byte RUN_FILE = 9;
	
	public static final byte FILE_CONTENT = 10;
	public static final byte SYSTEM_PROCESS_STARTED = 11;
	public static final byte SYSTEM_PROCESS_OUTPUT = 12;
	
	public static final byte WRITE_FILE_DATA = 13;
	public static final byte SYSTEM_PROCESS_INPUT = 14;
	public static final byte SYSTEM_PROCESS_STOP = 15;

}
