package com.skcc.acs;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.skcc.adapter.serial.SerialController;

@Component
public class SerialControllerManager {
	private static Logger logger = LoggerFactory.getLogger(SerialControllerManager.class);
	
	private Map<String, Integer> mapIndexOfSC = new HashMap<String, Integer>();		// (1165, 0) (1166, 1) (1167, 1)
	private Map<Integer, SerialController> mapSerialController = new HashMap<Integer, SerialController>();
	
	public SerialControllerManager(Environment env) {
		String cctvIds = env.getProperty("gate.opendoor.cctv.ids", "");		// 구분자 ; 로 다수 개
		String portNames = env.getProperty("gate.opendoor.ports", "");		// 구분자 ; 로 다수 개
		String strAutoOff = env.getProperty("gate.opendoor.autooff", "true");
		String strAutoOffTimeout = env.getProperty("gate.opendoor.autooff.time", "1000");
		
		String mode = SerialController.MODE_USB;					// MODE_USB, MODE_WIEGAND
		boolean autoOff = SerialController.AUTO_OFF_ENABLE;			// AUTO_OFF_ENABLE, AUTO_OFF_DISABLE
		if (strAutoOff != null && strAutoOff.equalsIgnoreCase("false")) {
			autoOff = SerialController.AUTO_OFF_DISABLE;
		}
		int autoOffTimeout = 1000;	// ms
		if (strAutoOffTimeout != null && Integer.valueOf(strAutoOffTimeout) > autoOffTimeout) {
			autoOffTimeout = Integer.valueOf(strAutoOffTimeout);
		}
				
		int bitrate = 9600;
		int databit = 8;
		int stopbit = 1;
		int parity = 0;
		
		// [1] ; 를 기준으로 cctvIds, portNames 파싱
		String cctvIdList[] = cctvIds.split(";");
		String portNameList[] = portNames.split(";");
		
		if (cctvIdList.length == 0 || portNameList.length == 0) {
			logger.error("gate.opendoor.cctv.ids or gate.opendoor.ports information is missing");
			return;
		}

		// [2] SerialController 생성
		for (int i=0; i<portNameList.length; i++) {
			// cctvIdList 개수 까지만 생성
			if (i >= cctvIdList.length)
				break;
			
			String portName = portNameList[i];
			SerialController sc = new SerialController(portName, mode, autoOff);
			if (sc != null) {
				logger.info("serialController - create ok ({}) (index= {})", portName, i);
				sc.setPortInfo(bitrate, databit, stopbit, parity);
				sc.setAutoOffTimeout(autoOffTimeout);
				logger.info("serialController - init ok ({}) (index= {})", portName, i);
				//
				mapSerialController.put(i, sc);
			}
		}
		
		// [3] cctvId 별로 몇 번째 index 의 SerialController 를 사용하면 되는 지 파싱
		for (int i=0; i<cctvIdList.length; i++) {
			String cctvId = cctvIdList[i];
			// cctvIdList 와 portNameList 와의 길이가 다를 수 있는데, 이 때는 마지막 ports 를 같이 사용하는 것으로 함
			int indexOfSC = i;
			if (i >= portNameList.length) {
				indexOfSC = portNameList.length-1;
			}
			//
			logger.info("index of SC for {} = {}", cctvId, indexOfSC);
			mapIndexOfSC.put(cctvId, indexOfSC);
		}
	}
	
	public SerialController getSerialController(String cctvId) {
		int index = mapIndexOfSC.get(cctvId);
		return this.mapSerialController.get(index);
	}
	
	@PreDestroy
	public void destroy() {
		for(int index: mapSerialController.keySet()) {
			SerialController sc = mapSerialController.get(index);
			if (sc != null) {
				sc.finalize();
			}
		}

		mapSerialController.clear();
		mapIndexOfSC.clear();
	}
}
