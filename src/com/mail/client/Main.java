package com.mail.client;

import com.mail.client.service.MailService;
import com.mail.client.xmpp.XMPPManager;

public class Main {

	public static void main(String[] args) {
		System.out.println("============= starting services ======================");
		try {
			MailService.getInstance().start();
			XMPPManager.getInstance().start();
			System.out.println("============= started services ======================");
		} catch (Exception e) {
			System.out.println("============= unable to start services :( +==========");
			e.printStackTrace();
		}
	}
}
