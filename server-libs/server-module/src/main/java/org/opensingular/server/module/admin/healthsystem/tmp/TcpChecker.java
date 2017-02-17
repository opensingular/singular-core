package org.opensingular.server.module.admin.healthsystem.tmp;

import java.net.Socket;

import org.opensingular.form.type.core.SIString;
import org.opensingular.form.validation.IInstanceValidatable;

public class TcpChecker implements IProtocolChecker {

	@Override
	public void protocolCheck(IInstanceValidatable<SIString> validatable) {
		String url = validatable.getInstance().getValue().replace("tcp://", "");
		String[] piecesSocketPath = url.split(":");
		Socket testClient;
		try {
			testClient = new Socket(piecesSocketPath[0], Integer.valueOf(piecesSocketPath[piecesSocketPath.length-1]));
			testClient.close();
		} catch (Exception e) {
			validatable.error(e.getMessage());
		} 
	}
}
