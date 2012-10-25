package de.sosd.mediaserver.service.dlna;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.controller.DLNAController;
import de.sosd.mediaserver.service.MediaserverConfiguration;

@Service
public class UPNPNetwork {

	public static final String URN_X_MS_MEDIA_RECEIVER_REGISTRAR_1 = "urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1";
	public static final String URN_SCHEMAS_CONNECTION_MANAGER = "urn:schemas-upnp-org:service:ConnectionManager:1";
	public static final String URN_SCHEMAS_CONTENT_DIRECTORY = "urn:schemas-upnp-org:service:ContentDirectory:1";
	public static final String URN_SCHEMAS_MEDIA_SERVER = "urn:schemas-upnp-org:device:MediaServer:1";
	public static final String UPNP_ROOTDEVICE = "upnp:rootdevice";
	public final static String UPNP_HOST = "239.255.255.250";
	public final static byte[] UPNP_HOST_ADDRESS = {(byte) 239, (byte) 255, (byte) 255, (byte) 250 };
	public final static int UPNP_PORT = 1900;
	public final static String BYEBYE = "ssdp:byebye";
	public final static String ALIVE = "ssdp:alive";
	public static final String CRLF = "\r\n";


	private final static Log logger = LogFactory.getLog(UPNPNetwork.class);

	private static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);

	@Autowired
	private MediaserverConfiguration cfg;
	private InetAddress upnpGroup;

	private boolean offline = false;
	private MSearchThread listenerThread;
	
	
	private MulticastSocket getNewMulticastSocket() throws IOException {
		return getNewMulticastSocket(0);
	}

	private MulticastSocket getNewMulticastSocket(final int port) throws IOException {	
		return getNewMulticastSocket(findAddress(), port);
	}

	private InetAddress findAddress() throws IOException {
		Enumeration<NetworkInterface> ifaces = NetworkInterface
				.getNetworkInterfaces();

		while (ifaces.hasMoreElements()) {
			final NetworkInterface iface = ifaces.nextElement();
			if (iface.getName().equalsIgnoreCase(this.cfg.getNetworkInterface())) {

				final Enumeration<InetAddress> addressList = iface.getInetAddresses();
				while (addressList.hasMoreElements()) {
					final InetAddress address = addressList.nextElement();
					if (!(address instanceof Inet6Address)) {
						logger.trace("use ipv4 (" + address
								+ ") on interface: " + iface.getName());
						
						return address;

					} else {
						logger.trace("ignore ipv6 (" + address
								+ ") on interface: " + iface.getName());
					}
				}
				logger.trace("found not usable addresses on interface: "
						+ iface.getName());
			} else {
				logger.trace("ignore interface : " + iface.getName() + "!");
			}
		}
		ifaces = NetworkInterface.getNetworkInterfaces();
		// configured one does not work! try any working ipv4 iface
		while (ifaces.hasMoreElements()) {
			final NetworkInterface iface = ifaces.nextElement();
			if (iface.getName().toLowerCase().startsWith("eth")) {
				final Enumeration<InetAddress> addressList = iface.getInetAddresses();
				while (addressList.hasMoreElements()) {
					final InetAddress address = addressList.nextElement();
					if (!(address instanceof Inet6Address)) {
						logger.trace("use ipv4 (" + address + ") on interface: "
								+ iface.getName());
						final String hostAddress = address.getHostAddress();
						final String hostName = address.getHostName();
						final String interfaceName = iface.getName();
						this.cfg.setNetworkProperties(interfaceName, hostAddress, hostName);
						return address;
	
					} else {
						logger.trace("ignore ipv6 (" + address + ") on interface: "
								+ iface.getName());
					}
				}
				logger.trace("found not usable addresses on interface: "
						+ iface.getName());
			}

		}

		throw new IOException(
				"Network not configured. Please set network-interface! Currently this is : " + this.cfg.getNetworkInterface());		
	}
	
	private MulticastSocket getNewMulticastSocket(final InetAddress address, final int port)
			throws IOException {
		logger.trace("open multicast socket on addresse: " + address);
		MulticastSocket ssdpSocket;
		if (port == 0) {
			ssdpSocket = new MulticastSocket();
		} else {
			ssdpSocket = new MulticastSocket(port);
		}
		ssdpSocket.setReuseAddress(true);
		ssdpSocket.setInterface(address);
		logger.trace("Sending message from multicast socket on network interface: "
				+ ssdpSocket.getNetworkInterface());
		logger.trace("Multicast socket is on interface: "
				+ ssdpSocket.getInterface());
		ssdpSocket.setTimeToLive(32);
		// ssdpSocket.setLoopbackMode(true);
		this.upnpGroup = InetAddress.getByAddress(UPNP_HOST, UPNP_HOST_ADDRESS);
		ssdpSocket.joinGroup(this.upnpGroup);
		logger.trace("Socket Timeout: " + ssdpSocket.getSoTimeout());
		logger.trace("Socket TTL: " + ssdpSocket.getTimeToLive());

//		updateUSN(address);
		return ssdpSocket;
	}

	private MulticastSocket getNewMulticastListenSocket() throws IOException {
		return getNewMulticastSocket(UPNP_PORT);
	}

