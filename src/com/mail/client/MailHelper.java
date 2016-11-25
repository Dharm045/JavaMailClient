package com.mail.client;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.internet.MimeMultipart;

public class MailHelper {
	
	public static void flags(Flags flags){
		Flag[] sysFlag = flags.getSystemFlags();
		String[] userFlag = flags.getUserFlags();
		for(Flag flag : sysFlag){
			System.out.println("System RECENT: " + flag);
		}
		for(String flag : userFlag){
			System.out.println("User Flag: " + flag);
		}
	}

	public static String getTextFromMessage(Message message) throws Exception {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart)
			throws Exception {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result
						+ getTextFromMimeMultipart((MimeMultipart) bodyPart
								.getContent());
			}
		}
		return result;
	}
}
