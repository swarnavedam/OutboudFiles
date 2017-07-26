import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class OutboundFiles {

	public static void main(String[] args) {
		ChannelSftp channelSftp = null;
		String SFTPWORKINGDIR = "/data_mgmt/outbound/ha/";
		try {
			JSch jsch = new JSch();
			String user = "swarna.vedam";
			String host = "transfer.cafewell.com";
			int port = 2222;

			String privateKey = "/Users/swarna.vedam/.ssh/id_rsa";
			jsch.addIdentity(privateKey, "swarnavedam");
			System.out.println("Identity Added");

			Session session = jsch.getSession(user, host, port);
			System.out.println("Session Created");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			System.out.println("Session Connected");

			Channel channel = session.openChannel("sftp");
			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);
			channel.connect();
			System.out.println("Shell Channel Connected");

			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(SFTPWORKINGDIR);
			System.out.println("Change the directory");
			byte[] buffer = new byte[1024];

			Vector<LsEntry> lsFiles = channelSftp.ls("*");

			Vector<ChannelSftp.LsEntry> foundTSFiles = new Vector<ChannelSftp.LsEntry>();
			Vector<ChannelSftp.LsEntry> foundQAFiles = new Vector<ChannelSftp.LsEntry>();
			Iterator it = lsFiles.iterator();

			while (it.hasNext()) {
				LsEntry lsentry = (LsEntry) it.next();
				if (lsentry
						.getFilename()
						.matches(
								"HA_TS_HNNY_86_201707.*|HA_TS_HNNY_86_201706.*|HA_TS_HNNY_86_201705.*")) {
					foundTSFiles.add(lsentry);
				} else if (lsentry
						.getFilename()
						.matches(
								"HA_QA_HNNY_86_201707.*|HA_QA_HNNY_86_201706.*|HA_QA_HNNY_86_201705.*")) {
					foundQAFiles.add(lsentry);
				}
			}

			File newFile = new File(
					"/Users/swarna.vedam/Desktop/outbound_TS.txt");
			OutputStream osts = new FileOutputStream(newFile);
			BufferedOutputStream bosts = new BufferedOutputStream(osts);

			Iterator iterator = foundTSFiles.iterator();
			Boolean firstFlag = true;
			String header = "";

			while (iterator.hasNext()) {
				LsEntry lsentry = (LsEntry) iterator.next();
				// BufferedInputStream bis = new BufferedInputStream(
				// channelSftp.get(lsentry.getFilename()));

				BufferedReader brs = new BufferedReader(new InputStreamReader(
						channelSftp.get(lsentry.getFilename())));

				String fileName = lsentry.getFilename();
				String line;

				while ((line = brs.readLine()) != null) {
					if (firstFlag) {
						header = line;
						firstFlag = false;
						line = line + "|FILE_NAME\n";
						bosts.write(line.getBytes());
						System.out.println("first here:  " + line);
					}
					// System.out.println("header:  "+ header);
					else if (!line.startsWith("RowCount")
							&& !line.equalsIgnoreCase(header)) {

						line = line + "|" + fileName + "\n";
						bosts.write(line.getBytes());
					}

				}

				System.out.println("Done file:  " + fileName);
				brs.close();
			}

			bosts.close();

			File newQAFile = new File(
					"/Users/swarna.vedam/Desktop/outbound_QA.txt");
			OutputStream osqs = new FileOutputStream(newQAFile);
			BufferedOutputStream bosqs = new BufferedOutputStream(osqs);

			iterator = foundQAFiles.iterator();
			firstFlag = true;
			header = "";

			while (iterator.hasNext()) {
				LsEntry lsentry = (LsEntry) iterator.next();
				// BufferedInputStream bis = new BufferedInputStream(
				// channelSftp.get(lsentry.getFilename()));

				BufferedReader brs = new BufferedReader(new InputStreamReader(
						channelSftp.get(lsentry.getFilename())));

				String fileName = lsentry.getFilename();
				String line;

				while ((line = brs.readLine()) != null) {
					if (firstFlag) {
						header = line;
						firstFlag = false;
						line = line + "|FILE_NAME\n";
						bosqs.write(line.getBytes());
						System.out.println("first here:  " + line);
					}
					// System.out.println("header:  "+ header);
					else if (!line.startsWith("RowCount")
							&& !line.equalsIgnoreCase(header)) {

						line = line + "|" + fileName + "\n";
						bosqs.write(line.getBytes());
					}

				}

				System.out.println("Done file:  " + fileName);
				brs.close();
			}

			bosqs.close();

			session.disconnect();
			channel.disconnect();

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();

		}

	}
}
