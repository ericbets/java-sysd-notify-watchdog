package c;

public class sysdnotify {
	static
	{		
		NarSystem.loadLibrary();
			}

	public native String pingWatchdog();


	public static void main(final String[] args) {	
		sysdnotify c = new sysdnotify();
		System.out.println("Success:" + c.pingWatchdog().equals("Notified"));
	}
}
