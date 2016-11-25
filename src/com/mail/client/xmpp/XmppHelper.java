package com.mail.client.xmpp;

import org.jivesoftware.smack.packet.DefaultExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;
import org.jivesoftware.smackx.nick.packet.Nick;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

public class XmppHelper {

	private static String KEY_BODY_TYPE = "bodyType";
	private static String KEY_PHONE = "phone";
	private static String KEY_TEXT = "text";

	public static Message createMessage(String fromJid, String nick, String phone,
			String toJid, String body) {

		Message message = new Message();
		message.setBody(body);
		message.setTo(toJid);
		message.setType(Type.chat);
		addProperty(message, KEY_BODY_TYPE, KEY_TEXT);
		message.addExtension(new Nick(nick));
		DeliveryReceiptRequest.addTo(message);
		addProperty(message, KEY_PHONE, phone);

		return message;

	}

	 private static void addProperty(Message message, String key, String val) {
	        if (val == null) return;

	        DefaultExtensionElement extensionElement = message.getExtension(JivePropertiesExtension.ELEMENT, JivePropertiesExtension.NAMESPACE);
	        if (extensionElement == null) {
	            extensionElement = new DefaultExtensionElement(JivePropertiesExtension.ELEMENT, JivePropertiesExtension.NAMESPACE);
	            message.addExtension(extensionElement);
	        }

	        extensionElement.setValue(key, val);
	    }
	
	public static String makeJid(String userId){
		return String.format("%s@%s", userId, XMPPManager.CHAT_HOST);
	}
}