//	public void updateUSN(InetAddress address) {
//		try {
//			NetworkInterface nic = NetworkInterface.getByInetAddress(address);
//			UUID uuid = UUID.nameUUIDFromBytes(nic.getHardwareAddress());
//
//			cfg.setUSN(uuid.toString());
//		} catch (SocketException e) {
//
//		}
//	}
	

//	NOTIFY * HTTP/1.1
//
//	Host:[FF02::C]:1900
//
//	NT:uuid:39eb073b-14a7-4d00-a3c7-91ee88978a77
//
//	NTS:ssdp:byebye
//
//	Location:http://[fe80::6503:d31b:18c7:2f69]:2869/upnphost/udhisapi.dll?content=uuid:39eb073b-14a7-4d00-a3c7-91ee88978a77
//
//	USN:uuid:39eb073b-14a7-4d00-a3c7-91ee88978a77
//
//	Cache-Control:max-age=1800
//
//	Server:Microsoft-Windows-NT/5.1 UPnP/1.0 UPnP-Device-Host/1.0
//
//	OPT:"http://schemas.upnp.org/upnp/1/0/"; ns=01
//
//	01-NLS:bb72a37c619b42ac9f26689d1980cd7d

	public void sendByeBye() {
		logger.debug("Sending ByeBye...");
		sendStatus(BYEBYE);
		this.offline = true;
	}		
	
	private void sendStatus(final String state) {
		MulticastSocket ssdpSocket = null;
		try {
			logger.trace("create alive-socket");
			ssdpSocket = getNewMulticastSocket();
//			sendMessage(ssdpSocket, buildDiscoverMsg(URN_SCHEMAS_MEDIA_SERVER));
//			sendMessage(ssdpSocket, buildMsg(UPNP_ROOTDEVICE, ALIVE, true));
//			sendMessage(ssdpSocket, buildMsg(null, ALIVE, true));
			sendMessage(ssdpSocket,
					buildNotifyMsg(URN_SCHEMAS_MEDIA_SERVER, state));
//			sendMessage(ssdpSocket,
//					buildMsg(URN_SCHEMAS_CONTENT_DIRECTORY, ALIVE, true));
//			sendMessage(ssdpSocket,
//					buildMsg(URN_SCHEMAS_CONNECTION_MANAGER, ALIVE, true));
//			sendMessage(ssdpSocket, buildMsg( URN_X_MS_MEDIA_RECEIVER_REGISTRAR_1,  ALIVE,true));

			//ssdpSocket.leaveGroup(upnpGroup);
		} catch (final IOException e) {
			logger.error("io-error while sending " + state);
		} finally {
			if (ssdpSocket != null) {
				logger.trace("close alive-socket");
				ssdpSocket.close();
			}
		}
	}

	public void sendAlive() throws IOException {
		if (! this.offline) {
			logger.debug("Sending ALIVE...");
			sendStatus(ALIVE);
		}
	}

//	private void sendDiscover(String host, int port) throws IOException {
//		sendDiscover(host, port, null);
//	}

	/**
	 
-	HTTP/1.1 200 OK
	CACHE-CONTROL:max-age=1200
	DATE:Sat, 23 Jul 2011 22:13:42 GMT
-	LOCATION:http://192.168.101.227:9090/MediaserverWeb/dlna/description/fetch
	SERVER:Mediaserver
-	ST:urn:schemas-upnp-org:device:MediaServer:1
	EXT:
-	USN:uuid:614146ca-f169-3d3c-b228-87a712faf143::urn:schemas-upnp-org:device:MediaServer:1
	Content-Length:0 
	
	 **/
	
	/**
		 
-	HTTP/1.1 200 OK	
-	ST:urn:schemas-upnp-org:device:MediaServer:1	
-	USN:uuid:cdca376d-cb81-4b2e-95a7-9bf7dd2347a3::urn:schemas-upnp-org:device:MediaServer:1	
-	Location:http://192.168.101.227:2869/upnphost/udhisapi.dll?content=uuid:cdca376d-cb81-4b2e-95a7-9bf7dd2347a3	
	OPT:"http://schemas.upnp.org/upnp/1/0/"; ns=01	
	01-NLS:b6d7028a8b1e541d7e757d0b6db33a00	
	Cache-Control:max-age=900	
	Server:Microsoft-Windows-NT/5.1 UPnP/1.0 UPnP-Device-Host/1.0	
	Ext:
	
	 **/	
