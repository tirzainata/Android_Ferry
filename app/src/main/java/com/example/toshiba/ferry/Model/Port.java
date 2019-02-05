package com.example.toshiba.ferry.Model;

public class Port {
	private String Name, Location;

	public Port(String name, String location) {
		Name = name;
		Location = location;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getLocation() {
		return Location;
	}

	public void setLocation(String location) {
		Location = location;
	}
}
