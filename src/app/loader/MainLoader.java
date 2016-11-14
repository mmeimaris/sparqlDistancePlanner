package app.loader;

public class MainLoader {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Loader l = new Loader();
		try {
			l.persistLUBM(args[0],args[1]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