//	private void sendDiscover(String host, int port, String nt)
//			throws IOException {
//		
//
//
//		sendReply(host, port, buildDiscoverMsg(nt));
//	}



	private void sendReply(final InetAddress remoteAddress, final int port, final String msg)
			throws IOException {
		DatagramSocket socket = null;
		try {
			logger.trace("create reply-socket");
			socket = new DatagramSocket();

			final DatagramPacket dgmPacket = new DatagramPacket(msg.getBytes(),
					msg.length(), remoteAddress, port);

			socket.send(dgmPacket);
			
			logger.debug("replied ["+remoteAddress + ":" + port +"] ->\n" + msg);
		} finally {
			if (socket != null) {
				logger.trace("close reply-socket");
				socket.close();
			}
		}
	}

	private void sendMessage(final MulticastSocket ssdpSocket, final String msg)
			throws IOException {
		final DatagramPacket ssdpPacket = new DatagramPacket(msg.getBytes(),
				msg.length(), this.upnpGroup, UPNP_PORT);
		ssdpSocket.send(ssdpPacket);
		logger.trace("send message ["+this.upnpGroup + ":" + UPNP_PORT +"] -> " + msg);
	}

	private String buildNotifyMsg(String nt, final String message) {
		final StringBuffer sb = new StringBuffer();

		sb.append("NOTIFY * HTTP/1.1");
		sb.append(CRLF);
		sb.append("Host: " + UPNP_HOST + ":" + UPNP_PORT);
		sb.append(CRLF);
		String usn = "uuid:" + this.cfg.getUSN();
		if ((nt == null) || (nt.length() == 0)) {
			nt = usn;
		} else {
			usn += "::" + nt;
		}
		sb.append("NT: " + nt);	
		sb.append(CRLF);
		sb.append("NTS: " + message);
		sb.append(CRLF);
		sb.append("Location: " + this.cfg.getHttpServerUrl() + DLNAController.getDescriptionRef());
		sb.append(CRLF);		
		sb.append("USN: " + usn);
		sb.append(CRLF);
		sb.append("Cache-Control: max-age=1800");
		sb.append(CRLF);
		sb.append("Server: " + this.cfg.getServerName());
		sb.append(CRLF);
		sb.append(CRLF);
		return sb.toString();
	}
	
	private String buildDiscoverMsg(final String st) {
		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		final StringBuffer sb = new StringBuffer();
		String usn = this.cfg.getUSN();
		if (st.equals(usn)) {
			usn = "";
		} else {
			usn = "uuid:" + usn + "::";
		}
		
		sb.append("HTTP/1.1 200 OK");
		sb.append(CRLF);
		sb.append("ST:");
		if ((st != null) && (st.length() > 0)) {
			sb.append(st);
		}	
		sb.append(CRLF);		
		sb.append("USN:");
		sb.append(usn);
		sb.append(st);
		sb.append(CRLF);
		sb.append("Location:" + this.cfg.getHttpServerUrl()	+ DLNAController.getDescriptionRef());
		sb.append(CRLF);		
		sb.append("Cache-Control:max-age=1200");
		sb.append(CRLF);
		sb.append("Server:" + this.cfg.getServerName());
		sb.append(CRLF);
		sb.append("Date:" + sdf.format(new Date(System.currentTimeMillis()))
				+ " GMT");
		sb.append(CRLF);
		sb.append("EXT:");
		sb.append(CRLF);

		sb.append("Content-Length:0");
		sb.append(CRLF);
		sb.append(CRLF);
		
		return sb.toString();
	}

	private class MSearchThread extends Thread {
		
		private MulticastSocket listener;

		@Override
		public void run() {
			setName("UPnP M-Search");
			super.run();
				while (!UPNPNetwork.this.offline) {
					try {
						if (this.listener == null) {
							this.listener = getNewMulticastListenSocket();
						}
//						Socket socket = listener.accept();
//						BufferedInputStream in = new BufferedInputStream( socket.getInputStream() );
//						ByteArrayOutputStream baos = new ByteArrayOutputStream();
//						int read = 0;
//						do {
//							read = in.read(buffer);
//							
//							if (read > 0) {
//								baos.write(buffer, 0, read);
//							}
//						} while (read > 0); 
//						
//						int remotePort = socket.getPort();
//						InetAddress remoteIp = socket.getInetAddress();
//						socket.close();
//						
//						String data = new String(baos.toByteArray());
						final DatagramPacket data = new DatagramPacket(new byte[4096], 4096);
						this.listener.receive(data);
						final String text = new String(data.getData());
						boolean answered = true;
						if (text.contains("M-SEARCH")) {
							answered = false;
							for (final String schema : new String[]{URN_SCHEMAS_CONTENT_DIRECTORY, URN_SCHEMAS_MEDIA_SERVER, UPNP_ROOTDEVICE, URN_X_MS_MEDIA_RECEIVER_REGISTRAR_1, UPNPNetwork.this.cfg.getUSN()}) {
								if (text.contains(schema)) {
									sendReply(data.getAddress(), data.getPort(), buildDiscoverMsg(schema));
									answered = true;
								}
							}
							if (! answered) {
								logger.trace("ignored m-search : " + text);
							}
						}
//						if (!answered)
//						System.out.println(text);
//						listener.close();
//						listener = null;
						
					} catch (final Throwable e) {
						if (this.listener != null) {
							this.listener.close();
							this.listener = null;
						}
						logger.error("error while reading from upnp-network",e);
					} 
				}
	
			if (this.listener != null) {
				this.listener.close();
				this.listener = null;
			}

		}
		
	}
	
	public void startListening() {
		if (this.listenerThread == null) {
			this.listenerThread = new MSearchThread();
			this.offline = false;
			this.listenerThread.start();
		}
	}

	public void stopListening() {
		this.offline = true;
		if (this.listenerThread != null) {
			this.listenerThread.interrupt();
		}
		this.listenerThread = null;
	}

	
