package com.mail.client.service;

//http://javapapers.com/java/receive-email-in-java-using-javamail-gmail-imap-example/

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import org.apache.log4j.Logger;

import com.mail.client.MailHelper;
import com.mail.client.dao.UserDao;
import com.mail.client.model.UserModel;
import com.mail.client.xmpp.XMPPManager;

public class MailService extends Thread {

	Logger logger = Logger.getLogger(MailService.class);

	private static String host = "mail.server.com";
	private static int port = 143;
	private String protocol = "imap";
	private String file = "INBOX";
	private Session session;
	private Store store;
	private Folder folder;
	private static String username = "sample_user";
	private static String password = "changeit";
	private static MailService mailService;

	private MailService() {
	}

	/**
	 * @return
	 */
	public static MailService getInstance() {
		if (mailService == null) {
			mailService = new MailService();
			mailService.init();
		}
		return mailService;
	}

	/**
	 * @return
	 */
	public boolean isLoggedIn() {
		return store.isConnected();
	}

	/**
	 * @author dharam
	 * 
	 * Init mail service
	 * 
	 */
	private void init() {
		if (session == null) {
			Properties props = null;
			try {
				props = System.getProperties();
			} catch (SecurityException sex) {
				props = new Properties();

			}
			try {
				URLName url = new URLName(protocol, host, port, file, username,
						password);
				session = Session.getInstance(props, null);
				store = session.getStore(url);
				// session.setDebug(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @author dharam
	 * 
	 * to login to the mail host server
	 */
	public void login() throws Exception {
		URLName url = new URLName(protocol, host, port, file, username,
				password);
		store = session.getStore(url);
		store.connect();
		folder = store.getFolder(url);
		folder.open(Folder.READ_WRITE);
		System.out.println("====== Connected to SMP server =====");
	}

	/**
	 * to logout from the mail host server
	 */
	public void logout() throws MessagingException {
		folder.close(false);
		store.close();
		store = null;
		session = null;
	}

	public Message[] getMessages() throws MessagingException {
		return folder.getMessages();
	}

	public Message[] getMessages(int start, int end) throws MessagingException {
		return folder.getMessages(start, end);
	}

	/**
	 * @author dharam
	 * 
	 * mail service, always running and check incoming mail
	 */
	public void startMailService() {
		if (!isLoggedIn())
			return;
		try {
			while (true) {
				int newmails = folder.getNewMessageCount();
				if (newmails > 0) {
					System.out.println("New mail received");
					loadNewMails();
				} else {
					System.out.println("No new mail");
				}
				Thread.sleep(3000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author dharam
	 * 
	 * Load new mails from SMTP server
	 * 
	 */
	public void loadNewMails() {
		System.out.println("MailService, inside loadNewMails");
		try {
			int totalMails = folder.getMessageCount();
			int newmails = folder.getNewMessageCount();

			Message[] messages = this.getMessages((totalMails - newmails) + 1,
					totalMails);

			if (messages == null)
				return;
			System.out.println("Loaded new mails " + messages.length);
			for (Message message : messages) {
				processMail(message);
			}
			folder.close(false);
			folder.open(Folder.READ_WRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processMail(Message message) {
		System.out.println("Inside processMail");
		if (message == null) {
			return;
		}
		try {
			System.out.println("MessageNumber: " + message.getMessageNumber());
			System.out.println("Content Type: " + message.getContentType());

			String subject = message.getSubject();
			String body = MailHelper.getTextFromMessage(message);
			body = String.format("Subject: %s %s %s", subject, "\n\n", body)
					.trim();

			String from = "";
			Address[] addresses = message.getFrom();
			for (Address address : addresses) {
				from = address.toString();
				break;
			}
			Address[] recvrs = message.getAllRecipients();
			for (Address address : recvrs) {
				sendMailToXmpp(from, address.toString(), body);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author dharam
	 * 
	 * 
	 * 
	 * @param fromMailId
	 * @param toMailId
	 * @param body
	 */
	private void sendMailToXmpp(String fromMailId, String toMailId, String body) {
		System.out.println("inside sendMailToXmpp...");
		UserDao userDao = new UserDao();
		UserModel fromUser = null;
		if (fromUser == null) {
			fromUser = new UserModel();
			fromUser.setUserId("");
			fromUser.setNick("");
			fromUser.setPhone("");
		}
		System.out.println("toMailId : " + toMailId);
		UserModel touser = userDao.getUserById(toMailId);
		if (touser == null || touser.getUserId() == null) {
			System.out.println("Errro, No receipant found in XMPP");
			return;
		}
		try {
			XMPPManager.getInstance().sendMessage(fromUser, touser.getUserId(),
					body);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			if (!isLoggedIn()) {
				System.out.println("SMTP Not loggedIn, Trying to login");
				try {
					login();
					Thread.sleep(500);
					startMailService();
				} catch (Exception e) {
					logger.error("Error in SMTP login, " + e.getMessage());
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
