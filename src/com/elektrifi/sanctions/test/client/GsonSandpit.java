package com.elektrifi.sanctions.test.client;

import com.google.gson.Gson;
import com.elektrifi.sanctions.beans.UserBean;

public class GsonSandpit {

	public static void main(String[] args) {
		
		UserBean userBean = new UserBean();		
		userBean.setUserName("demoUser");
		userBean.setPassword("lk23_yo19#");
	
		Gson gson = new Gson();
		String requestJson = gson.toJson(userBean);
		System.out.println("requestJson: " + requestJson);
		
		UserBean newUserBean = new UserBean();
		newUserBean = gson.fromJson(requestJson, UserBean.class);
		System.out.println("Recovered newUserBean: " + "\n" 
				+ "UserName: " + newUserBean.getUserName() + "\n"
				+ "Password: " + newUserBean.getPassword()
				);
		
	}
	
}
