package com.mail.client.xmpp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import com.mail.client.model.UserModel;

public class XMPPManager extends Thread {

	public static String CHAT_HOST = "";
	private static int CHAT_PORT = 5222;
	private static String userName = "";
	private static String password = "";

	private XMPPTCPConnection eConnection = null;

	private static XMPPManager xmppManager;

	private XMPPManager() {
	}

	public static XMPPManager getInstance() {
		if (xmppManager == null) {
			xmppManager = new XMPPManager();
		}
		return xmppManager;
	}

	public void login() {
		try {
			XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration
					.builder();
			config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
			config.setUsernameAndPassword(userName, password);
			config.setServiceName(CHAT_HOST);
			config.setHost(CHAT_HOST);
			config.setPort(CHAT_PORT);
			config.setCompressionEnabled(false);
			config.setSendPresence(true);
			config.setDebuggerEnabled(true);
			try {
				eConnection = new XMPPTCPConnection(config.build());
				eConnection.connect();
				Roster roster = Roster.getInstanceFor(eConnection);
				roster.setRosterLoadedAtLogin(false);
				eConnection.login();
			} catch (Exception e) {
				System.err.println("Error in connect, " + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(Message message) throws InterruptedException {
		if (!isAuthenticated())
			return;
		try {
			eConnection.sendStanza(message);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(UserModel from, String toUserId, String body)
			throws InterruptedException {
		System.out.println("XmppManager, sending Message to "+toUserId);
		System.out.println("XmppManager, body; \n"+body);
		Message message = XmppHelper.createMessage(
				XmppHelper.makeJid(from.getUserId()), from.getNick(),
				from.getPhone(), XmppHelper.makeJid(toUserId), body);
		sendMessage(message);
	}

	public boolean isAuthenticated() {
		return eConnection == null ? false : eConnection.isAuthenticated();
	}

	public boolean isConnected() {
		return eConnection.isConnected();
	}

	public XMPPConnection getConnection() {
		return eConnection != null && eConnection.isConnected() ? eConnection
				: null;
	}

	@Override
	public void run() {
		while (true) {
			if (!isAuthenticated()) {
				System.out.println("Not conncetd to XMPP,  connceting...");
				login();
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
