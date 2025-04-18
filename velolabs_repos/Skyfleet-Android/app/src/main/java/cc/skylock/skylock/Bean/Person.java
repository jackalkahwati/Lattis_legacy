package cc.skylock.skylock.Bean;

/**
 * Simplified take on the model vended by http://api.randomuser.me/
 */
public class Person {

	public static class Name {
		public String title;
		public String first;
		public String last;
	}

	public static class Location {
		public String street;
		public String city;
		public String state;
		public String postcode;
	}

	public static class Picture {
		public static String large;
		public static String medium;
		public static String thumbnail;
	}

	public String name;
	public Location location;
	public String email;
	public String phone;
	public String cell;
	public String contactId;
	public Picture picture;
	public boolean isSelected = false;

	public Person() {
	}
}
