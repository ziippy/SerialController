package com.skcc.acs;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skcc.adapter.serial.SerialController;

public class SubscribeProcessor implements MessageListener {
	private static Logger logger = LoggerFactory.getLogger(SubscribeProcessor.class);
	
	private String cctvId = "";
	private SerialController serialController = null;
	private String usbPortName = "";		

	public SubscribeProcessor(String cctvId, SerialControllerManager serialControllerManager) {
		this.cctvId = cctvId;
		try {
			this.serialController = serialControllerManager.getSerialController(cctvId);
		} catch (Exception e) {
			logger.error("SubscribeProcessor Exception: " + e.toString());
		}
		if (this.serialController != null)
			this.usbPortName = this.serialController.getPortName();		 
	}
	
	@Override
	public void onMessage(Message message) {
		//System.out.println(message);
		
		//logger.info("1111111111111111111111");
		boolean needToOpenDoor = false;
		String personId = "";
		
		try {
			String data = ((TextMessage)message).getText();
			
			JSONObject jsonObj = new JSONObject(data);
			String includeMatchingData = jsonObj.getString("includeMatchingData");
			//System.out.println("includeMatchingData => " + includeMatchingData);
			if ("yes".equalsIgnoreCase(includeMatchingData)) {
				needToOpenDoor = true;
				//logger.info("222222222222222");
				
				try {
					JSONObject jsonObjMatchingInfo = jsonObj.getJSONObject("matchingInfo");
					personId = jsonObjMatchingInfo.getString("personId");
				} catch (Exception e) {
					logger.error("receiveMessage Exception2: " + e.toString());
				}
				
				//logger.info("3333333333333333333");				
			}
		} catch (Exception e) {
			logger.error("receiveMessage Exception: " + e.toString());
		}
		
		if (needToOpenDoor == true) {
			logger.info("VAS_ACS_CONTROLLER >>>>> Open the door (personId: {}) -> ({}, {})", personId, cctvId, usbPortName);
			if (serialController != null) {
				serialController.doorControl(SerialController.DOOR_OPEN);	// DOOR_OPEN, DOOR_CLOSE
			}
			//logger.info("VAS_ACS_CONTROLLER >>>>> after Open the door (personId: {}) -> ({}, {})", personId, cctvId, usbPortName);
		}
	}
	
	/*
	@JmsListener(destination = "${gate.opendoor.cctv.id}")
	public String receiveMessage(final Message jsonMessage) {
		//System.out.println("Received message: " + jsonMessage);

		boolean needToOpenDoor = false;
		String personId = "";
		
		try {
			String payload = (String)jsonMessage.getPayload();
			//System.out.println("Payload: " + response);
			JSONObject jsonObj = new JSONObject(payload);
			String includeMatchingData = jsonObj.getString("includeMatchingData");
			//System.out.println("includeMatchingData => " + includeMatchingData);
			if ("yes".equalsIgnoreCase(includeMatchingData)) {
				needToOpenDoor = true;
				
				try {
					JSONObject jsonObjMatchingInfo = jsonObj.getJSONObject("matchingInfo");
					personId = jsonObjMatchingInfo.getString("personId");
				} catch (Exception e) {
					logger.error("receiveMessage Exception2: " + e.toString());
				}				
				
			}
		} catch (Exception e) {
			logger.error("receiveMessage Exception: " + e.toString());
		}
		
		if (needToOpenDoor == true && this.serialController != null) {
			logger.info("VAS_ACS_CONTROLLER >>>>> Open the door (personId: {}) -> ({}, {})", personId, cctvId, usbPortName);
			serialController.doorControl(SerialController.DOOR_OPEN);	// DOOR_OPEN, DOOR_CLOSE
		}
		
		return null;
	}
	 */	
}