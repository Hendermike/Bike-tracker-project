package com.example.dani.biketracker.controlModule;

public class pairStatePackage {
	
	double currentBicyclePositon;
	double currentBicycleSpeed;
	double previousBicyclePositon;
	double previousBicycleSpeed;
	double leaderSpeed;
	double desiredSpacing;
	
	public pairStatePackage(double currentBicyclePositon, double currentBicycleSpeed, double previousBicyclePositon, double previousBicycleSpeed, double leaderSpeed, double desiredSpacing) {
		this.currentBicyclePositon = currentBicyclePositon;
		this.currentBicycleSpeed = currentBicycleSpeed;
		this.previousBicyclePositon = previousBicyclePositon;
		this.previousBicycleSpeed = previousBicycleSpeed;
		this.leaderSpeed = leaderSpeed;
		this.desiredSpacing = desiredSpacing;
	}
	
	//GETTERS
	public double getCurrentBicyclePositon() {
		return this.currentBicyclePositon;
	}
	
	public double getCurrentBicycleSpeed() {
		return this.currentBicycleSpeed;
	}
	
	public double getPreviousBicyclePositon() {
		return this.previousBicyclePositon;
	}
	
	public double getPreviousBicycleSpeed() {
		return this.previousBicycleSpeed;
	}
	
	public double getLeaderSpeed() {
		return this.leaderSpeed;
	}

	public double getDesiredSpacing() {return this.desiredSpacing;}

	//SETTERS
	public void setCurrentBicyclePositon(double position) {
		this.currentBicyclePositon = position;
	}
	
	public void setCurrentBicycleSpeed(double speed) {
		this.currentBicycleSpeed = speed;
	}
	
	public void setPreviousBicyclePositon(double position) {
		this.previousBicyclePositon = position;
	}
	
	public void setPreviousBicycleSpeed(double speed) {
		this.previousBicycleSpeed = speed;
	}
	
	public void setLeaderSpeed(double speed) {
		this.leaderSpeed = speed;
	}

	public void setDesiredSpacing(double DesiredSpacing) {this.desiredSpacing = DesiredSpacing;}
	
}