//	private static Thread listener = null;
//	private boolean isAlive;
//	private static MulticastSocket socket;
//	
//	private boolean openMulticastSocket() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		listener = new Thread() {
//			
//			@Override
//			public void run() {
//				super.run();
//				isAlive = openMulticastSocket();
//				while (isAlive) {
//					DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
//					try {
//						socket.receive(p);
//						
//						p.get
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					
//				}
//			}
//
//
//			
//		};
//		
//	}
//
//	@Override
//	public void destroy() throws Exception {
//		// TODO Auto-generated method stub
//		
//	}



//	private void sendDiscover(DatagramPacket packet_r, DatagramSocket socket) throws IOException {
//		String data = new String(packet_r.getData());
//		String remoteAddr = packet_r.getAddress().getHostAddress();
//		int remotePort = packet_r.getPort();
//
//		if (data.startsWith("M-SEARCH")) {
//			if (data.indexOf(UPNPNetwork.URN_SCHEMAS_CONTENT_DIRECTORY) > 0) {
//				logger.trace("Receiving a M-SEARCH from [" + remoteAddr + ":"
//						+ remotePort + "] discover-content");
//				sendDiscover(remoteAddr, remotePort,
//						UPNPNetwork.URN_SCHEMAS_CONTENT_DIRECTORY);
//			} else if (data.indexOf(UPNPNetwork.URN_SCHEMAS_CONNECTION_MANAGER) > 0) {
//				logger.trace("Receiving a M-SEARCH from [" + remoteAddr + ":"
//						+ remotePort + "] discover-connection");
//				sendDiscover(remoteAddr, remotePort,
//						UPNPNetwork.URN_SCHEMAS_CONNECTION_MANAGER);
//			} else 
//				if (data.indexOf(UPNPNetwork.URN_SCHEMAS_MEDIA_SERVER) > 0) {
//				logger.trace("Receiving a M-SEARCH from [" + remoteAddr + ":"
//						+ remotePort + "] discover-medias");
//				sendDiscover(remoteAddr, remotePort,
//						UPNPNetwork.URN_SCHEMAS_MEDIA_SERVER);
//			} 
//				else if (data
//					.indexOf(UPNPNetwork.URN_X_MS_MEDIA_RECEIVER_REGISTRAR_1) > 0) {
//				logger.trace("Receiving a M-SEARCH from [" + remoteAddr + ":"
//						+ remotePort + "] discover-ms-registrar");
//				sendDiscover(remoteAddr, remotePort,
//						UPNPNetwork.URN_X_MS_MEDIA_RECEIVER_REGISTRAR_1);
//			} else if (data.indexOf(UPNPNetwork.UPNP_ROOTDEVICE) > 0) {
//				logger.trace("Receiving a M-SEARCH from [" + remoteAddr + ":"
//						+ remotePort + "] discover-root");
//				sendDiscover(remoteAddr, remotePort,
//						UPNPNetwork.UPNP_ROOTDEVICE);
//			} 
//			
//			else
//
//			if (data.indexOf(cfg.getUSN()) > 0) {
//				logger.trace("Receiving a M-SEARCH from [" + remoteAddr + ":"
//						+ remotePort + "] discover-usn");
//				sendDiscover(remoteAddr, remotePort);
//			} else 
//			{
//				//logger.debug("unknown discovery : " + data);
//			}
//		} else {
////			logger.trace("don't handle data from [" + remoteAddr + ":" + remotePort + "] -> " + data);
//		}
//
////		if (data.startsWith("NOTIFY")) {
////			logger.trace("Receiving a NOTIFY from [" + remoteAddr + ":" + remotePort + "]");
////		}
//	}

	
}
